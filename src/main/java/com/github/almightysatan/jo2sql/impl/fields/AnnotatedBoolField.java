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

public class AnnotatedBoolField extends SimpleAnnotatedField {

	public AnnotatedBoolField(SqlProviderImpl provider, Field field, Column annotation) throws Throwable {
		super(provider, field, annotation);
	}

	@Override
	protected String loadColumn() {
		return "BOOL";
	}

	@Override
	public void setValues(PreparedStatement statement, int index, Object value) throws Throwable {
		statement.setBoolean(index, value == null ? false : (boolean) value);
	}

	@Override
	public Object loadValue(String prefix, ResultSet result) throws Throwable {
		return result.getBoolean(prefix + this.getColumnName());
	}
}
