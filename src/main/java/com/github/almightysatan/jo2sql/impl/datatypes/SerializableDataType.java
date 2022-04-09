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

package com.github.almightysatan.jo2sql.impl.datatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.AnnotatedField;
import com.github.almightysatan.jo2sql.impl.ColumnData;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.Table;

public class SerializableDataType implements DataType {

	@Override
	public Class<?>[] getClasses() {
		return new Class[] { SqlSerializable.class };
	}

	@Override
	public ColumnData[] getColumnData(SqlProviderImpl provider, Class<?> type, int size) {
		@SuppressWarnings("unchecked")
		Table<?> table = provider.getTable((Class<SqlSerializable>) type);
		List<ColumnData> dataList = new ArrayList<>();
		for (AnnotatedField field : table.getType().getPrimaryKey().indexFields)
			for (ColumnData data : field.getColumnData())
				dataList.add(new ColumnData(data.getProcessedName(), data.getSqlType()));
		return dataList.toArray(new ColumnData[dataList.size()]);
	}

	@Override
	public Object getValue(SqlProviderImpl provider, Class<?> type, ResultSet result, String label) throws Throwable {
		@SuppressWarnings("unchecked")
		Table<SqlSerializable> table = provider.getTable((Class<SqlSerializable>) type);
		Object[] values = new Object[table.getType().getPrimaryKey().indexFields.length];
		int i = 0;
		for (AnnotatedField field : table.getType().getPrimaryKey().indexFields)
			values[i++] = field.getType().getValue(provider, field.getField().getType(), result,
					label + AnnotatedField.INTERNAL_COLUMN_DELIMITER + field.getColumnData()[0].getProcessedName());

		return provider.runDatabaseAction(table.preparePrimarySelect().values(values));
	}

	@Override
	public void setValue(SqlProviderImpl provider, PreparedStatement statement, int index, Object value)
			throws Throwable {
		@SuppressWarnings("unchecked")
		Table<SqlSerializable> table = provider.getTable((Class<SqlSerializable>) value.getClass());
		for (AnnotatedField field : table.getType().getPrimaryKey().indexFields)
			field.getType().setValue(provider, statement, index++, field.getField().get(value));
		provider.runDatabaseAction(table.prepareReplace().object((SqlSerializable) value));
	}
}
