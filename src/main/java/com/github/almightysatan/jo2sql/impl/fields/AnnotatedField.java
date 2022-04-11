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

package com.github.almightysatan.jo2sql.impl.fields;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.github.almightysatan.jo2sql.Column;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;

public abstract class AnnotatedField {

	static final char INTERNAL_COLUMN_DELIMITER = '#';

	private SqlProviderImpl provider;
	private final Field field;
	private final Column columnAnnotation;
	private final ColumnData[] columnData;

	public AnnotatedField(SqlProviderImpl provider, Field field, Column annotation) throws Throwable {
		this.provider = provider;
		this.field = field;
		this.field.setAccessible(true);
		this.columnAnnotation = annotation;
		this.columnData = this.createColumnData();

		if (annotation.value().contains(String.valueOf(AnnotatedField.INTERNAL_COLUMN_DELIMITER)))
			throw new Error(String.format("Column name %s in %s contains an invalid char", annotation.value(),
					field.getDeclaringClass().getName()));
	}

	private ColumnData[] createColumnData() throws Throwable {
		ColumnData[] columns = this.loadColumns();
		for (ColumnData data : columns)
			data.sql = this.appendAnnotationProperties(new StringBuilder().append(data.sql)).toString();
		return columns;
	}

	/**
	 * Loads the {@link ColumnData} for this field
	 * 
	 * @return An array of {@link ColumnData} containing information about the
	 *         columns
	 * @throws Throwable Depending on the implementation this method may throw a
	 *                   number of different exceptions
	 */
	protected abstract ColumnData[] loadColumns() throws Throwable;

	private StringBuilder appendAnnotationProperties(StringBuilder stringBuilder) {
		if (this.columnAnnotation.notNull())
			stringBuilder.append(" NOT NULL");
		else
			stringBuilder.append(" NULL");

		if (this.columnAnnotation.autoIncrement())
			stringBuilder.append(" AUTO_INCREMENT");

		return stringBuilder;
	}

	public void appendIndex(StringBuilder builder, String delimiter) {
		if (this.columnAnnotation.unique())
			builder.append(delimiter).append("UNIQUE INDEX `").append(this.columnAnnotation.value())
					.append("_UNIQUE` (`").append(this.columnAnnotation.value()).append("` ASC) VISIBLE");
	}

	/**
	 * Sets the parameters of a {@link PreparedStatement} to the value of this
	 * field. This method may invoke database requests to save nested objects to the
	 * database.
	 * 
	 * @param statement  The {@link PreparedStatement}
	 * @param startIndex The index where the first parameter should be set
	 * @param value      The value that should be loaded into the
	 *                   {@link PreparedStatement}
	 * @throws Throwable Depending on the implementation this method may throw a
	 *                   number of different exceptions
	 */
	public abstract void setValues(PreparedStatement statement, int startIndex, Object value) throws Throwable;

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
	public void loadValue(String prefix, ResultSet result, Object instance) throws Throwable {
		this.field.set(instance, this.loadValue(prefix, result));
	}

	/**
	 * Creates an object that is created from the contents of the given
	 * {@link ResultSet}. This method may invoke further database requests to load
	 * nested objects.
	 * 
	 * @param prefix A prefix that is added to the column name when loading values
	 *               from the {@link ResultSet}. May be empty but should not be null
	 * @param result The {@link ResultSet}
	 * @throws Throwable Depending on the implementation this method may throw a
	 *                   number of different exceptions
	 */
	public abstract Object loadValue(String prefix, ResultSet result) throws Throwable;

	protected SqlProviderImpl getProvider() {
		return this.provider;
	}

	protected Field getField() {
		return this.field;
	}

	public Object getValue(Object instance) throws IllegalArgumentException, IllegalAccessException {
		return instance == null ? null : this.field.get(instance);
	}

	protected Column getColumnAnnotation() {
		return this.columnAnnotation;
	}

	public String getColumnName() {
		return this.columnAnnotation.value();
	}

	public ColumnData[] getColumnData() {
		return this.columnData;
	}
}
