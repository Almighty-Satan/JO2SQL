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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.almightysatan.jo2sql.DatabaseAction;
import com.github.almightysatan.jo2sql.PreparedDelete;
import com.github.almightysatan.jo2sql.PreparedObjectDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.Selector;
import com.github.almightysatan.jo2sql.SqlSerializable;

public abstract class Table<T extends SqlSerializable> {

	protected final SqlProviderImpl provider;
	protected final SerializableClass<T> type;
	protected final String fullName;
	private boolean created;

	protected Table(SqlProviderImpl provider, SerializableClass<T> type) {
		this.provider = provider;
		this.type = type;
		this.fullName = this.getFullName(this.type.getName());
	}

	protected String getFullName(String name) {
		return name;
	}

	void createIfNecessary() throws Throwable {
		if (!this.created) {
			this.check();
			this.created = true;
		}
	}

	protected abstract void check() throws Throwable;

	public PreparedReplace<T, Void> prepareReplace() {
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
							.executeQuery(Table.this.provider.getSelectLastInsertIdStatement());
					result.next();
					return result.getLong(1);
				});
			}
		};
	}

	private String buildReplaceSql() {
		StringBuilder replaceBuilder = new StringBuilder().append("REPLACE INTO ").append(this.fullName).append(" (`");
		int columns = 0;
		boolean first = true;
		for (AnnotatedField field : this.getType().getFields().values()) {
			for (ColumnData column : field.getColumnData()) {
				if (first)
					first = false;
				else
					replaceBuilder.append("`,`");
				replaceBuilder.append(column.getProcessedName());
				columns++;
			}
		}

		replaceBuilder.append("`) VALUES (");
		for (int i = 0; i < columns; i++) {
			if (i != 0)
				replaceBuilder.append(",");
			replaceBuilder.append("?");
		}
		return replaceBuilder.append(");").toString();
	}

	private void loadValues(CachedStatement statement, T object)
			throws IllegalArgumentException, IllegalAccessException, SQLException {
		int i = 0;
		for (AnnotatedField field : this.getType().getFields().values())
			statement.setParameter(i++, field.getType(), field.getField().get(object));
	}

	public PreparedSelect<T> preparePrimarySelect() {
		return this.prepareSingleSelect(this.type.getPrimaryKey().getSelector());
	}

	PreparedSelect<T> prepareSingleSelect(Selector selector) {
		return this.prepareSelect(result -> {
			try {
				if (result.next())
					return this.createObject(result);
				else
					return null;
			} catch (Throwable e) {
				throw new Error("Error while parsing result", e);
			}
		}, selector);
	}

	@SuppressWarnings("unchecked")
	PreparedSelect<T[]> prepareMultiSelect(Selector selector) {
		return this.prepareSelect(result -> {
			try {
				List<T> list = new ArrayList<>();
				while (result.next())
					list.add(this.createObject(result));
				return list.toArray((T[]) Array.newInstance(this.type.getType(), list.size()));
			} catch (Throwable e) {
				throw new Error("Error while parsing result", e);
			}
		}, selector);
	}

	private <X> PreparedSelect<X> prepareSelect(Function<ResultSet, X> resultInterpreter, Selector selector) {
		AnnotatedField[] fields = this.type.getFieldsByKey(selector.getKeys());
		String sql = new StringBuilder("SELECT * FROM ").append(this.fullName).append(" ").append("WHERE ")
				.append(selector.getCommand()).append(";").toString();

		return new PreparedSelect<X>() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<X> values(Object... values) {
				return Table.this.provider.createDatabaseAction(() -> {
					Table.this.createIfNecessary();

					if (this.statement == null)
						this.statement = Table.this.provider.prepareStatement(sql);

					for (int i = 0; i < fields.length; i++)
						this.statement.setParameter(i, fields[i].getType(), values[i]);
					ResultSet result = Table.this.provider.executeQuery(this.statement);
					return resultInterpreter.apply(result);
				});
			}
		};
	}

	private T createObject(ResultSet result) throws Throwable {
		T value = this.type.newInstance();
		for (AnnotatedField field : this.getType().getFields().values())
			field.getField().set(value,
					field.getType().getValue(this.provider, field.getField().getType(), result, field.getName()));
		return value;
	}

	PreparedObjectDelete<T> prepareObjectDelete() {
		AnnotatedField[] fields = new AnnotatedField[this.type.getPrimaryKey().indexFields.length];
		String sql = new StringBuilder("DELETE FROM ").append(this.fullName).append(" ").append("WHERE ")
				.append(this.type.getPrimaryKey().getSelector()).append(";").toString();

		return new PreparedObjectDelete<T>() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<Void> object(T object) {
				return Table.this.provider.createDatabaseAction(() -> {
					Table.this.createIfNecessary();

					if (this.statement == null)
						this.statement = Table.this.provider.prepareStatement(sql);

					for (int i = 0; i < fields.length; i++)
						this.statement.setParameter(i, fields[i].getType(), fields[i].getField().get(object));
					Table.this.provider.executeUpdate(this.statement);
					return null;
				});
			}
		};
	}

	PreparedDelete prepareDelete(Selector selector) {
		AnnotatedField[] fields = this.type.getFieldsByKey(selector.getKeys());
		String sql = new StringBuilder("DELETE FROM ").append(this.fullName).append("WHERE ")
				.append(selector.getCommand()).append(";").toString();

		return new PreparedDelete() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<Void> values(Object... values) {
				return Table.this.provider.createDatabaseAction(() -> {
					Table.this.createIfNecessary();

					if (this.statement == null)
						this.statement = Table.this.provider.prepareStatement(sql);

					for (int i = 0; i < fields.length; i++)
						this.statement.setParameter(i, fields[i].getType(), values[i]);
					Table.this.provider.executeUpdate(this.statement);
					return null;
				});
			}
		};
	}

	public SerializableClass<T> getType() {
		return this.type;
	}

	public String getFullName() {
		return this.fullName;
	}
}
