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
import java.util.List;
import java.util.function.Function;

import com.github.almightysatan.jo2sql.DatabaseAction;
import com.github.almightysatan.jo2sql.PreparedDelete;
import com.github.almightysatan.jo2sql.PreparedObjectDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.Selector;

public abstract class TableImpl<T> {

	protected final SqlProviderImpl provider;
	protected final SerializableObject<T> type;
	protected final String fullName;
	private boolean created;

	protected TableImpl(SqlProviderImpl provider, SerializableObject<T> type) {
		this.provider = provider;
		this.type = type;
		this.fullName = this.getFullName(this.type.getName());
	}

	protected abstract String getFullName(String name);

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
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNecessary();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);
					TableImpl.this.type.serialize(this.statement, value);
					TableImpl.this.provider.executeUpdate(this.statement);
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
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNecessary();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);
					TableImpl.this.type.serialize(this.statement, value);
					TableImpl.this.provider.executeUpdate(this.statement);
					ResultSet result = TableImpl.this.provider
							.executeQuery(TableImpl.this.provider.getSelectLastInsertIdStatement());
					result.next();
					return result.getLong(1);
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

	public PreparedSelect<T> preparePrimarySelect() {
		return this.prepareSingleSelect(this.type.getPrimaryKey().getSelector());
	}

	<X> PreparedSelect<X> preparePrimarySelect(Function<ResultSet, X> resultInterpreter) {
		return this.prepareSelect(resultInterpreter, this.type.getPrimaryKey().getSelector());
	}

	PreparedSelect<T> prepareSingleSelect(Selector selector) {
		return this.prepareSelect(result -> {
			try {
				if (result.next())
					return this.type.deserialize(result);
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
					list.add(this.type.deserialize(result));
				return list.toArray((T[]) Array.newInstance(this.type.getType(), list.size()));
			} catch (Throwable e) {
				throw new Error("Error while parsing result", e);
			}
		}, selector);
	}

	private <X> PreparedSelect<X> prepareSelect(Function<ResultSet, X> resultInterpreter, Selector selector) {
		SerializableAttribute[] fields = this.type.getAttributes(selector.getKeys());
		String sql = new StringBuilder("SELECT * FROM ").append(this.fullName).append(" ").append("WHERE ")
				.append(selector.getCommand()).append(";").toString();

		return new PreparedSelect<X>() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<X> values(Object... values) {
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNecessary();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);

					for (int i = 0; i < fields.length; i++)
						this.statement.setParameter(i, fields[i], values[i]);
					ResultSet result = TableImpl.this.provider.executeQuery(this.statement);
					return resultInterpreter.apply(result);
				});
			}
		};
	}

	PreparedObjectDelete<T> prepareObjectDelete() {
		SerializableAttribute[] attributes = TableImpl.this.type.getPrimaryKey().getIndexFields();
		String sql = new StringBuilder("DELETE FROM ").append(this.fullName).append(" ").append("WHERE ")
				.append(this.type.getPrimaryKey().getSelector().getCommand()).append(";").toString();

		return new PreparedObjectDelete<T>() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<Void> object(T object) {
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNecessary();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);

					for (int i = 0; i < attributes.length; i++)
						this.statement.setParameter(i, attributes[i],
								((AnnotatedField) attributes[i]).getFieldValue(object));
					TableImpl.this.provider.executeUpdate(this.statement);
					return null;
				});
			}
		};
	}

	PreparedDelete prepareDelete(Selector selector) {
		SerializableAttribute[] attributes = this.type.getAttributes(selector.getKeys());
		String sql = new StringBuilder("DELETE FROM ").append(this.fullName).append("WHERE ")
				.append(selector.getCommand()).append(";").toString();

		return new PreparedDelete() {
			private CachedStatement statement;

			@Override
			public DatabaseAction<Void> values(Object... values) {
				return TableImpl.this.provider.createDatabaseAction(() -> {
					TableImpl.this.createIfNecessary();

					if (this.statement == null)
						this.statement = TableImpl.this.provider.prepareStatement(sql);

					for (int i = 0; i < attributes.length; i++)
						this.statement.setParameter(i, attributes[i], values[i]);
					TableImpl.this.provider.executeUpdate(this.statement);
					return null;
				});
			}
		};
	}

	public SerializableObject<T> getType() {
		return this.type;
	}

	public String getFullName() {
		return this.fullName;
	}
}
