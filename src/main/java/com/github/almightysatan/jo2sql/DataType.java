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

package com.github.almightysatan.jo2sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface DataType {

	/**
	 * Sets the parameter of a {@link PreparedStatement} to the value of this type
	 * 
	 * @param statement The {@link PreparedStatement}
	 * @param index     The index of the parameter
	 * @param value     The value that should be loaded into the
	 *                  {@link PreparedStatement}
	 * @throws Throwable Depending on the implementation a number of different
	 *                   exceptions may occur
	 */
	void serialize(PreparedStatement statement, int index, Object value) throws Throwable;

	/**
	 * Returns a value read from the {@link ResultSet}
	 * 
	 * @param columnLabel A prefix that is added to the column name when loading
	 *                    values from the {@link ResultSet}. May be empty but should
	 *                    not be null
	 * @param result      The {@link ResultSet}
	 * @throws Throwable Depending on the implementation a number of different
	 *                   exceptions may occur
	 */
	Object deserialize(String columnLabel, ResultSet result) throws Throwable;

	Class<?>[] getClasses();

	String getSqlType(int size);

	default boolean isOfType(Class<?> type) {
		for (Class<?> clazz : this.getClasses())
			if (clazz == type)
				return true;
		return false;
	}

	default String getPreparedReplaceSql(String columnName, String tableName) {
		return "?";
	}
}
