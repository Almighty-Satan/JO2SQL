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

@SqlSerializable("ChildChildObject")
public class ChildChildObject {

	@Column(value = "firstName", size = 100, primary = true)
	public String firstName;
	@Column(value = "lastName", size = 100, primary = true)
	public String lastName;
	@Column(value = "age")
	public int age;

	public ChildChildObject() {
	}

	public ChildChildObject(String firstName, String lastName, int age) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.age, this.firstName, this.lastName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		ChildChildObject other = (ChildChildObject) obj;
		return Objects.equals(this.age, other.age) && Objects.equals(this.firstName, other.firstName)
				&& Objects.equals(this.lastName, other.lastName);
	}

	@Override
	public String toString() {
		return "ChildChildObject [firstName=" + this.firstName + ", lastName=" + this.lastName + ", age=" + this.age
				+ "]";
	}
}
