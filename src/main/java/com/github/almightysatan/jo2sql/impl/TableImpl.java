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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.almightysatan.jo2sql.DatabaseAction;
import com.github.almightysatan.jo2sql.PreparedDelete;
import com.github.almightysatan.jo2sql.PreparedObjectDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;

public abstract class TableImpl<T> {

	protected final SqlProviderImpl provider;
	protected final SerializableObject<T> type;
	protected final String fullName;
	private boolean exists;

	protected TableImpl(SqlProviderImpl provider, SerializableObject<T> type) {
		this.provider = provider;
		this.type = type;
		this.fullName = this.getFullName(this.type.getName());
	}

	protected abstract String getFullName(String name);

	void createIfNotExists() throws Throwable {
		if (!this.exists) {
			this.check();
			this.exists = true;
		}
	}

	void dropIfExists() throws Throwable {
		this.provider.executeUpdate("DROP TABLE IF EXISTS " + this.getFullName() + ";");
		this.exists = false;
	}

	protected void check() throws Throwable {
		if (!this.provider.executeQuery(this.getTableSelectStatement()).next()) {
			// Table does not exist
			this.provider.getLogger().info("Creating table %s", this.getFullName());

			StringBuilder statement = new StringBuilder().append("CREATE TABLE ").append(this.fullName).append(" (");
			boolean first = true;
			for (SerializableAttribute attribute : this.getType().getAttributes()) {
				for (ColumnData column : attribute.getColumnData()) {
					if (first)
						first = false;
					else
						statement.append(",");
					statement.append("`").append(column.getName()).append("`").append(column.getSqlStatement());
				}
			}
			this.getType().getPrimaryKey().appendIndex(statement, ",");
			statement.append(");");

			this.provider.executeUpdate(statement.toString());
		} else {
			// Table exists
			ResultSet result = this.provider.executeQuery(this.getColumnSelectStatement());
			Map<String, ColumnData> columns = Arrays.stream(this.type.getAttributes())
					.flatMap(attribute -> Arrays.stream(attribute.getColumnData()))
					.collect(Collectors.toMap(ColumnData::getName, (v) -> v, (v1, v2) -> {
						throw new Error();
					}, HashMap::new));
			while (result.next())
				columns.remove(result.getString(this.getColumnNameLabel()));

			for (ColumnData column : columns.values()) {
				this.provider.getLogger().info("Adding column %s to table %s", column.getName(), this.getFullName());

				this.provider.executeUpdate("ALTER TABLE " + this.fullName + " ADD COLUMN " + column.getName() + " "
						+ column.getSqlStatement() + ";");
			}
		}

		Map<String, Index> indices = Arrays.stream(this.type.getAttributes())
				.flatMap(attribute -> Arrays.stream(attribute.getIndices()))
				.collect(Collectors.toMap(Index::getName, (v) -> v, (v1, v2) -> {
					throw new Error();
				}, HashMap::new));
		ResultSet result = this.provider.executeQuery(this.getIndicesSelectStatement());
		while (result.next()) {
			String indexName = result.getString(this.getIndexNameLabel());
			if (indices.remove(indexName) == null && !indexName.equals("PRIMARY") && !indexName.startsWith("sqlite_"))
				this.provider.executeUpdate(this.getIndexDropStatement(indexName));
		}
		for (Index index : indices.values()) {
			this.provider.getLogger().info("Adding index %s to table %s", index.getName(), this.getFullName());
			this.provider.executeUpdate(index.getSql(this.fullName));
		}
	}

	protected abstract CachedStatement getTableSelectStatement();

	protected abstract CachedStatement getColumnSelectStatement();

	protected abstract String getIndicesSelectStatement();

	protected abstract String getIndexDropStatement(String name);

	protected abstract String getColumnNameLabel();

	protected abstract String getIndexNameLabel();

