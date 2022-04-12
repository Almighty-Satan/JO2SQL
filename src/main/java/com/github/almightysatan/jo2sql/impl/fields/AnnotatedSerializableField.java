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
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.Table;

public class AnnotatedSerializableField extends AnnotatedField {

	private Table<SqlSerializable> table;
	private PreparedReplace<SqlSerializable, Void> replace;
	private PreparedSelect<SqlSerializable> primarySelect;

	public AnnotatedSerializableField(SqlProviderImpl provider, Field field, Column annotation) throws Throwable {
		super(provider, field, annotation);

		if (annotation.primary())
			throw new Error("The type of field that is part of the primary key can not be a nested object");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ColumnData[] loadColumns() throws Throwable {
		this.table = this.getProvider().getTable((Class<SqlSerializable>) this.getField().getType());
		this.replace = this.table.prepareReplace();
		this.primarySelect = this.table.preparePrimarySelect();

		ColumnData[] dataArray = new ColumnData[this.table.getType().getPrimaryKey().getColumnData().length];
		int i = 0;
		for (ColumnData data : this.table.getType().getPrimaryKey().getColumnData()) {
			dataArray[i++] = new ColumnData(this.getColumnName() + INTERNAL_COLUMN_DELIMITER + data.getName(),
					data.getSqlType());
		}

		return dataArray;
	}

	@Override
	public void setValues(PreparedStatement statement, int startIndex, Object value) throws Throwable {
		for (AnnotatedField field : this.table.getType().getPrimaryKey().getIndexFields())
			field.setValues(statement, startIndex++, value == null ? null : field.getField().get(value));
		if (value != null)
			this.getProvider().runDatabaseAction(this.replace.object((SqlSerializable) value));
	}

	@Override
	public Object loadValue(String prefix, ResultSet result) throws Throwable {
		Object[] values = new Object[this.table.getType().getPrimaryKey().getIndexFields().length];
		int i = 0;
		for (AnnotatedField field : this.table.getType().getPrimaryKey().getIndexFields())
			values[i++] = field.loadValue(prefix + this.getColumnName() + INTERNAL_COLUMN_DELIMITER, result);

		return this.getProvider().runDatabaseAction(this.primarySelect.values(values));
	}
}
