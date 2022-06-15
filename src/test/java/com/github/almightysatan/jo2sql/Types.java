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
import java.util.UUID;

import com.github.almightysatan.jo2sql.annotations.Column;
import com.github.almightysatan.jo2sql.annotations.SqlSerializable;

@SqlSerializable("Types")
public class Types {

	@Column(value = "uuid", primary = true)
	public UUID uuid;

	@Column(value = "enum", unique = true)
	public TestEnum enumValue;

	public Types() {
	}

	public Types(UUID uuid, TestEnum enumValue) {
		this.uuid = uuid;
		this.enumValue = enumValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.enumValue, this.uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Types other = (Types) obj;
		return this.enumValue == other.enumValue && Objects.equals(this.uuid, other.uuid);
	}

	@Override
	public String toString() {
		return "Types [uuid=" + this.uuid + ", enumValue=" + this.enumValue + "]";
	}

	public static enum TestEnum {
		ABC, DEF, GHI;
	}

}