	PreparedReplace<T, Void> prepareReplace() {
		String sql = this.buildReplaceSql();

		return new PreparedReplace<T, Void>() {

			private PreparedObjectDelete<T> preparedDelete;
			private CachedStatement statement;

			@Override
			public PreparedReplace<T, Void> overwriteNestedObjects(boolean overwrite) {
				if (overwrite)
					this.preparedDelete = TableImpl.this.prepareObjectDelete().overwriteNestedObjects();
				else
					this.preparedDelete = null;
				return this;
			}

			@Override
			public DatabaseAction<Void> object(T value) {
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNotExists();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);
					if (this.preparedDelete != null)
						TableImpl.this.provider.runDatabaseAction(this.preparedDelete.object(value));
					TableImpl.this.type.serialize(this.statement, 0, value,
							TableImpl.this.type.needsPrevValue() ? TableImpl.this.getPrevValues(value) : null);
					TableImpl.this.provider.executeUpdate(this.statement);
				});
			}
		};
	}

	PreparedReplace<T, Long> prepareAiReplace() {
		String sql = this.buildReplaceSql();

		return new PreparedReplace<T, Long>() {

			private PreparedObjectDelete<T> preparedDelete;
			private CachedStatement statement;

			@Override
			public PreparedReplace<T, Long> overwriteNestedObjects(boolean overwrite) {
				if (overwrite)
					this.preparedDelete = TableImpl.this.prepareObjectDelete().overwriteNestedObjects();
				else
					this.preparedDelete = null;
				return this;
			}

			@Override
			public DatabaseAction<Long> object(T value) {
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNotExists();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);
					if (this.preparedDelete != null)
						TableImpl.this.provider.runDatabaseAction(this.preparedDelete.object(value));
					TableImpl.this.type.serialize(this.statement, 0, value,
							TableImpl.this.type.needsPrevValue() ? TableImpl.this.getPrevValues(value) : null);
					TableImpl.this.provider.executeUpdate(this.statement);
					return TableImpl.this.provider.getLastInsertId(TableImpl.this.getFullName());
				});
			}
		};
	}

	private String buildReplaceSql() {
		StringBuilder replaceBuilder = new StringBuilder().append("REPLACE INTO ").append(this.fullName).append(" (`");
		boolean first = true;
		for (SerializableAttribute field : this.getType().getAttributes()) {
			for (ColumnData column : field.getColumnData()) {
				if (first)
					first = false;
				else
					replaceBuilder.append("`,`");
				replaceBuilder.append(column.getName());
			}
		}

		replaceBuilder.append("`) VALUES (");
		first = true;
		for (SerializableAttribute field : this.getType().getAttributes()) {
			for (ColumnData column : field.getColumnData()) {
				if (first)
					first = false;
				else
					replaceBuilder.append(",");
				replaceBuilder.append(column.getReplaceSql());
			}
		}
		return replaceBuilder.append(");").toString();
	}

	ResultSet getPrevValues(T value) throws Throwable {
		ResultSet prevValues;
		SerializableAttribute[] primaryAttributes = this.type.getPrimaryKey().getIndexFields();
		Object[] primaryValues = new Object[primaryAttributes.length];
		for (int i = 0; i < primaryAttributes.length; i++)
			primaryValues[i] = ((AnnotatedField) primaryAttributes[i]).getFieldValue(value);
		// TODO don't call prepareSelect every time this is executed
		prevValues = this.provider.runDatabaseAction(
				this.prepareSelect(this.type.getPrimaryKey().getSelector(), 0, 1, "*").values(primaryValues));
		return prevValues.next() ? prevValues : null;
	}

	PreparedSelect<T> preparePrimarySelect() {
		return this.prepareSingleSelect(this.type.getPrimaryKey().getSelector());
	}

	PreparedSelect<T> prepareSingleSelect(SelectorImpl selector) {
		return this.prepareSelect(result -> {
			try {
				if (result.next())
					return this.type.deserialize("", result);
				else
					return null;
			} catch (Throwable e) {
				throw new Error("Error while parsing result", e);
			}
		}, selector, 0, 1, "*");
	}

	@SuppressWarnings("unchecked")
	PreparedSelect<T[]> prepareMultiSelect(SelectorImpl selector, int offset, int limit) {
		return this.prepareSelect(result -> {
			try {
				List<T> list = new ArrayList<>();
				while (result.next())
					list.add(this.type.deserialize("", result));
				return list.toArray((T[]) Array.newInstance(this.type.getType(), list.size()));
			} catch (Throwable e) {
				throw new Error("Error while parsing result", e);
			}
		}, selector, offset, limit, "*");
	}

	<X> PreparedSelect<X> prepareSelect(Function<ResultSet, X> resultInterpreter, SelectorImpl selector, int offset,
			int limit, String selectedColumns) {
		PreparedSelect<ResultSet> preparedSelect = this.prepareSelect(selector, offset, limit, selectedColumns);

		return new PreparedSelect<X>() {

			@Override
			public DatabaseAction<X> values(Object... values) {
				DatabaseAction<ResultSet> action = preparedSelect.values(values);
				return TableImpl.this.provider.createDatabaseAction(() -> {
					return resultInterpreter.apply(TableImpl.this.provider.runDatabaseAction(action));
				});
			}
		};
	}

	PreparedSelect<ResultSet> prepareSelect(SelectorImpl selector, int offset, int limit, String selectedColumns) {
		SerializableAttribute[] attributes = this.type.getAttributes(selector.getKeys());
		String sql = new StringBuilder("SELECT ").append(selectedColumns).append(" FROM ").append(this.fullName)
				.append(" ").append("WHERE ").append(selector.getCommand()).append(" LIMIT ").append(offset)
				.append(", ").append(limit).append(";").toString();

		return new PreparedSelect<ResultSet>() {

			private CachedStatement statement;

			@Override
			public DatabaseAction<ResultSet> values(Object... values) {
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNotExists();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);

					for (int i = 0, size = 0; i < attributes.length; i++)
						size += attributes[i].serialize(this.statement, size, values[i], null);
					return TableImpl.this.provider.executeQuery(this.statement);
				});
			}
		};
	}

	PreparedSelect<Integer> count(SelectorImpl selector) {
		return this.prepareSelect(result -> {
			try {
				return result.next() ? (int) SqlProviderImpl.INT_TYPE.deserialize("count(*)", result) : 0;
			} catch (Throwable e) {
				throw new Error("Error while parsing result", e);
			}
		}, selector, 0, Integer.MAX_VALUE, "COUNT(*)");
	}

	PreparedObjectDelete<T> prepareObjectDelete() {
		SerializableAttribute[] attributes = this.type.getAttributes(this.type.getPrimaryKey().getSelector().getKeys());
		PreparedDelete preparedDelete = this.prepareDelete(this.type.getPrimaryKey().getSelector());
		return new PreparedObjectDelete<T>() {
			@Override
			public PreparedObjectDelete<T> overwriteNestedObjects(boolean overwrite) {
				preparedDelete.overwriteNestedObjects(overwrite);
				return this;
			}

			@Override
			public DatabaseAction<Void> object(T object) {
				return TableImpl.this.provider.createDatabaseAction(() -> {
					Object[] values = new Object[attributes.length];
					for (int i = 0; i < attributes.length; i++)
						values[i] = ((AnnotatedField) attributes[i]).getFieldValue(object);
					TableImpl.this.provider.runDatabaseAction(preparedDelete.values(values));
				});
			}
		};
	}

	PreparedDelete prepareDelete(SelectorImpl selector) {
		SerializableAttribute[] attributes = this.type.getAttributes(selector.getKeys());
		String sql = new StringBuilder("DELETE FROM ").append(this.fullName).append("WHERE ")
				.append(selector.getCommand()).append(";").toString();

		return new PreparedDelete() {

			private PreparedSelect<T[]> preparedMultiSelect;
			private CachedStatement statement;

			@Override
			public PreparedDelete overwriteNestedObjects(boolean overwrite) {
				this.preparedMultiSelect = overwrite ? TableImpl.this.prepareMultiSelect(selector, 0, Integer.MAX_VALUE)
						: null;
				return this;
			}

			@Override
			public DatabaseAction<Void> values(Object... values) {
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNotExists();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);

					if (this.preparedMultiSelect != null) {
						T[] objects = TableImpl.this.provider
								.runDatabaseAction(this.preparedMultiSelect.values(values));
						for (SerializableAttribute attribute : TableImpl.this.type.getAttributes())
							for (T object : objects)
								attribute.deleteNested(((AnnotatedField) attribute).getFieldValue(object));
					}

					for (int i = 0, size = 0; i < attributes.length; i++)
						size += attributes[i].serialize(this.statement, size, values[i], null);
					TableImpl.this.provider.executeUpdate(this.statement);
					return null;
				});
			}
		};
	}

	public PreparedDelete preparePrimaryDelete() {
		return this.prepareDelete(this.type.getPrimaryKey().getSelector());
	}

	public SerializableObject<T> getType() {
		return this.type;
	}

	public String getFullName() {
		return this.fullName;
	}
}
