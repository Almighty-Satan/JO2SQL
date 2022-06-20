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
import java.util.HashMap;
import java.util.Map;

import com.github.almightysatan.jo2sql.annotations.EnumId;
import com.github.almightysatan.jo2sql.impl.CachedStatement;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;

public class SerializableIdEnumAttribute extends SimpleSerializableAttribute {

	private final Map<Integer, Object> idToEnum = new HashMap<>();
	private final Map<Object, Integer> enumToId = new HashMap<>();

	public SerializableIdEnumAttribute(Class<?> enumType, String tableName, String columnName)
			throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException {
		super(SqlProviderImpl.INT_TYPE, tableName, columnName, -1);

		for (Field field : enumType.getDeclaredFields()) {
			if (field.isEnumConstant()) {
				EnumId annotation = field.getAnnotation(EnumId.class);
				if (annotation == null)
					throw new Error("Missing EnumId annotation in class " + enumType);
				field.setAccessible(true);
				Object value = field.get(null);
				this.idToEnum.put(annotation.value(), value);
				this.enumToId.put(value, annotation.value());
			}
		}
	}

	@Override
	public int serialize(CachedStatement statement, int startIndex, Object value, ResultSet prevValues)
			throws Throwable {
		return super.serialize(statement, startIndex, value != null ? this.enumToId.get(value) : null, prevValues);
	}

	@Override
	public Object deserialize(String prefix, ResultSet result) throws Throwable {
		Integer id = (Integer) super.deserialize(prefix, result);
		return id != null ? this.idToEnum.get(id) : null;
	}
}
