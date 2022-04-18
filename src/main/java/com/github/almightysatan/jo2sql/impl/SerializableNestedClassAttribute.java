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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.SqlSerializable;

public class SerializableNestedClassAttribute implements SerializableAttribute {

	private final SqlProviderImpl provider;
	private final Class<SqlSerializable> type;
	private ColumnData[] columnData;
	private TableImpl<SqlSerializable> table;
	private PreparedReplace<SqlSerializable, Void> replace;
	private PreparedSelect<SqlSerializable> primarySelect;
	private String columnName;

	public SerializableNestedClassAttribute(SqlProviderImpl provider, Class<SqlSerializable> type, String columnName)
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

		ColumnData[] dataArray = new ColumnData[this.table.getType().getPrimaryKey().getColumnData().length];
		int i = 0;
		for (ColumnData data : this.table.getType().getPrimaryKey().getColumnData()) {
			dataArray[i++] = new ColumnData(this.getColumnName() + INTERNAL_COLUMN_DELIMITER + data.getName(),
					data.getSqlType(), data.getReplaceSql());
		}
		this.columnData = dataArray;
	}

	@Override
	public void appendIndex(StringBuilder builder, String delimiter) {
		// TODO Auto-generated method stub
	}

	@Override
	public void serialize(PreparedStatement statement, int startIndex, Object value) throws Throwable {
		for (SerializableAttribute field : this.table.getType().getPrimaryKey().getIndexFields())
			field.serialize(statement, startIndex++,
					value == null ? null : ((AnnotatedField) field).getField().get(value));
		if (value != null)
			this.provider.runDatabaseAction(this.replace.object((SqlSerializable) value));
	}

	@Override
	public Object deserialize(String prefix, ResultSet result) throws Throwable {
		Object[] values = new Object[this.table.getType().getPrimaryKey().getIndexFields().length];
		int i = 0;
		for (SerializableAttribute field : this.table.getType().getPrimaryKey().getIndexFields())
			values[i++] = field.deserialize(prefix + this.getColumnName() + INTERNAL_COLUMN_DELIMITER, result);

		return this.provider.runDatabaseAction(this.primarySelect.values(values));
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
