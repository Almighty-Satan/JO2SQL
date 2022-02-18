package com.github.almightysatan.jo2sql.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class CachedStatement {

	private final String sql;
	private final DataType[] parameterTypes;
	private final Object[] parameters;
	private PreparedStatement statement;

	CachedStatement(String sql, int numValues) {
		this.sql = sql;
		this.parameterTypes = new DataType[numValues];
		this.parameters = new Object[numValues];
	}

	void setParameter(int parameterIndex, DataType parameterType, Object parameter) {
		this.parameterTypes[parameterIndex] = parameterType;
		this.parameters[parameterIndex] = parameter;
	}

	void setParameter(int parameterIndex, Object parameter) {
		this.setParameter(parameterIndex, DataType.get(parameter.getClass()), parameter);
	}

	PreparedStatement getValidPreparedStatement(Connection connection) throws SQLException {
		if (this.statement == null || this.statement.getConnection() != connection)
			this.statement = connection.prepareStatement(this.sql);

		for (int i = 0; i < this.parameterTypes.length; i++)
			this.parameterTypes[i].setValue(this.statement, i + 1, this.parameters[i]);

		return this.statement;
	}
}
