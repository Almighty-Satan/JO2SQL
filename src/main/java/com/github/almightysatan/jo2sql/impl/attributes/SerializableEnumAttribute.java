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

package com.github.almightysatan.jo2sql.impl.attributes;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.annotations.EnumId;
import com.github.almightysatan.jo2sql.impl.CachedStatement;
import com.github.almightysatan.jo2sql.impl.ColumnData;
import com.github.almightysatan.jo2sql.impl.Index;

public class SerializableEnumAttribute implements SerializableAttribute {

	private final SerializableAttribute attribute;

	public SerializableEnumAttribute(DataType stringType, Class<?> enumType, String tableName, String columnName)
			throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException {
		for (Field field : enumType.getDeclaredFields())
			if (field.isEnumConstant() && field.isAnnotationPresent(EnumId.class)) {
				this.attribute = new SerializableIdEnumAttribute(enumType, tableName, columnName);
				return;
			}
		this.attribute = new SerializableStringEnumAttribute(stringType, enumType, tableName, columnName);
	}

	@Override
	public int serialize(CachedStatement statement, int startIndex, Object value, ResultSet prevValues)
			throws Throwable {
		return this.attribute.serialize(statement, startIndex, value, prevValues);
	}

	@Override
	public Object deserialize(String prefix, ResultSet result) throws Throwable {
		return this.attribute.deserialize(prefix, result);
	}

	@Override
	public boolean needsPrevValue() {
		return this.attribute.needsPrevValue();
	}

	@Override
	public void deleteNested(Object value) throws Throwable {
		this.deleteNested(value);
	}

	@Override
	public String getColumnName() {
		return this.attribute.getColumnName();
	}

	@Override
	public ColumnData[] getColumnData() {
		return this.attribute.getColumnData();
	}

	@Override
	public Index[] getIndices() {
		return this.attribute.getIndices();
	}
}
