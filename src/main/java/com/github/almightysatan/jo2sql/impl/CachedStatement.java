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

public class CachedStatement {

	private final String sql;
	private final SerializableAttribute[] fields;
	private final Object[] parameters;
	private PreparedStatement statement;

	public CachedStatement(String sql, int numValues) {
		this.sql = sql;
		this.fields = new SerializableAttribute[numValues];
		this.parameters = new Object[numValues];
	}

	public void setParameter(int parameterIndex, SerializableAttribute field, Object parameter) {
		this.fields[parameterIndex] = field;
		this.parameters[parameterIndex] = parameter;
	}

	public PreparedStatement getValidPreparedStatement(SqlProviderImpl provider, Connection connection)
			throws Throwable {
		if (this.statement == null || this.statement.getConnection() != connection)
			this.statement = connection.prepareStatement(this.sql);

		for (int i = 1, j = 0; j < this.fields.length; j++) {
			SerializableAttribute field = this.fields[j];
			if (field != null) {
				field.serialize(this.statement, i, this.parameters[j]);
				i += field.getColumnData().length;
			}
		}

		return this.statement;
	}
}
