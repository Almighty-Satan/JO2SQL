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

package com.github.almightysatan.jo2sql.impl;

public class ColumnData {

	private final String name;
	private String sqlType;
	private String sqlStatement;
	private String replaceSql;

	public ColumnData(String name, String sqlType, String replaceSql) {
		this.name = name;
		this.sqlType = sqlType;
		this.sqlStatement = sqlType;
		this.replaceSql = replaceSql;
	}

	public String getName() {
		return this.name;
	}

	public void setSqlType(String sqlType) {
		this.sqlType = sqlType;
	}

	public String getSqlType() {
		return this.sqlType;
	}

	public String getSqlStatement() {
		return this.sqlStatement;
	}

	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	public String getReplaceSql() {
		return this.replaceSql;
	}
}
