package com.github.almightysatan.jo2sql.impl.mysql;

import com.github.almightysatan.jo2sql.impl.datatypes.StringDataType;

public class MysqlStringDataType extends StringDataType {

	@Override
	public String getDatatype(int size) {
		if (size <= 0)
			throw new Error("Invalid size: " + size);

		return "VARCHAR(" + size + ") CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci'";
	}
}
