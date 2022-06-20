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

import java.lang.reflect.Method;
import java.sql.ResultSet;

import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.impl.CachedStatement;

public class SerializableStringEnumAttribute extends SimpleSerializableAttribute {

	private static final int SIZE = 200;

	private final Method valueOfMethod;

	public SerializableStringEnumAttribute(DataType stringType, Class<?> enumType, String tableName, String columnName)
			throws NoSuchMethodException, SecurityException {
		super(stringType, tableName, columnName, SIZE);
		this.valueOfMethod = enumType.getDeclaredMethod("valueOf", String.class);
	}

	@Override
	public int serialize(CachedStatement statement, int startIndex, Object value, ResultSet prevValues)
			throws Throwable {
		return super.serialize(statement, startIndex, value != null ? value.toString() : null, prevValues);
	}

	@Override
	public Object deserialize(String prefix, ResultSet result) throws Throwable {
		String string = (String) super.deserialize(prefix, result);
		return string != null ? this.valueOfMethod.invoke(null, string) : null;
	}
}
