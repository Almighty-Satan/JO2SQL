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

package com.github.almightysatan.jo2sql.impl.attributes;

import com.github.almightysatan.jo2sql.impl.ColumnData;
import com.github.almightysatan.jo2sql.impl.Index;
import com.github.almightysatan.jo2sql.impl.Serializable;

public interface SerializableAttribute extends Serializable<Object> {

	static final char INTERNAL_COLUMN_DELIMITER = '#';

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

	Index[] getIndices();
}
