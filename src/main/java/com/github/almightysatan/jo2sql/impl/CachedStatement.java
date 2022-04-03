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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.DataType;

public class CachedStatement {

	private final String sql;
	private final DataType[] parameterTypes;
	private final Object[] parameters;
	private PreparedStatement statement;

	public CachedStatement(String sql, int numValues) {
		this.sql = sql;
		this.parameterTypes = new DataType[numValues];
		this.parameters = new Object[numValues];
	}

	public void setParameter(int parameterIndex, DataType parameterType, Object parameter) {
		this.parameterTypes[parameterIndex] = parameterType;
		this.parameters[parameterIndex] = parameter;
	}

	public PreparedStatement getValidPreparedStatement(Connection connection) throws SQLException {
		if (this.statement == null || this.statement.getConnection() != connection)
			this.statement = connection.prepareStatement(this.sql);

		for (int i = 0; i < this.parameterTypes.length; i++)
			this.parameterTypes[i].setValue(this.statement, i + 1, this.parameters[i]);

		return this.statement;
	}
}
