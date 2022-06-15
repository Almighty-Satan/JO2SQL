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
import java.util.UUID;

import com.github.almightysatan.jo2sql.DataType;

public class UuidType implements DataType {

	private static final int SIZE = 36;

	private DataType stringType;

	public UuidType(DataType stringType) {
		this.stringType = stringType;
	}

	@Override
	public void serialize(PreparedStatement statement, int index, Object value) throws Throwable {
		this.stringType.serialize(statement, index, value != null ? ((UUID) value).toString() : null);
	}

	@Override
	public Object deserialize(String columnLabel, ResultSet result) throws Throwable {
		String string = (String) this.stringType.deserialize(columnLabel, result);
		return string != null ? UUID.fromString(string) : null;
	}

	@Override
	public Class<?>[] getClasses() {
		return new Class<?>[] { UUID.class };
	}

	@Override
	public String getSqlType(int size) {
		return this.stringType.getSqlType(SIZE);
	}

}
