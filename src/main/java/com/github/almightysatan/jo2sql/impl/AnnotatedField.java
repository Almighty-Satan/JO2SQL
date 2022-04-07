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
import com.github.almightysatan.jo2sql.DataType;

public class AnnotatedField {

	private Field field;
	private DataType type;
	private Column annotation;

	AnnotatedField(Field field, DataType type, Column annotation) {
		this.field = field;
		this.annotation = annotation;

		this.field.setAccessible(true);

		this.type = type;
	}

	public void appendColumn(StringBuilder builder) {
		builder.append("`").append(this.annotation.value()).append("` ")
				.append(this.type.getDatatype(this.annotation.size()));

		if (this.annotation.notNull())
			builder.append(" NOT NULL");
		else
			builder.append(" NULL");

		if (this.annotation.autoIncrement())
			builder.append(" AUTO_INCREMENT");
	}

	public void appendIndex(StringBuilder builder, String delimiter) {
		if (this.annotation.unique())
			builder.append(delimiter).append("UNIQUE INDEX `").append(this.annotation.value()).append("_UNIQUE` (`")
					.append(this.annotation.value()).append("` ASC) VISIBLE");

		if (this.annotation.index())
			builder.append(delimiter).append("INDEX `").append(this.annotation.value()).append("` (`")
					.append(this.annotation.value()).append("` ASC) VISIBLE");
	}

	public String getName() {
		return this.annotation.value();
	}

	Field getField() {
		return this.field;
	}

	DataType getType() {
		return this.type;
	}
}
