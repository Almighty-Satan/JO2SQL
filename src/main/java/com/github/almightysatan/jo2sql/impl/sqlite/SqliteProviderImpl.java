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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.github.almightysatan.jo2sql.Column;
import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.SerializableClass;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.Table;
import com.github.almightysatan.jo2sql.impl.fields.AnnotatedField;
import com.github.almightysatan.jo2sql.impl.fields.FieldSupplier;
import com.github.almightysatan.jo2sql.logger.Logger;

public class SqliteProviderImpl extends SqlProviderImpl {

	static final FieldSupplier STRING_FIELD_PROVIDER = new FieldSupplier() {

		@Override
		public boolean isType(Field field) {
			return field.getType() == String.class;
		}

		@Override
		public AnnotatedField createField(SqlProviderImpl provider, Field field, Column annotation) throws Throwable {
			return new SqliteAnnotatedStringField(provider, field, annotation);
		}
	};

	private final String path;

	private SqliteProviderImpl(Logger logger, List<FieldSupplier> types, String path) {
		super(logger, types = new ArrayList<>(types));
		types.add(STRING_FIELD_PROVIDER);
		this.path = path;
	}

	public SqliteProviderImpl(Logger logger, List<FieldSupplier> types) {
		this(logger, types, (String) null);
	}

	public SqliteProviderImpl(Logger logger, List<FieldSupplier> types, File file) {
		this(logger, types, file.getAbsolutePath());
	}

	@Override
	protected Connection createConnection() throws SQLException {
		return this.path == null ? DriverManager.getConnection("jdbc:sqlite::memory:")
				: DriverManager.getConnection("jdbc:sqlite:" + this.path);
	}

	@Override
	protected String getLastInsertIdFunc() {
		return "last_insert_rowid";
	}

	@Override
	protected <T extends SqlSerializable> Table<T> newTable(SerializableClass<T> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		return new SqliteTable<>(this, type);
	}
}
