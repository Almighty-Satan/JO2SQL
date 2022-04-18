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

package com.github.almightysatan.jo2sql;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.almightysatan.jo2sql.impl.mysql.MysqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.sqlite.SqliteProviderImpl;
import com.github.almightysatan.jo2sql.logger.CoutLogger;
import com.github.almightysatan.jo2sql.logger.Logger;

/**
 * Used to create an {@link SqlProvider}. Can be used to create multiple
 * {@link SqlProvider SqlProviders}
 * 
 * @author Almighty-Satan
 */
public class SqlBuilder {

	private static final Logger DEFAULT_LOGGER = new CoutLogger();

	private Logger logger = DEFAULT_LOGGER;
	private List<DataType> dataTypes = new ArrayList<>();

	public SqlBuilder addDataTypes(DataType... dataTypes) {
		this.dataTypes.addAll(Arrays.asList(dataTypes));
		return this;
	}

	public SqlBuilder addDataTypes(List<DataType> dataTypes) {
		this.dataTypes.addAll(dataTypes);
		return this;
	}

	/**
	 * Sets the {@link Logger} for all {@link SqlProvider SqlProviders} created from
	 * this {@link SqlBuilder}. The default value is a static instance of
	 * {@link CoutLogger}
	 * 
	 * @param logger The {@link Logger}
	 * @return This {@link SqlBuilder}
	 */
	public SqlBuilder setLogger(Logger logger) {
		if (logger == null)
			throw new IllegalArgumentException();

		this.logger = logger;
		return this;
	}

	/**
	 * Creates a MYSQL connection. The {@link SqlBuilder} can be reused afterwards.
	 * 
	 * @param url      The URL of the database
	 * @param user     The user
	 * @param password The password of the user
	 * @param schema   The name of the schema
	 * @return An {@link SqlProvider}
	 */
	public SqlProvider mysql(String url, String user, String password, String schema) {
		return new MysqlProviderImpl(this.logger, new ArrayList<>(this.dataTypes), url, user, password, schema);
	}

	/**
	 * Creates an in-memory Sqlite database. The {@link SqlBuilder} can be reused
	 * afterwards.
	 * 
	 * @return An {@link SqlProvider}
	 */
	public SqlProvider sqlite() {
		return new SqliteProviderImpl(this.logger, new ArrayList<>(this.dataTypes));
	}

	/**
	 * Creates an Sqlite database that is saved to the given file. The
	 * {@link SqlBuilder} can be reused afterwards.
	 * 
	 * @return An {@link SqlProvider}
	 */
	public SqlProvider sqlite(File file) {
		return new SqliteProviderImpl(this.logger, new ArrayList<>(this.dataTypes), file);
	}
}
