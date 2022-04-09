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

import com.github.almightysatan.jo2sql.Column;
import com.github.almightysatan.jo2sql.impl.datatypes.DataType;

public class AnnotatedField {

	public static final char INTERNAL_COLUMN_DELIMITER = '#';

	private final Field field;
	private final DataType type;
	private final Column annotation;
	private final ColumnData[] columnData;

	AnnotatedField(SqlProviderImpl provider, Field field, Column annotation) {
		this.field = field;
		this.field.setAccessible(true);
		this.annotation = annotation;
		this.type = provider.getDataType(field.getType());
		this.columnData = this.createColumnData(provider);
	}

	private ColumnData[] createColumnData(SqlProviderImpl provider) {
		ColumnData[] columns = this.type.getColumnData(provider, this.field.getType(), this.annotation.size());
		for (ColumnData data : columns) {
			data.processedName = data.name == null ? this.annotation.value()
					: this.annotation.value() + INTERNAL_COLUMN_DELIMITER + data.name;

			StringBuilder builder = new StringBuilder();

			builder.append("`").append(data.processedName).append("` ").append(data.sqlType);

			if (this.annotation.notNull())
				builder.append(" NOT NULL");
			else
				builder.append(" NULL");

			if (this.annotation.autoIncrement())
				builder.append(" AUTO_INCREMENT");

			data.sqlStatement = builder.toString();
		}
		return columns;
	}

	public String getName() {
		return this.annotation.value();
	}

	public Field getField() {
		return this.field;
	}

	public DataType getType() {
		return this.type;
	}

	public ColumnData[] getColumnData() {
		return this.columnData;
	}

	public void appendIndex(StringBuilder builder, String delimiter) {
		if (this.annotation.unique())
			builder.append(delimiter).append("UNIQUE INDEX `").append(this.annotation.value()).append("_UNIQUE` (`")
					.append(this.annotation.value()).append("` ASC) VISIBLE");

		if (this.annotation.index())
			builder.append(delimiter).append("INDEX `").append(this.annotation.value()).append("` (`")
					.append(this.annotation.value()).append("` ASC) VISIBLE");
	}
}
