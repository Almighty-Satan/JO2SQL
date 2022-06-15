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

import java.sql.ResultSet;

import com.github.almightysatan.jo2sql.PreparedObjectDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;

public class SerializableNestedClassAttribute<T> implements SerializableAttribute {

	private final SqlProviderImpl provider;
	private final Class<T> type;
	private ColumnData[] columnData;
	private TableImpl<T> table;
	private PreparedReplace<T, Void> replace;
	private PreparedSelect<T> primarySelect;
	private PreparedObjectDelete<T> delete;
	private String columnName;
	private boolean shouldGetPrevValues;

	public SerializableNestedClassAttribute(SqlProviderImpl provider, Class<T> type, String columnName)
			throws Throwable {
		this.provider = provider;
		this.type = type;
		this.columnName = columnName;
		this.loadColumns();
	}

	protected void loadColumns() throws Throwable {
		this.table = this.provider.getOrCreateTable(this.type);
		this.replace = this.table.prepareReplace();
		this.primarySelect = this.table.preparePrimarySelect();
		this.delete = this.table.prepareObjectDelete().overwriteNestedObjects();

		ColumnData[] dataArray = new ColumnData[this.table.getType().getPrimaryKey().getColumnData().length];
		int i = 0;
		for (ColumnData data : this.table.getType().getPrimaryKey().getColumnData()) {
			dataArray[i++] = new ColumnData(this.getColumnName() + INTERNAL_COLUMN_DELIMITER + data.getName(),
					data.getSqlType(), data.getReplaceSql());
		}
		this.columnData = dataArray;

		for (SerializableAttribute attributes : this.table.type.getAttributes())
			if (attributes.needsPrevValue()) {
				this.shouldGetPrevValues = true;
				break;
			}
	}

	@Override
	public void appendIndex(StringBuilder builder, String delimiter) {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("unchecked")
	@Override
	public int serialize(CachedStatement statement, int startIndex, Object value, ResultSet prevValues)
			throws Throwable {
		for (SerializableAttribute attribute : this.table.getType().getPrimaryKey().getIndexFields()) {
			startIndex += attribute.serialize(statement, startIndex,
					value != null ? ((AnnotatedField) attribute).getField().get(value) : null,
					value != null && this.needsPrevValue() ? this.table.getPrevValues((T) value) : null);
		}

		if (value != null)
			this.provider.runDatabaseAction(this.replace.object((T) value));
		return startIndex;
	}

	@Override
	public boolean needsPrevValue() {
		return this.shouldGetPrevValues;
	}

	@Override
	public Object deserialize(String prefix, ResultSet result) throws Throwable {
		Object[] values = new Object[this.table.getType().getPrimaryKey().getIndexFields().length];
		int i = 0;
		for (SerializableAttribute field : this.table.getType().getPrimaryKey().getIndexFields())
			values[i++] = field.deserialize(prefix + this.getColumnName() + INTERNAL_COLUMN_DELIMITER, result);

		return this.provider.runDatabaseAction(this.primarySelect.values(values));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteNested(Object value) throws Throwable {
		if (value != null)
			this.provider.runDatabaseAction(this.delete.object((T) value));
	}

	@Override
	public String getColumnName() {
		return this.columnName;
	}

	@Override
	public ColumnData[] getColumnData() {
		return this.columnData;
	}
}
