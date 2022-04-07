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

import java.util.Objects;

public class TestObject implements SqlSerializable {

	@Column(value = "string", size = 100, primary = true)
	public String string;
	@Column("bool")
	public boolean bool;
	@Column("integer")
	public int integer;

	public TestObject() {
	}

	public TestObject(String string, boolean bool, int integer) {
		super();
		this.string = string;
		this.bool = bool;
		this.integer = integer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.bool, this.integer, this.string);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		TestObject other = (TestObject) obj;
		return this.bool == other.bool && this.integer == other.integer && Objects.equals(this.string, other.string);
	}

	@Override
	public String getTableName() {
		return "TestObject";
	}
}
