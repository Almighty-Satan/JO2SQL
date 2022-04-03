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
