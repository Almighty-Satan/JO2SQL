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
import com.github.almightysatan.jo2sql.impl.AnnotatedField;
import com.github.almightysatan.jo2sql.impl.Table;

public class SqliteTable<T extends SqlSerializable> extends Table<T> {

	SqliteTable(SqliteProviderImpl provider, Class<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(provider, type);
	}

	@Override
	protected String getLastInsertIdFunc() {
		return "last_insert_rowid";
	}

	@Override
	protected void check() throws SQLException {
		if (!this.provider
				.executeQuery("SELECT * FROM sqlite_schema WHERE type='table' AND name = '" + this.name + "' LIMIT 1;")
				.next()) {
			// Table does not exist
			this.provider.getLogger().info("Creating table %s", this.name);

			StringBuilder statement = new StringBuilder().append("CREATE TABLE ").append(this.fullName).append(" (");
			boolean first = true;
			for (AnnotatedField field : this.fields.values()) {
				if (first)
					first = false;
				else
					statement.append(",");
				field.appendColumn(statement);
				field.appendIndex(statement, ",");
			}
			if (!this.primaryKey.indexFields.isEmpty())
				this.primaryKey.appendIndex(statement, ",");
			statement.append(");");

			this.provider.executeUpdate(statement.toString());
		} else {
			// Table exists
			// TODO implement this
		}
	}
}