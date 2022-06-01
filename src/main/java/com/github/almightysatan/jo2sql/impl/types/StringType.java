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

package com.github.almightysatan.jo2sql.impl.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.DataType;

public class StringType implements DataType {

	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] { String.class };
	}

	@Override
	public String getSqlType(int size) {
		throw new UnsupportedOperationException();
	}

	protected void assertValidSize(int size) {
		if (size <= 0)
			throw new Error("Invalid String size: " + size);
	}

	@Override
	public void serialize(PreparedStatement statement, int index, Object value) throws SQLException {
		statement.setString(index, (String) value);
	}

	@Override
	public Object deserialize(String columnLabel, ResultSet result) throws SQLException {
		return result.getString(columnLabel);
	}
}
