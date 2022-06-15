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

import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.impl.CachedStatement;
import com.github.almightysatan.jo2sql.impl.SerializableObject;
import com.github.almightysatan.jo2sql.impl.TableImpl;

public class MysqlTable<T> extends TableImpl<T> {

	MysqlTable(MysqlProviderImpl provider, SerializableObject<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(provider, type);
	}

	@Override
	protected String getFullName(String name) {
		return "`" + ((MysqlProviderImpl) this.provider).getSchema() + "`.`" + name + "`";
	}

	@Override
	protected CachedStatement getTableSelectStatement() {
		String schema = ((MysqlProviderImpl) this.provider).getSchema();
		DataType stringType = this.provider.getStringType();

		CachedStatement statement = this.provider.prepareStatement(
				"SELECT * FROM information_schema.tables WHERE table_schema = ? AND table_name = ? LIMIT 1;");
		statement.setParameter(0, stringType, schema);
		statement.setParameter(1, stringType, this.type.getName());
		return statement;
	}

	@Override
	protected CachedStatement getColumnSelectStatement() {
		String schema = ((MysqlProviderImpl) this.provider).getSchema();
		DataType stringType = this.provider.getStringType();

		CachedStatement statement = this.provider
				.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=? AND TABLE_NAME=?;");
		statement.setParameter(0, stringType, schema);
		statement.setParameter(1, stringType, this.type.getName());
		return statement;
	}

	@Override
	protected String getIndicesSelectStatement() {
		return "SHOW INDEX FROM " + this.fullName + ";";
	}

	@Override
	protected String getIndexDropStatement(String name) {
		return "ALTER TABLE " + this.fullName + " DROP INDEX `" + name + "`;";
	}

	@Override
	protected String getColumnNameLabel() {
		return "COLUMN_NAME";
	}

	@Override
	protected String getIndexNameLabel() {
		return "Key_name";
	}
}
