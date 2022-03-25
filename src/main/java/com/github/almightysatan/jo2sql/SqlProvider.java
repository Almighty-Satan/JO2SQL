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

import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;

public interface SqlProvider {

	<T extends SqlSerializable> DatabaseAction<Void> createIfNecessary(Class<T> type);

	<T extends SqlSerializable> PreparedReplace<T, Void> prepareReplace(Class<T> type);

	<T extends SqlSerializable> PreparedReplace<T, Long> prepareAiReplace(Class<T> type);

	<T extends SqlSerializable> PreparedSelect<T> prepareSelect(Class<T> type, String... keys);

	<T extends SqlSerializable> PreparedSelect<T[]> prepareMultiSelect(Class<T> type, String... keys);

	<T extends SqlSerializable> PreparedObjectDelete<T> prepareObjectDelete(Class<T> type);

	<T extends SqlSerializable> PreparedDelete prepareDelete(Class<T> type, String... keys);

	public static SqlProvider newInstance(String url, String user, String password, String schema) {
		return new SqlProviderImpl(url, user, password, schema);
	}
}