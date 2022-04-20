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

package com.github.almightysatan.jo2sql.impl.mariadb;

import java.util.List;

import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.impl.mysql.MysqlProviderImpl;
import com.github.almightysatan.jo2sql.logger.Logger;

public class MariadbProviderImpl extends MysqlProviderImpl {

	static final DataType STRING_TYPE = new MariadbStringType();

	public MariadbProviderImpl(Logger logger, List<DataType> types, String url, String user, String password,
			String schema) {
		super(logger, types, url, user, password, schema);
	}

	@Override
	protected DataType getStringType() {
		return STRING_TYPE;
	}
}
