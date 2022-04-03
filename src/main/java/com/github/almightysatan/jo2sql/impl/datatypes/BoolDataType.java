package com.github.almightysatan.jo2sql.impl.datatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.DataType;

public class BoolDataType implements DataType {

	@Override
	public Class<?>[] getClasses() {
		return new Class[] { boolean.class, Boolean.class };
	}

	@Override
	public String getDatatype(int size) {
		return "BOOL";
	}

	@Override
	public Object getValue(ResultSet result, String label) throws SQLException {
		return result.getBoolean(label);
	}

	@Override
	public void setValue(PreparedStatement statement, int index, Object value) throws SQLException {
		statement.setBoolean(index, (boolean) value);
	}
}
