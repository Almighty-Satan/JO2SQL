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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
import com.github.almightysatan.jo2sql.logger.Logger;

public abstract class SqlProviderImpl implements SqlProvider {

	private static final List<DataType> DATA_TYPES = Arrays.asList(new BoolDataType(), new IntDataType(),
			new LongDataType());

	private final List<DataType> types;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final Logger logger;
	private final Map<Class<?>, Table<?>> tables = new ConcurrentHashMap<>();
	private Connection connection;

	public SqlProviderImpl(Logger logger, List<DataType> types) {
		this.logger = logger;
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
			public Future<T> queue(ExecutorService callbackExecutor, Consumer<T> success, Consumer<Throwable> error) {
				return SqlProviderImpl.this.executor.submit(() -> {
					try {
						T result = action.run();
						this.executeCallback(callbackExecutor, success, result);
						return result;
					} catch (Throwable t) {
						if (error != null)
							this.executeCallback(callbackExecutor, error, t);
						else
							SqlProviderImpl.this.logger.error("SQL error", t);
						return null;
					}
				});
			}

			private <R> void executeCallback(ExecutorService executor, Consumer<R> callback, R result) {
				if (callback != null) {
					if (executor == null || executor == SqlProviderImpl.this.executor)
						callback.accept(result);
					else
						executor.execute(() -> callback.accept(result));
				}
			}
		};
	}

	public CachedStatement prepareStatement(String sql) throws SQLException {
		this.logger.debug("Preparing statement %s", sql);
		return new CachedStatement(sql, (int) sql.chars().filter(c -> c == '?').count());
	}

	public int executeUpdate(CachedStatement cachedStatement) throws SQLException {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this.getValidConnection());
		this.logger.debug("Executing prepared update statement %s", statement.toString());
		return statement.executeUpdate();
	}

	public ResultSet executeQuery(CachedStatement cachedStatement) throws SQLException {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this.getValidConnection());
		this.logger.debug("Executing prepared query statement %s", statement.toString());
		return statement.executeQuery();
	}

	public int executeUpdate(String sql) throws SQLException {
		this.logger.debug("Executing update statement %s", sql);
		return this.getValidConnection().createStatement().executeUpdate(sql);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		this.logger.debug("Executing query statement %s", sql);
		return this.getValidConnection().createStatement().executeQuery(sql);
	}

	@Override
	public void terminate() {
		this.executor.shutdown();
	}

	@Override
	public void terminate(long timeout, TimeUnit timeUnit) throws InterruptedException {
		this.executor.shutdown();
		this.executor.awaitTermination(timeout, timeUnit);
	}

	public Logger getLogger() {
		return this.logger;
	}
}
