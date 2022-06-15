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

import com.github.almightysatan.jo2sql.annotations.Column;
import com.github.almightysatan.jo2sql.annotations.SqlSerializable;

@SqlSerializable("ParentObject")
public class ParentObject {

	@Column(value = "id", primary = true, autoIncrement = true)
	public long id;
	@Column(value = "child", notNull = false)
	public ChildObject child;

	public ParentObject() {
	}

	public ParentObject(ChildObject child) {
		this.child = child;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.child, this.id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		ParentObject other = (ParentObject) obj;
		return Objects.equals(this.child, other.child) && this.id == other.id;
	}

	@Override
	public String toString() {
		return "ParentObject [integer=" + this.id + ", child=" + this.child + "]";
	}
}
