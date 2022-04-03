package com.github.almightysatan.jo2sql.impl.datatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.DataType;

public abstract class StringDataType implements DataType {

	@Override
	public Class<?>[] getClasses() {
		return new Class[] { String.class };
	}

	@Override
	public Object getValue(ResultSet result, String label) throws SQLException {
		return result.getString(label);
	}

	@Override
	public void setValue(PreparedStatement statement, int index, Object value) throws SQLException {
		statement.setString(index, (String) value);
	}

}
