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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.almightysatan.jo2sql.annotations.Column;
import com.github.almightysatan.jo2sql.annotations.ListColumn;
import com.github.almightysatan.jo2sql.annotations.SqlSerializable;

@SqlSerializable("NestedObjectList")
public class NestedObjectList {

	@Column(value = "id", autoIncrement = true, primary = true)
	long id;

	@ListColumn(valueType = ChildChildObject.class)
	@Column(value = "list", type = ArrayList.class, notNull = false)
	List<ChildChildObject> list;

	public NestedObjectList() {
	}

	public NestedObjectList(List<ChildChildObject> list) {
		this.list = list;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.list);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		NestedObjectList other = (NestedObjectList) obj;
		return this.id == other.id && Objects.equals(this.list, other.list);
	}

	@Override
	public String toString() {
		return "NestedObjectList [id=" + this.id + ", list=" + this.list + "]";
	}
}
