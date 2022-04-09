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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.almightysatan.jo2sql.Column;
import com.github.almightysatan.jo2sql.SqlSerializable;

public class SerializableClass<T extends SqlSerializable> {

	private final Class<T> type;
	private final Constructor<T> constructor;
	private final String name;
	private final Map<String, AnnotatedField> fields = new HashMap<>();
	private final PrimaryKey primaryKey = new PrimaryKey();

	public SerializableClass(SqlProviderImpl provider, Class<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.type = type;
		this.constructor = type.getDeclaredConstructor();
		this.constructor.setAccessible(true);
		this.name = this.constructor.newInstance().getTableName();

		List<AnnotatedField> primaryFields = new ArrayList<>();
		for (Field field : type.getDeclaredFields()) {
			Column annotation = field.getAnnotation(Column.class);
			if (annotation != null) {
				if (this.fields.containsKey(annotation.value()))
					throw new Error(
							String.format("Duplicate column name: %s in class %s", annotation.value(), type.getName()));
				if (annotation.value().contains(String.valueOf(AnnotatedField.INTERNAL_COLUMN_DELIMITER)))
					throw new Error(String.format("Column name contains an invalid char: %s in class %s",
							annotation.value(), type.getName()));
				AnnotatedField annotatedField = new AnnotatedField(provider, field, annotation);
				if (annotation.primary())
					primaryFields.add(annotatedField);
				this.fields.put(annotation.value(), annotatedField);
			}
		}

		if (this.fields.size() == 0)
			throw new Error("No columns found in class: " + type.getName());

		if ((this.primaryKey.indexFields = primaryFields.toArray(new AnnotatedField[primaryFields.size()])).length == 0)
			throw new Error("Missing primary key in table " + this.getName());
	}

	AnnotatedField[] getFieldsByKey(String... keys) {
		AnnotatedField[] fields = new AnnotatedField[keys.length];
		for (int i = 0; i < keys.length; i++)
			if ((fields[i] = this.fields.get(keys[i])) == null)
				throw new Error(String.format("Unknown key '%s' for table %s", keys[i], this.name));
		return fields;
	}

	public T newInstance()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return this.constructor.newInstance();
	}

	public Class<T> getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public Map<String, AnnotatedField> getFields() {
		return this.fields;
	}

	public PrimaryKey getPrimaryKey() {
		return this.primaryKey;
	}
}
