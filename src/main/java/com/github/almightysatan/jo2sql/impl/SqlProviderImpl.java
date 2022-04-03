/*
 * JO2SQL
 * Copyright (C) 2022  Almighty-Satan
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package com.github.almightysatan.jo2sql.impl;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.almightysatan.jo2sql.AsyncDatabaseException;
import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.DatabaseAction;
import com.github.almightysatan.jo2sql.PreparedDelete;
import com.github.almightysatan.jo2sql.PreparedObjectDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.SqlProvider;
import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.datatypes.BoolDataType;
import com.github.almightysatan.jo2sql.impl.datatypes.IntDataType;
import com.github.almightysatan.jo2sql.impl.datatypes.LongDataType;

public abstract class SqlProviderImpl implements SqlProvider {

	private static final List<DataType> DATA_TYPES = Arrays.asList(new BoolDataType(), new IntDataType(),
			new LongDataType());

	private final List<DataType> types;
	private final ExecutorService exectuor = Executors.newSingleThreadExecutor();
	private final Logger logger = LoggerFactory.getLogger("JO2SQL");
	private final Map<Class<?>, Table<?>> tables = new ConcurrentHashMap<>();
	private Connection connection;

	public SqlProviderImpl(List<DataType> types) {
		types.addAll(DATA_TYPES);
		this.types = types;
	}

	private Connection getValidConnection() throws SQLException {
		if (this.connection == null || !this.connection.isValid(30)) {
			if (this.connection != null) {
				this.logger.debug("Connection to database lost! Reconnecting...");
				try {
					this.connection.close();
				} catch (Throwable t) {
					// ignore
				}
			}
			this.connection = this.createConnection();
		}
		return this.connection;
	}

	protected abstract Connection createConnection() throws SQLException;

	@SuppressWarnings("unchecked")
	private <T extends SqlSerializable> Table<T> getTable(Class<T> type) {
		if (this.tables.containsKey(type))
			return (Table<T>) this.tables.get(type);
		else {
			try {
				Table<T> table = this.newTable(type);

				Optional<Table<?>> duplicate = this.tables.values().stream()
						.filter(t -> t.getName().equals(table.getName())).findAny();
				if (duplicate.isPresent())
					throw new Error(String.format("Duplicate table name in classes %s and %s",
							table.getType().getName(), duplicate.get().getType().getName()));
				this.tables.put(type, table);
				return table;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new Error("Error while loading class", e);
			}
		}
	}

	protected abstract <T extends SqlSerializable> Table<T> newTable(Class<T> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException;

	DataType getDataType(Class<?> type) {
		for (DataType value : this.types)
			for (Class<?> t : value.getClasses())
				if (t == type)
					return value;
		throw new Error("Unsupported type: " + type.getName());
	}

	@Override
	public <T extends SqlSerializable> DatabaseAction<Void> createIfNecessary(Class<T> type) {
		return this.createDatabaseAction(() -> {
			this.getTable(type).createIfNecessary();
			return null;
		});
	}

	@Override
	public <T extends SqlSerializable> PreparedReplace<T, Void> prepareReplace(Class<T> type) {
		return this.getTable(type).prepareReplace();
	}

	@Override
	public <T extends SqlSerializable> PreparedReplace<T, Long> prepareAiReplace(Class<T> type) {
		return this.getTable(type).prepareAiReplace();
	}

	@Override
	public <T extends SqlSerializable> PreparedSelect<T> prepareSelect(Class<T> type, String... keys) {
		return this.getTable(type).prepareSingleSelect(keys);
	}

	@Override
	public <T extends SqlSerializable> PreparedSelect<T[]> prepareMultiSelect(Class<T> type, String... keys) {
		return this.getTable(type).prepareMultiSelect(keys);
	}

	@Override
	public <T extends SqlSerializable> PreparedObjectDelete<T> prepareObjectDelete(Class<T> type) {
		return this.getTable(type).prepareObjectDelete();
	}

	@Override
	public <T extends SqlSerializable> PreparedDelete prepareDelete(Class<T> type, String... keys) {
		return this.getTable(type).prepareDelete(keys);
	}

	protected <T> DatabaseAction<T> createDatabaseAction(ThrowableSupplier<T> action) {
		return new DatabaseAction<T>() {

			@Override
			public T complete() throws AsyncDatabaseException {
				Future<T> future = SqlProviderImpl.this.exectuor.submit(() -> {
					try {
						return action.run();
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				});
				try {
					return future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new AsyncDatabaseException(e);
				}
			}

			@Override
			public T completeUsafe() {
				try {
					return this.complete();
				} catch (AsyncDatabaseException e) {
					throw new Error("Async error", e);
				}
			}

			@Override
			public void queue(ExecutorService callbackExecutor, Consumer<T> success, Consumer<Throwable> error) {
				SqlProviderImpl.this.exectuor.execute(() -> {
					try {
						T result = action.run();
						if (success != null)
							callbackExecutor.execute(() -> success.accept(result));
					} catch (Throwable t) {
						if (error != null)
							callbackExecutor.execute(() -> error.accept(t));
						else
							SqlProviderImpl.this.logger.error("SQL error", t);
					}
				});
			}

			@Override
			public void queue(ExecutorService callbackExecutor, Consumer<T> success) {
				this.queue(callbackExecutor, success, null);
			}

			@Override
			public void queue(Consumer<T> success, Consumer<Throwable> error) {
				SqlProviderImpl.this.exectuor.execute(() -> {
					try {
						T result = action.run();
						if (success != null)
							success.accept(result);
					} catch (Throwable t) {
						if (error != null)
							error.accept(t);
						else
							SqlProviderImpl.this.logger.error("SQL error", t);
					}
				});
			}

			@Override
			public void queue(Consumer<T> success) {
				this.queue(success, null);
			}

			@Override
			public void queue() {
				this.queue((Consumer<T>) null, null);
			}
		};
	}

	public CachedStatement prepareStatement(String sql) throws SQLException {
		this.logger.debug("Preparing statement {}", sql);
		return new CachedStatement(sql, (int) sql.chars().filter(c -> c == '?').count());
	}

	public int executeUpdate(CachedStatement cachedStatement) throws SQLException {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this.getValidConnection());
		this.logger.debug("Executing prepared update statement {}", statement.toString());
		return statement.executeUpdate();
	}

	public ResultSet executeQuery(CachedStatement cachedStatement) throws SQLException {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this.getValidConnection());
		this.logger.debug("Executing prepared query statement {}", statement.toString());
		return statement.executeQuery();
	}

	public int executeUpdate(String sql) throws SQLException {
		this.logger.debug("Executing update statement {}", sql);
		return this.getValidConnection().createStatement().executeUpdate(sql);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		this.logger.debug("Executing query statement {}", sql);
		return this.getValidConnection().createStatement().executeQuery(sql);
	}

	public Logger getLogger() {
		return this.logger;
	}
}
