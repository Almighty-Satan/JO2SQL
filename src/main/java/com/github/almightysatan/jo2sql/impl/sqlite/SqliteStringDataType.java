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

package com.github.almightysatan.jo2sql.impl.sqlite;

import com.github.almightysatan.jo2sql.impl.SerializableClass;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.datatypes.StringDataType;

public class SqliteStringDataType extends StringDataType {

	@Override
	public String getSqlDataType(SqlProviderImpl provider, SerializableClass<?> serializableClass, int size) {
		if (size <= 0)
			throw new Error("Invalid size: " + size);

		return "VARCHAR(" + size + ")";
	}
}
