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

import com.github.almightysatan.jo2sql.SqlSerializable;

public class MapEntry implements SqlSerializable {

	/*
	 * 1. Primary Key columns of the class that declares this map
	 * 
	 * 2. A single column for the map key (no nested objects, maps, lists, arrays,
	 * etc. as key
	 * 
	 * 3. Columns for the map value
	 */

	@Override
	public String getTableName() {
		return "MapEntry";
	}
}
