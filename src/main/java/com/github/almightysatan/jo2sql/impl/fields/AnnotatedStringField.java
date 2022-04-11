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

public abstract class AnnotatedStringField extends SimpleAnnotatedField {

	public AnnotatedStringField(SqlProviderImpl provider, Field field, Column annotation) throws Throwable {
		super(provider, field, annotation);
	}

	@Override
	protected final String loadColumn() {
		int size = this.getColumnAnnotation().size();
		if (size <= 0)
			throw new Error("Invalid size: " + size);
		return this.loadColumn(size);
	}

	protected abstract String loadColumn(int size);

	@Override
	public void setValues(PreparedStatement statement, int index, Object value) throws Throwable {
		statement.setString(index, (String) value);
	}

	@Override
	public Object loadValue(String prefix, ResultSet result) throws Throwable {
		return result.getString(prefix + this.getColumnName());
	}
}
