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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.almightysatan.jo2sql.Column;
import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.DatabaseAction;
import com.github.almightysatan.jo2sql.PreparedDelete;
import com.github.almightysatan.jo2sql.PreparedObjectDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.SqlSerializable;

public abstract class Table<T extends SqlSerializable> {

	protected final SqlProviderImpl provider;
	private final Class<T> type;
	private final Constructor<T> constructor;
	protected final String name;
	protected final String fullName;
	protected final Map<String, FieldColumn> columns = new LinkedHashMap<>();
	protected final AbstractIndex primaryKey = new PrimaryKey();
	private boolean created;

	protected Table(SqlProviderImpl provider, Class<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.provider = provider;
		this.type = type;
		this.constructor = type.getDeclaredConstructor();
		this.constructor.setAccessible(true);
		this.name = this.constructor.newInstance().getTableName();
		this.fullName = this.getFullName(this.name);

		for (Field field : type.getDeclaredFields()) {
			Column annotation = field.getAnnotation(Column.class);
			if (annotation != null) {
				if (this.columns.containsKey(annotation.value()))
					throw new Error(
							String.format("Duplicate column name: %s in class %s", annotation.value(), type.getName()));
				FieldColumn column = new FieldColumn(field, provider.getDataType(field.getType()), annotation);
				if (annotation.primary())
					this.primaryKey.indexColumns.add(column);
				this.columns.put(annotation.value(), column);
			}
		}

		if (this.columns.size() == 0)
			throw new Error("No columns found in class: " + type.getName());
	}

	protected String getFullName(String name) {
		return name;
	}

	protected abstract String getLastInsertIdFunc();

	void createIfNecessary() throws SQLException {
		if (!this.created) {
			this.check();
			this.created = true;
		}
	}

	protected abstract void check() throws SQLException;

	PreparedReplace<T, Void> prepareReplace() {
		String sql = this.buildReplaceSql();

		return new PreparedReplace<T, Void>() {

			private CachedStatement statement;

			@Override
			public DatabaseAction<Void> object(T value) {
				return Table.this.provider.createDatabaseAction(() -> {
					Table.this.createIfNecessary();

					if (this.statement == null)
						this.statement = Table.this.provider.prepareStatement(sql);
					Table.this.loadValues(this.statement, value);
					Table.this.provider.executeUpdate(this.statement);
					return null;
				});
			}
		};
	}

	PreparedReplace<T, Long> prepareAiReplace() {
		String sql = this.buildReplaceSql();

		return new PreparedReplace<T, Long>() {

			private CachedStatement statement;

			@Override
			public DatabaseAction<Long> object(T value) {
				return Table.this.provider.createDatabaseAction(() -> {
					Table.this.createIfNecessary();

					if (this.statement == null)
						this.statement = Table.this.provider.prepareStatement(sql);
					Table.this.loadValues(this.statement, value);
					Table.this.provider.executeUpdate(this.statement);
					ResultSet result = Table.this.provider
							.executeQuery("SELECT " + Table.this.getLastInsertIdFunc() + "();");
					result.next();
					return result.getLong(1);
				});
			}
		};
	}

	private String buildReplaceSql() {
		StringBuilder replaceBuilder = new StringBuilder().append("REPLACE INTO ").append(this.fullName).append(" (`");
		boolean first = true;
		for (FieldColumn column : this.columns.values()) {
			if (first)
				first = false;
			else
				replaceBuilder.append("`,`");
			replaceBuilder.append(column.getName());
		}

		replaceBuilder.append("`) VALUES (");
		for (int i = 0; i < this.columns.size(); i++) {
			if (i != 0)
				replaceBuilder.append(",");
			replaceBuilder.append("?");
		}
		return replaceBuilder.append(");").toString();
	}

	private void loadValues(CachedStatement statement, T object)
			throws IllegalArgumentException, IllegalAccessException, SQLException {
		int i = 0;
		for (FieldColumn column : this.columns.values())
			statement.setParameter(i++, column.getType(), column.getField().get(object));
	}

	PreparedSelect<T> prepareSingleSelect(String... keys) {
		return this.prepareSelect(result -> {
			try {
				if (result.next())
					return this.createObject(result);
				else
					return null;
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException
					| InvocationTargetException | SQLException e) {
				throw new Error("Error while parsing result", e);
			}
		}, keys);
	}

	@SuppressWarnings("unchecked")
	PreparedSelect<T[]> prepareMultiSelect(String... keys) {
		return this.prepareSelect(result -> {
			try {
				List<T> list = new ArrayList<>();
				while (result.next())
					list.add(this.createObject(result));
				return list.toArray((T[]) Array.newInstance(this.type, list.size()));
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException
					| InvocationTargetException | SQLException e) {
				throw new Error("Error while parsing result", e);
			}
		}, keys);
	}

