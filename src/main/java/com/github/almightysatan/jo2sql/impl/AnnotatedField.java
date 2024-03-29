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

import java.lang.reflect.Field;
import java.sql.ResultSet;

import com.github.almightysatan.jo2sql.annotations.Column;
import com.github.almightysatan.jo2sql.impl.attributes.SerializableAttribute;

public class AnnotatedField implements SerializableAttribute {

	private final SqlProviderImpl provider;
	private final Field field;
	private final Column columnAnnotation;
	private final Class<?> type;
	private final SerializableAttribute attribute;
	private final ColumnData[] columnData;
	private final Index[] indices;

	public AnnotatedField(SqlProviderImpl provider, Field field, Column annotation, SerializableAttribute attribute)
			throws Throwable {
		this.provider = provider;
		this.field = field;
		this.field.setAccessible(true);
		this.columnAnnotation = annotation;
		this.attribute = attribute;

		StringUtil.assertAlphanumeric(annotation.value());

		if (annotation.type() == void.class)
			this.type = field.getType();
		else {
			if (field.getType().isAssignableFrom(annotation.type()))
				this.type = annotation.type();
			else
				throw new Error(String.format("%s is not a superclass of %s", field.getType(), annotation.type()));
		}

		if (annotation.value().contains(String.valueOf(AnnotatedField.INTERNAL_COLUMN_DELIMITER)))
			throw new Error(String.format("Column name %s in %s contains an invalid char", annotation.value(),
					field.getDeclaringClass().getName()));

		this.columnData = this.attribute.getColumnData();
		for (ColumnData data : this.columnData) {
			this.processColumnData(data);
			data.setSqlStatement(
					this.appendAnnotationProperties(new StringBuilder().append(data.getSqlType())).toString());
		}

		if (annotation.unique())
			this.indices = new Index[] { new UniqueIndex(annotation.value()) };
		else
			this.indices = new Index[0];
	}

	protected void processColumnData(ColumnData data) {
	}

	protected StringBuilder appendAnnotationProperties(StringBuilder stringBuilder) {
		if (this.columnAnnotation.notNull())
			stringBuilder.append(" NOT NULL");
		else
			stringBuilder.append(" NULL");

		return stringBuilder;
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
	public void deleteNested(Object value) throws Throwable {
		this.attribute.deleteNested(value);
	}

	/**
	 * Sets the value of this field to an object that is created from the contents
	 * of the given {@link ResultSet}. This method may invoke further database
	 * requests to load nested objects.
	 * 
	 * @param prefix   A prefix that is added to the column name when loading values
	 *                 from the {@link ResultSet}. May be empty but should not be
	 *                 null
	 * @param result   The {@link ResultSet}
	 * @param instance An instance of the class that declares this field
	 * @throws Throwable Depending on the implementation this method may throw a
	 *                   number of different exceptions
	 */
	public void setFieldValue(String prefix, ResultSet result, Object instance) throws Throwable {
		this.field.set(instance, this.deserialize(prefix, result));
	}

	public Object getFieldValue(Object instance) throws IllegalArgumentException, IllegalAccessException {
		return instance == null ? null : this.field.get(instance);
	}

	protected SqlProviderImpl getProvider() {
		return this.provider;
	}

	public Field getField() {
		return this.field;
	}

	public Class<?> getType() {
		return this.type;
	}

	protected Column getColumnAnnotation() {
		return this.columnAnnotation;
	}

	@Override
	public String getColumnName() {
		return this.columnAnnotation.value();
	}

	@Override
	public ColumnData[] getColumnData() {
		return this.columnData;
	}

	@Override
	public Index[] getIndices() {
		return this.indices;
	}

	@Override
	public boolean needsPrevValue() {
		return this.attribute.needsPrevValue();
	}
}
