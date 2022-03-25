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

package com.github.almightysatan.jo2sql.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

enum DataType {

	INT(int.class, Integer.class) {
		@Override
		String getDatatype(int size) {
			return "INT";
		}

		@Override
		Object getValue(ResultSet result, String label) throws SQLException {
			return result.getInt(label);
		}

		@Override
		void setValue(PreparedStatement statement, int index, Object value) throws SQLException {
			statement.setInt(index, (int) value);
		}
	},
	LONG(long.class, Long.class) {
		@Override
		String getDatatype(int size) {
			return "BIGINT";
		}

		@Override
		Object getValue(ResultSet result, String label) throws SQLException {
			return result.getLong(label);
		}

		@Override
		void setValue(PreparedStatement statement, int index, Object value) throws SQLException {
			statement.setLong(index, (long) value);
		}
	},
	BOOL(boolean.class, Boolean.class) {
		@Override
		String getDatatype(int size) {
			return "BOOL";
		}

		@Override
		Object getValue(ResultSet result, String label) throws SQLException {
			return result.getBoolean(label);
		}

		@Override
		void setValue(PreparedStatement statement, int index, Object value) throws SQLException {
			statement.setBoolean(index, (boolean) value);
		}
	},
	STRING(String.class) {
		@Override
		String getDatatype(int size) {
			if (size <= 0)
				throw new Error("Invalid size: " + size);

			return "VARCHAR(" + size + ") CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci'";
		}

		@Override
		Object getValue(ResultSet result, String label) throws SQLException {
			return result.getString(label);
		}

		@Override
		void setValue(PreparedStatement statement, int index, Object value) throws SQLException {
			statement.setString(index, (String) value);
		}
	};

	private Class<?>[] types;

	private DataType(Class<?>... types) {
		this.types = types;
	}

	abstract String getDatatype(int size);

	abstract Object getValue(ResultSet result, String label) throws SQLException;

	abstract void setValue(PreparedStatement statement, int index, Object value) throws SQLException;

	static DataType get(Class<?> type) {
		for (DataType value : values())
			for (Class<?> t : value.types)
				if (t == type)
					return value;
		throw new Error("Unsupported type: " + type.getName());
	}
}
