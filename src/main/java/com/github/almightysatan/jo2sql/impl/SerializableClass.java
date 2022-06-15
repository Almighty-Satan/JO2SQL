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
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.annotations.Column;

public class SerializableClass<T> implements SerializableObject<T> {

	private final Class<T> type;
	private final Constructor<T> constructor;
	private final String name;
	private final Map<String, AnnotatedField> fieldMap = new HashMap<>();
	private final AnnotatedField[] fields;
	private final PrimaryKey primaryKey;
	private AnnotatedField aiField;
	private boolean shouldGetPrevValues;

	public SerializableClass(SqlProviderImpl provider, Class<T> type) throws Throwable {
		this.type = type;
		this.constructor = type.getDeclaredConstructor();
		this.constructor.setAccessible(true);

		SqlSerializable annotation = type.getAnnotation(SqlSerializable.class);
		if (annotation == null)
			throw new Error("Missing @SqlSerializable annotation in class " + type.getName());
		StringUtil.assertAlphanumeric(annotation.value());

		this.name = annotation.value();

		List<AnnotatedField> primaryFields = new ArrayList<>();
		this.loadFields(provider, primaryFields, type);

		if (this.fieldMap.size() == 0)
			throw new Error("No columns found in class: " + type.getName());

		this.fields = this.fieldMap.values().toArray(new AnnotatedField[this.fieldMap.size()]);
		this.primaryKey = new PrimaryKey(primaryFields.toArray(new AnnotatedField[primaryFields.size()]));
		if (this.primaryKey.getIndexFields().length == 0)
			throw new Error("Missing primary key in table " + this.getName());

		if (this.aiField != null && this.primaryKey.getIndexFields().length > 1)
			throw new Error(String.format("Multiple columns in Primary Key while using Auto Increment in class %s",
					this.type.getName()));

		for (SerializableAttribute attributes : this.fields)
			if (attributes.needsPrevValue()) {
				this.shouldGetPrevValues = true;
				break;
			}
	}

	private void loadFields(SqlProviderImpl provider, List<AnnotatedField> primaryFields, Class<?> clazz)
			throws Throwable {
		for (Field field : clazz.getDeclaredFields()) {
			Column annotation = field.getAnnotation(Column.class);
			if (annotation != null && !Modifier.isStatic(field.getModifiers())) {
				AnnotatedField annotatedField;
				try {
					annotatedField = provider.createAnnotatedField(field, annotation, this);
				} catch (Throwable t) {
					throw new Error(String.format("An error occured while trying to load field %s in class %s",
							field.getName(), field.getDeclaringClass().getName()), t);
				}
				if (annotation.primary() || annotation.autoIncrement())
					primaryFields.add(annotatedField);

				if (annotation.autoIncrement()) {
					if (this.aiField == null)
						this.aiField = annotatedField;
					else
						throw new Error(
								String.format("Multiple Auto Increment columns in class %s", this.type.getName()));
				}

				if (this.fieldMap.put(annotation.value(), annotatedField) != null)
					throw new Error(String.format("Duplicate column name: %s in class %s", annotation.value(),
							this.type.getName()));
			}
		}

		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
			this.loadFields(provider, primaryFields, clazz.getSuperclass());
	}

	@Override
	public int serialize(CachedStatement statement, int startIndex, T value, ResultSet prevValues) throws Throwable {
		for (AnnotatedField field : this.fields)
			startIndex += field.serialize(statement, startIndex, field.getFieldValue(value), prevValues);
		return startIndex;
	}

	@Override
	public T deserialize(String prefix, ResultSet result) throws Throwable {
		T instance = this.constructor.newInstance();
		for (AnnotatedField field : this.fieldMap.values())
			field.setFieldValue("", result, instance);
		return instance;
	}

	@Override
	public boolean needsPrevValue() {
		return this.shouldGetPrevValues;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Class<T> getType() {
		return this.type;
	}

	@Override
	public SerializableAttribute[] getAttributes() {
		return this.fields;
	}

	@Override
	public SerializableAttribute[] getAttributes(String... keys) {
		SerializableAttribute[] fields = new SerializableAttribute[keys.length];
		for (int i = 0; i < keys.length; i++)
			if ((fields[i] = this.fieldMap.get(keys[i])) == null)
				throw new Error(String.format("Unknown key '%s' for table %s", keys[i], this.name));
		return fields;
	}

	@Override
	public PrimaryKey getPrimaryKey() {
		return this.primaryKey;
	}
}
