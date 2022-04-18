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

package com.github.almightysatan.jo2sql.impl.mysql;

import java.lang.reflect.InvocationTargetException;

import com.github.almightysatan.jo2sql.impl.ColumnData;
import com.github.almightysatan.jo2sql.impl.SerializableAttribute;
import com.github.almightysatan.jo2sql.impl.SerializableObject;
import com.github.almightysatan.jo2sql.impl.Table;

public class MysqlTable<T> extends Table<T> {

	MysqlTable(MysqlProviderImpl provider, SerializableObject<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(provider, type);
	}

	@Override
	protected String getFullName(String name) {
		return "`" + ((MysqlProviderImpl) this.provider).getSchema() + "`.`" + this.getType().getName() + "`";
	}

	@Override
	protected void check() throws Throwable {
		String schema = ((MysqlProviderImpl) this.provider).getSchema();

		if (!this.provider.executeQuery("SELECT * FROM information_schema.tables WHERE table_schema = '" + schema
				+ "' AND table_name = '" + this.type.getName() + "' LIMIT 1;").next()) {
			// Table does not exist
			this.provider.getLogger().info("Creating table %s", this.getFullName());

			StringBuilder statement = new StringBuilder().append("CREATE TABLE ").append(this.fullName).append(" (");
			boolean first = true;
			for (SerializableAttribute attribute : this.getType().getAttributes()) {
				for (ColumnData column : attribute.getColumnData()) {
					if (first)
						first = false;
					else
						statement.append(",");
					statement.append("`").append(column.getName()).append("`").append(column.getSqlStatement());
				}
				attribute.appendIndex(statement, ",");
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
