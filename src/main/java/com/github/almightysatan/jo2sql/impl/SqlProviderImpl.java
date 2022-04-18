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
	private final Logger logger;
	private final Map<Class<?>, Table<?>> tables = new HashMap<>();
	private Connection connection;
	private CachedStatement selectLastInsertIdStatement;

	public SqlProviderImpl(Logger logger, List<DataType> types) {
		this.logger = logger;
		types.addAll(UNIVERSAL_DATA_TYPES);
		types.add(this.getStringType());
		this.types = types;
		this.selectLastInsertIdStatement = this.prepareStatement("SELECT " + this.getLastInsertIdFunc() + "();");
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

	protected abstract String getLastInsertIdFunc();

	@SuppressWarnings("unchecked")
	public synchronized <T extends SqlSerializable> Table<T> getTable(Class<T> type) {
		if (this.tables.containsKey(type))
			return (Table<T>) this.tables.get(type);
		else {
			try {
				Table<T> table = this.newTable(new SerializableClass<>(this, type));

				Optional<Table<?>> duplicate = this.tables.values().stream()
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

	public abstract <T> Table<T> newTable(SerializableObject<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	public DataType getDataType(Class<?> type) {
		throw new UnsupportedOperationException();
	}

	public SerializableAttribute createSerializableAttribute(Class<?> clazz, String tableName, String columnName,
			int size) throws Throwable {
		for (DataType type : this.types)
			if (type.isOfType(clazz))
				return new SimpleSerializableAttribute(type, tableName, columnName, size);

		throw new Error(String.format("Unsupported type of field %s in %s", columnName, size));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SerializableAttribute createSerializableAttribute(Field field, Column annotation,
			SerializableObject<?> parent) throws Throwable {
		Class<?> clazz = annotation.type() == void.class ? field.getType() : annotation.type();
		String columnName = annotation.value();
		int size = annotation.size();

		if (annotation.autoIncrement()) {
			DataType aiLongType = this.getAiLongType();
			if (aiLongType.isOfType(clazz))
				return new SimpleSerializableAttribute(aiLongType, parent.getName(), columnName, size);
			else
				throw new Error(
						String.format("Invalid auto increment type in class %s", field.getDeclaringClass().getName()));
		}

		if (SqlSerializable.class.isAssignableFrom(clazz))
			return new SerializableNestedClassAttribute(this, (Class<SqlSerializable>) clazz, columnName);

		MapColumn mapAnnotation = field.getAnnotation(MapColumn.class);
		if (mapAnnotation != null && Map.class.isAssignableFrom(field.getType()))
			return new AnnotatedField(this, field, annotation,
					new SerializableMapEntryAttribute(this, annotation.type(), mapAnnotation.keyType(),
							mapAnnotation.keySize(), mapAnnotation.valueType(), mapAnnotation.valueSize(),
							annotation.value(), parent));

		return this.createSerializableAttribute(clazz, parent.getName(), columnName, size);
	}

	public AnnotatedField createAnnotatedField(Field field, Column annotation, SerializableObject<?> parent)
			throws Throwable {
		return new AnnotatedField(this, field, annotation, this.createSerializableAttribute(field, annotation, parent));
	}

	@Override
	public <T extends SqlSerializable> DatabaseAction<Void> createIfNecessary(Class<T> type) {
		Table<T> table = this.getTable(type);
		return this.createDatabaseAction(() -> {
			table.createIfNecessary();
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
	public <T extends SqlSerializable> PreparedSelect<T> prepareSelect(Class<T> type, Selector selector) {
		return this.getTable(type).prepareSingleSelect(selector);
	}

	@Override
	public <T extends SqlSerializable> PreparedSelect<T[]> prepareMultiSelect(Class<T> type, Selector selector) {
		return this.getTable(type).prepareMultiSelect(selector);
	}

	@Override
	public <T extends SqlSerializable> PreparedObjectDelete<T> prepareObjectDelete(Class<T> type) {
		return this.getTable(type).prepareObjectDelete();
	}

	@Override
	public <T extends SqlSerializable> PreparedDelete prepareDelete(Class<T> type, Selector selector) {
		return this.getTable(type).prepareDelete(selector);
	}

	protected <T> DatabaseAction<T> createDatabaseAction(ThrowableSupplier<T> action) {
		return new DatabaseActionImpl<>(action);
	}

	public <T> T runDatabaseAction(DatabaseAction<T> action) throws Throwable {
		return ((DatabaseActionImpl<T>) action).action.run();
	}

	public CachedStatement prepareStatement(String sql) {
		this.logger.debug("Preparing statement %s", sql);
		return new CachedStatement(sql, (int) sql.chars().filter(c -> c == '?').count());
	}

	public int executeUpdate(CachedStatement cachedStatement) throws Throwable {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this, this.getValidConnection());
		this.logger.debug("Executing prepared update statement %s", statement.toString());
		return statement.executeUpdate();
	}

	public ResultSet executeQuery(CachedStatement cachedStatement) throws Throwable {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this, this.getValidConnection());
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

	CachedStatement getSelectLastInsertIdStatement() {
		return this.selectLastInsertIdStatement;
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