	private <X> PreparedSelect<X> prepareSelect(Function<ResultSet, X> resultInterpreter, String... keys) {
		FieldColumn[] columns = new FieldColumn[keys.length];
		StringBuilder builder = new StringBuilder("SELECT * FROM ").append(this.fullName).append(" ");
		if (keys.length > 0) {
			builder.append("WHERE");
			int i = 0;
			for (String key : keys)
				if (this.columns.containsKey(key)) {
					if (i != 0)
						builder.append(" AND ");
					builder.append("`").append(key).append("`=?");
					columns[i++] = this.columns.get(key);
				} else
					throw new Error(String.format("Unknown key %s for table %s", key, this.name));
		}
		String sql = builder.append(";").toString();

		return new PreparedSelect<X>() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<X> values(Object... values) {
				return Table.this.provider.createDatabaseAction(() -> {
					Table.this.createIfNecessary();

					if (this.statement == null)
						this.statement = Table.this.provider.prepareStatement(sql);

					for (int i = 0; i < columns.length; i++)
						this.statement.setParameter(i, columns[i].getType(), values[i]);
					ResultSet result = Table.this.provider.executeQuery(this.statement);
					return resultInterpreter.apply(result);
				});
			}
		};
	}

	private T createObject(ResultSet result) throws IllegalArgumentException, IllegalAccessException, SQLException,
			InstantiationException, InvocationTargetException {
		T value = this.constructor.newInstance();
		for (FieldColumn column : this.columns.values())
			column.getField().set(value, column.getType().getValue(result, column.getName()));
		return value;
	}

	PreparedObjectDelete<T> prepareObjectDelete() {
		if (this.primaryKey.indexColumns.size() == 0)
			throw new Error("Missing primary key in table " + this.getName());

		FieldColumn[] columns = new FieldColumn[this.primaryKey.indexColumns.size()];
		StringBuilder builder = new StringBuilder("DELETE FROM ").append(this.fullName);

		builder.append("WHERE");
		int i = 0;
		for (FieldColumn column : this.primaryKey.indexColumns) {
			String key = column.getName();
			if (this.columns.containsKey(key)) {
				if (i != 0)
					builder.append(" AND ");
				builder.append("`").append(key).append("`=?");
				columns[i++] = this.columns.get(key);
			} else
				throw new Error(String.format("Unknown key %s for table %s", key, this.name));
		}
		String sql = builder.append(";").toString();

		return new PreparedObjectDelete<T>() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<Void> object(T object) {
				return Table.this.provider.createDatabaseAction(() -> {
					Table.this.createIfNecessary();

					if (this.statement == null)
						this.statement = Table.this.provider.prepareStatement(sql);

					for (int i = 0; i < columns.length; i++)
						this.statement.setParameter(i, columns[i].getType(), columns[i].getField().get(object));
					Table.this.provider.executeUpdate(this.statement);
					return null;
				});
			}
		};
	}

	PreparedDelete prepareDelete(String... keys) {
		FieldColumn[] columns = new FieldColumn[keys.length];
		StringBuilder builder = new StringBuilder("DELETE FROM ").append(this.fullName);
		if (keys.length > 0) {
			builder.append("WHERE");
			int i = 0;
			for (String key : keys)
				if (this.columns.containsKey(key)) {
					if (i != 0)
						builder.append(" AND ");
					builder.append("`").append(key).append("`=?");
					columns[i++] = this.columns.get(key);
				} else
					throw new Error(String.format("Unknown key %s for table %s", key, this.name));
		}
		String sql = builder.append(";").toString();

		return new PreparedDelete() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<Void> values(Object... values) {
				return Table.this.provider.createDatabaseAction(() -> {
					Table.this.createIfNecessary();

					if (this.statement == null)
						this.statement = Table.this.provider.prepareStatement(sql);

					for (int i = 0; i < columns.length; i++)
						this.statement.setParameter(i, columns[i].getType(), values[i]);
					Table.this.provider.executeUpdate(this.statement);
					return null;
				});
			}
		};
	}

	Class<T> getType() {
		return this.type;
	}

	String getName() {
		return this.name;
	}

	public abstract class AbstractIndex {

		public List<FieldColumn> indexColumns = new ArrayList<>();

		public abstract void appendIndex(StringBuilder builder, String delimiter);
	}

	private class Index extends AbstractIndex {

		@Override
		public void appendIndex(StringBuilder builder, String delimiter) {
			// TODO Auto-generated method stub
		}
	}

	private class PrimaryKey extends AbstractIndex {

		@Override
		public void appendIndex(StringBuilder builder, String delimiter) {
			builder.append(delimiter).append("PRIMARY KEY (`")
					.append(this.indexColumns.stream().map(FieldColumn::getName).collect(Collectors.joining("`,`")))
					.append("`)");
		}
	}

	public static class FieldColumn {

		private Field field;
		private DataType type;
		private Column annotation;

		private FieldColumn(Field field, DataType type, Column annotation) {
			this.field = field;
			this.annotation = annotation;

			this.field.setAccessible(true);

			this.type = type;
		}

		public void appendColumn(StringBuilder builder) {
			builder.append("`").append(this.annotation.value()).append("` ")
					.append(this.type.getDatatype(this.annotation.size()));

			if (this.annotation.notNull())
				builder.append(" NOT NULL");
			else
				builder.append(" NULL");

			if (this.annotation.autoIncrement())
				builder.append(" AUTO_INCREMENT");
		}

		public void appendIndex(StringBuilder builder, String delimiter) {
			if (this.annotation.unique())
				builder.append(delimiter).append("UNIQUE INDEX `").append(this.annotation.value()).append("_UNIQUE` (`")
						.append(this.annotation.value()).append("` ASC) VISIBLE");

			if (this.annotation.index())
				builder.append(delimiter).append("INDEX `").append(this.annotation.value()).append("` (`")
						.append(this.annotation.value()).append("` ASC) VISIBLE");
		}

		public String getName() {
			return this.annotation.value();
		}

		private Field getField() {
			return this.field;
		}

		private DataType getType() {
			return this.type;
		}
	}
}
