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

import java.io.File;
import java.util.ArrayList;

import com.github.almightysatan.jo2sql.impl.mysql.MysqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.sqlite.SqliteProviderImpl;

public class SqlBuilder {

	public SqlProvider mysql(String url, String user, String password, String schema) {
		return new MysqlProviderImpl(new ArrayList<>(), url, user, password, schema);
	}

	public SqlProvider sqlite() {
		return new SqliteProviderImpl(new ArrayList<>());
	}

	public SqlProvider sqlite(File file) {
		return new SqliteProviderImpl(new ArrayList<>(), file);
	}
}
