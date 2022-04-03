package com.github.almightysatan.jo2sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DataType {

	abstract Class<?>[] getClasses();

	abstract String getDatatype(int size);

	abstract Object getValue(ResultSet result, String label) throws SQLException;

	abstract void setValue(PreparedStatement statement, int index, Object value) throws SQLException;
}
