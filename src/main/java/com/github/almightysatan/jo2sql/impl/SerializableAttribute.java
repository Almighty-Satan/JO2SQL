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

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface SerializableAttribute {

	static final char INTERNAL_COLUMN_DELIMITER = '#';

	void appendIndex(StringBuilder builder, String delimiter);

	/**
	 * Sets the parameters of a {@link CachedStatement} to the value of this
	 * attribute. This method may invoke database requests to save nested objects to
	 * the database.
	 * 
	 * @param statement  The {@link CachedStatement}
	 * @param startIndex The index where the first parameter should be set
	 * @param value      The value that should be loaded into the
	 *                   {@link PreparedStatement}
	 * @throws Throwable Depending on the implementation this method may throw a
	 *                   number of different exceptions
	 */
	int serialize(CachedStatement statement, int startIndex, Object value, ResultSet prevValues) throws Throwable;

	/**
	 * Creates an object that is created from the contents of the given
	 * {@link ResultSet}. This method may invoke further database requests to load
	 * nested objects.
	 * 
	 * @param prefix A prefix that is added to the column name when loading values
	 *               from the {@link ResultSet}. May be empty but should not be null
	 * @param result The {@link ResultSet}
	 * @throws Throwable Depending on the implementation this method may throw a
	 *                   number of different exceptions
	 */
	Object deserialize(String prefix, ResultSet result) throws Throwable;

	/**
	 * Deletes any nested object from the database. If this is not a nested object
	 * this will do nothing. Otherwise it will invoke one or multiple database
	 * requests.
	 * 
	 * @param value The object to be deleted
	 * @throws Throwable Depending on the implementation this method may throw a
	 *                   number of different exceptions
	 */
	void deleteNested(Object value) throws Throwable;

	String getColumnName();

	ColumnData[] getColumnData();

	boolean needsPrevValue();

}
