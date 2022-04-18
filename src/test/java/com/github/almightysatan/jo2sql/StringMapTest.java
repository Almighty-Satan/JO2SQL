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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StringMapTest implements SqlSerializable {

	@Column(value = "id", autoIncrement = true, primary = true)
	long id;

	@MapColumn(keyType = String.class, keySize = 100, valueType = String.class, valueSize = 100)
	@Column(value = "map", type = HashMap.class)
	Map<String, String> map;

	public StringMapTest() {
	}

	public StringMapTest(Map<String, String> map) {
		this.map = map;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.map);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		StringMapTest other = (StringMapTest) obj;
		return this.id == other.id && Objects.equals(this.map, other.map);
	}

	@Override
	public String toString() {
		return "StringMapTest [id=" + this.id + ", map=" + this.map + "]";
	}

	@Override
	public String getTableName() {
		return "StringMap";
	}
}