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

public class Subclass extends Superclass {

	@Column(value = "id", autoIncrement = true, primary = true)
	long id;
	@Column(value = "abc", size = 100)
	private String abc;

	public Subclass() {
	}

	public Subclass(String test, String abc) {
		super(test);
		this.abc = abc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.abc, this.id);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Subclass other = (Subclass) obj;
		return Objects.equals(this.abc, other.abc) && this.id == other.id;
	}

	@Override
	public String toString() {
		return "Subclass [id=" + this.id + ", abc=" + this.abc + "]";
	}

	@Override
	public String getTableName() {
		return "Subclass";
	}
}
