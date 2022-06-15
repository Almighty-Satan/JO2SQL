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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.github.almightysatan.jo2sql.Column;
import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.DatabaseAction;
import com.github.almightysatan.jo2sql.MapColumn;
import com.github.almightysatan.jo2sql.PreparedDelete;
import com.github.almightysatan.jo2sql.PreparedObjectDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.Selector;
import com.github.almightysatan.jo2sql.SqlProvider;
import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.Table;
import com.github.almightysatan.jo2sql.impl.types.BoolType;
import com.github.almightysatan.jo2sql.impl.types.IntType;
import com.github.almightysatan.jo2sql.impl.types.LongType;
import com.github.almightysatan.jo2sql.logger.Logger;

public abstract class SqlProviderImpl implements SqlProvider {

	public static final DataType BOOL_TYPE = new BoolType();
	public static final DataType INT_TYPE = new IntType();
	public static final DataType LONG_TYPE = new LongType();

	private static final List<DataType> UNIVERSAL_DATA_TYPES = Arrays.asList(BOOL_TYPE, INT_TYPE, LONG_TYPE);

	private final List<DataType> types;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private Thread thread;
	private final Logger logger;
	private final Map<Class<?>, TableImpl<?>> tables = new HashMap<>();
	private Connection connection;

	public SqlProviderImpl(Logger logger, List<DataType> types) {
		this.logger = logger;
		types.addAll(UNIVERSAL_DATA_TYPES);
		types.add(this.getStringType());
		this.types = types;

		this.executor.submit(() -> this.thread = Thread.currentThread());
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

	protected abstract DataType getStringType();

	protected abstract DataType getAiLongType();

	protected abstract long getLastInsertId(String tableName) throws Throwable;

	@SuppressWarnings("unchecked")
	public synchronized <T> TableImpl<T> getOrCreateTable(Class<T> type) {
		if (this.tables.containsKey(type))
			return (TableImpl<T>) this.tables.get(type);
		else {
			try {
				TableImpl<T> table = this.newTable(new SerializableClass<>(this, type));

				Optional<TableImpl<?>> duplicate = this.tables.values().stream()
						.filter(t -> t.getType().getName().equals(table.getType().getName())).findAny();
				if (duplicate.isPresent())
					throw new Error(String.format("Duplicate table name in %s and %s", type,
							duplicate.get().getType().getType()));
				this.tables.put(type, table);
				return table;
			} catch (Throwable e) {
				throw new Error("Error while loading class", e);
			}
		}
	}

	public abstract <T> TableImpl<T> newTable(SerializableObject<T> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException;

	public DataType getDataType(Class<?> type) {
		throw new UnsupportedOperationException();
	}

	public SerializableAttribute createSerializableAttribute(Class<?> clazz, String tableName, String columnName,
			int size) throws Throwable {
		for (DataType type : this.types)
			if (type.isOfType(clazz))
				return new SimpleSerializableAttribute(type, tableName, columnName, size);

		throw new Error(String.format("Unsupported type of field %s", columnName));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SerializableAttribute createSerializableAttribute(Class<?> clazz, String columnName, int size, boolean ai,
			SerializableObject<?> parent) throws Throwable {
		StringUtil.assertAlphanumeric(columnName);

		if (ai) {
			DataType aiLongType = this.getAiLongType();
			if (aiLongType.isOfType(clazz))
				return new SimpleSerializableAttribute(aiLongType, parent.getName(), columnName, size);
			else
				throw new Error(String.format("Invalid auto increment type in class %s", parent.getType().getName()));
		}

		if (clazz.isAnnotationPresent(SqlSerializable.class))
			return new SerializableNestedClassAttribute(this, clazz, columnName);

		return this.createSerializableAttribute(clazz, parent.getName(), columnName, size);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SerializableAttribute createSerializableAttribute(Field field, Column annotation,
			SerializableObject<?> parent) throws Throwable {
		Class<?> clazz = annotation.type() == void.class ? field.getType() : annotation.type();

		MapColumn mapAnnotation = field.getAnnotation(MapColumn.class);
		if (mapAnnotation != null && Map.class.isAssignableFrom(clazz))
			return new AnnotatedField(this, field, annotation,
					new SerializableMapAttribute(this, annotation.type(), mapAnnotation.keyType(),
							mapAnnotation.keySize(), mapAnnotation.valueType(), mapAnnotation.valueSize(),
							annotation.value(), parent));

		return this.createSerializableAttribute(clazz, annotation.value(), annotation.size(),
				annotation.autoIncrement(), parent);
	}

	public AnnotatedField createAnnotatedField(Field field, Column annotation, SerializableObject<?> parent)
			throws Throwable {
		return new AnnotatedField(this, field, annotation, this.createSerializableAttribute(field, annotation, parent));
	}

	@Override
	public <T> DatabaseAction<Void> createIfNotExists(Class<T> type) {
		TableImpl<T> table = this.getOrCreateTable(type);
		return this.createDatabaseAction(table::createIfNotExists);
	}

	@Override
	public <T> DatabaseAction<Void> dropIfExists(Class<T> type) {
		TableImpl<T> table = this.getOrCreateTable(type);
		return this.createDatabaseAction(table::dropIfExists);
	}

	@Override
	public <T> PreparedReplace<T, Void> prepareReplace(Class<T> type) {
		return this.getOrCreateTable(type).prepareReplace();
	}

	@Override
	public <T> PreparedReplace<T, Long> prepareAiReplace(Class<T> type) {
		return this.getOrCreateTable(type).prepareAiReplace();
	}

	@Override
	public <T> PreparedSelect<T> preparePrimarySelect(Class<T> type) {
		return this.getOrCreateTable(type).preparePrimarySelect();
	}

	@Override
	public <T> PreparedSelect<T> prepareSelect(Class<T> type, Selector selector) {
		return this.getOrCreateTable(type).prepareSingleSelect((SelectorImpl) selector);
	}

	@Override
	public <T> PreparedSelect<T[]> prepareMultiSelect(Class<T> type, Selector selector) {
		return this.getOrCreateTable(type).prepareMultiSelect((SelectorImpl) selector);
	}

	@Override
	public <T> PreparedObjectDelete<T> prepareObjectDelete(Class<T> type) {
		return this.getOrCreateTable(type).prepareObjectDelete();
	}

	@Override
	public <T> PreparedDelete prepareDelete(Class<T> type, Selector selector) {
		return this.getOrCreateTable(type).prepareDelete((SelectorImpl) selector);
	}

	@Override
	public <T> Table<T> getTable(Class<T> type) {
		return new Table<T>() {

			private TableImpl<T> table = SqlProviderImpl.this.getOrCreateTable(type);

			@Override
			public DatabaseAction<Void> createIfNotExists() {
				return SqlProviderImpl.this.createDatabaseAction(this.table::createIfNotExists);
			}

			@Override
			public DatabaseAction<Void> dropIfExists() {
				return SqlProviderImpl.this.createDatabaseAction(this.table::dropIfExists);
			}

			@Override
			public PreparedReplace<T, Void> prepareReplace() {
				return this.table.prepareReplace();
			}

			@Override
			public PreparedReplace<T, Long> prepareAiReplace() {
				return this.table.prepareAiReplace();
			}

			@Override
			public PreparedSelect<T> preparePrimarySelect() {
				return this.table.preparePrimarySelect();
			}

			@Override
			public PreparedSelect<T> prepareSelect(Selector selector) {
				return this.table.prepareSingleSelect((SelectorImpl) selector);
			}

			@Override
			public PreparedSelect<T[]> prepareMultiSelect(Selector selector) {
				return this.table.prepareMultiSelect((SelectorImpl) selector);
			}

			@Override
			public PreparedObjectDelete<T> prepareObjectDelete() {
				return this.table.prepareObjectDelete();
			}

			@Override
			public PreparedDelete prepareDelete(Selector selector) {
				return this.table.prepareDelete((SelectorImpl) selector);
			}
		};
	}

	protected <T> DatabaseAction<T> createDatabaseAction(ThrowableSupplier<T> action) {
		return new DatabaseActionImpl<>(action);
	}

	protected DatabaseAction<Void> createDatabaseAction(ThrowableRunnable action) {
		return this.createDatabaseAction(() -> {
			action.run();
			return null;
		});
	}

	public <T> T runDatabaseAction(DatabaseAction<T> action) throws Throwable {
		return ((DatabaseActionImpl<T>) action).action.run();
	}

	public CachedStatement prepareStatement(String sql) {
		this.logger.debug("Preparing statement %s", sql);
		return new CachedStatement(sql, (int) sql.chars().filter(c -> c == '?').count());
	}

	public int executeUpdate(CachedStatement cachedStatement) throws Throwable {
		this.assertExecutorThread();
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this, this.getValidConnection());
		this.logger.debug("Executing prepared update statement %s", statement.toString());
		return statement.executeUpdate();
	}

	public ResultSet executeQuery(CachedStatement cachedStatement) throws Throwable {
		this.assertExecutorThread();
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this, this.getValidConnection());
		this.logger.debug("Executing prepared query statement %s", statement.toString());
		return statement.executeQuery();
	}

	public int executeUpdate(String sql) throws SQLException {
		this.assertExecutorThread();
		this.logger.debug("Executing update statement %s", sql);
		return this.getValidConnection().createStatement().executeUpdate(sql);
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		this.assertExecutorThread();
		this.logger.debug("Executing query statement %s", sql);
		return this.getValidConnection().createStatement().executeQuery(sql);
	}

	private void assertExecutorThread() {
		if (Thread.currentThread() != this.thread)
			throw new Error();
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

	class DatabaseActionImpl<T> implements DatabaseAction<T> {

		private ThrowableSupplier<T> action;

		public DatabaseActionImpl(ThrowableSupplier<T> action) {
			this.action = action;
		}

		@Override
		public Future<T> queue(ExecutorService callbackExecutor, Consumer<T> success, Consumer<Throwable> error) {
			return SqlProviderImpl.this.executor.submit(() -> {
				try {
					T result = this.action.run();
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
	}
}
