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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.impl.SerializableObject;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.TableImpl;
import com.github.almightysatan.jo2sql.logger.Logger;

public class MysqlProviderImpl extends SqlProviderImpl {

	static final DataType AI_LONG_TYPE = new MysqlAiLongType();
	static final DataType STRING_TYPE = new MysqlStringType();

	private final String url;
	private final String user;
	private final String password;
	private final String schema;

	public MysqlProviderImpl(Logger logger, List<DataType> types, String url, String user, String password,
			String schema) {
		super(logger, types);
		this.url = url;
		this.user = user;
		this.password = password;
		this.schema = schema;
	}

	@Override
	protected Connection createConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://" + this.url + "?allowMultiQueries=true", this.user,
				this.password);
	}

	@Override
	protected DataType getStringType() {
		return STRING_TYPE;
	}

	@Override
	protected DataType getAiLongType() {
		return AI_LONG_TYPE;
	}

	@Override
	protected String getLastInsertIdFunc() {
		return "LAST_INSERT_ID";
	}

	@Override
	public <T> TableImpl<T> newTable(SerializableObject<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return new MysqlTable<>(this, type);
	}

	public String getSchema() {
		return this.schema;
	}
}
