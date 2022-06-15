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

import com.github.almightysatan.jo2sql.DataType;

public class SimpleSerializableAttribute implements SerializableAttribute {

	private final DataType type;
	private final String tableName, columnName;
	private final int size;

	public SimpleSerializableAttribute(DataType type, String tableName, String columnName, int size) {
		this.type = type;
		this.tableName = tableName;
		this.columnName = columnName;
		this.size = size;
	}

	@Override
	public int serialize(CachedStatement statement, int startIndex, Object value, ResultSet prevValues)
			throws Throwable {
		statement.setParameter(startIndex, this.type, value);
		return 1; // This attribute always needs one column
	}

	@Override
	public Object deserialize(String prefix, ResultSet result) throws Throwable {
		return this.type.deserialize(prefix + this.columnName, result);
	}

	@Override
	public void deleteNested(Object value) throws Throwable {
		// Do nothing
	}

	@Override
	public String getColumnName() {
		return this.columnName;
	}

	@Override
	public ColumnData[] getColumnData() {
		return new ColumnData[] { new ColumnData(this.getColumnName(), this.type.getSqlType(this.size),
				this.type.getPreparedReplaceSql(this.columnName, this.tableName)) };
	}

	@Override
	public Index[] getIndices() {
		return new Index[0];
	}

	@Override
	public boolean needsPrevValue() {
		return false;
	}
}
