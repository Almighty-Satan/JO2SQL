package com.github.almightysatan.jo2sql.impl.datatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.DataType;

public class IntDataType implements DataType {

	@Override
	public Class<?>[] getClasses() {
		return new Class[] { int.class, Integer.class };
	}

	@Override
	public String getDatatype(int size) {
		return "INT";
	}

	@Override
	public Object getValue(ResultSet result, String label) throws SQLException {
		return result.getInt(label);
	}

	@Override
	public void setValue(PreparedStatement statement, int index, Object value) throws SQLException {
		statement.setInt(index, (int) value);
	}
}
