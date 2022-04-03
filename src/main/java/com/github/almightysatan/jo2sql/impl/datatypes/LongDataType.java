package com.github.almightysatan.jo2sql.impl.datatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.DataType;

public class LongDataType implements DataType {

	@Override
	public Class<?>[] getClasses() {
		return new Class[] { long.class, Long.class };
	}

	@Override
	public String getDatatype(int size) {
		return "BIGINT";
	}

	@Override
	public Object getValue(ResultSet result, String label) throws SQLException {
		return result.getLong(label);
	}

	@Override
	public void setValue(PreparedStatement statement, int index, Object value) throws SQLException {
		statement.setLong(index, (long) value);
	}
}
