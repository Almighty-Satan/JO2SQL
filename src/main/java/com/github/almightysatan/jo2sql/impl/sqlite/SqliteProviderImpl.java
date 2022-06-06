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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.impl.SerializableObject;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.TableImpl;
import com.github.almightysatan.jo2sql.logger.Logger;

public class SqliteProviderImpl extends SqlProviderImpl {

	static final DataType AI_LONG_TYPE = new SqliteAiLongType();
	static final DataType STRING_TYPE = new SqliteStringType();

	private final String path;

	private SqliteProviderImpl(Logger logger, List<DataType> types, String path) {
		super(logger, types);
		this.path = path;
	}

	public SqliteProviderImpl(Logger logger, List<DataType> types) {
		this(logger, types, (String) null);
	}

	public SqliteProviderImpl(Logger logger, List<DataType> types, File file) {
		this(logger, types, file.getAbsolutePath());
	}

	@Override
	protected Connection createConnection() throws SQLException {
		return this.path == null ? DriverManager.getConnection("jdbc:sqlite::memory:")
				: DriverManager.getConnection("jdbc:sqlite:" + this.path);
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
	protected long getLastInsertId(String tableName) throws Throwable {
		ResultSet result = this.executeQuery("SELECT IFNULL(MAX(`id`), 0) FROM " + tableName + ";");
		result.next();
		return result.getLong(1);
	}

	@Override
	public <T> TableImpl<T> newTable(SerializableObject<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return new SqliteTable<>(this, type);
	}
}
