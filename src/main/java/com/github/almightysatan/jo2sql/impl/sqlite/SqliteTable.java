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

package com.github.almightysatan.jo2sql.impl.sqlite;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.SerializableClass;
import com.github.almightysatan.jo2sql.impl.Table;
import com.github.almightysatan.jo2sql.impl.fields.AnnotatedField;
import com.github.almightysatan.jo2sql.impl.fields.ColumnData;

public class SqliteTable<T extends SqlSerializable> extends Table<T> {

	SqliteTable(SqliteProviderImpl provider, SerializableClass<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(provider, type);
	}

	@Override
	protected void check() throws SQLException {
		if (!this.provider.executeQuery(
				"SELECT * FROM sqlite_schema WHERE type='table' AND name = '" + this.type.getName() + "' LIMIT 1;")
				.next()) {
			// Table does not exist
			this.provider.getLogger().info("Creating table %s", this.getFullName());

			StringBuilder statement = new StringBuilder().append("CREATE TABLE ").append(this.fullName).append(" (");
			boolean first = true;
			for (AnnotatedField field : this.getType().getFields().values()) {
				for (ColumnData column : field.getColumnData()) {
					if (first)
						first = false;
					else
						statement.append(",");
					statement.append("`").append(column.getName()).append("`").append(column.getSql());
				}
				field.appendIndex(statement, ",");
			}
			this.getType().getPrimaryKey().appendIndex(statement, ",");
			statement.append(");");

			this.provider.executeUpdate(statement.toString());
		} else {
			// Table exists
			// TODO implement this
		}
	}
}
