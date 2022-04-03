package com.github.almightysatan.jo2sql.impl.sqlite;

import com.github.almightysatan.jo2sql.impl.datatypes.StringDataType;

public class SqliteStringDataType extends StringDataType {

	@Override
	public String getDatatype(int size) {
		if (size <= 0)
			throw new Error("Invalid size: " + size);

		return "VARCHAR(" + size + ")";
	}
}
