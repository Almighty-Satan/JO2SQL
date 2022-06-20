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

package com.github.almightysatan.jo2sql.impl.attributes;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.github.almightysatan.jo2sql.impl.MapEntry;
import com.github.almightysatan.jo2sql.impl.SerializableObject;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;

public class SerializableMapAttribute extends AbstractSerializableMapAttribute {

	public SerializableMapAttribute(SqlProviderImpl provider, Class<?> type, Class<?> keyType, int keySize,
			Class<?> valueType, int valueSize, String columnName, SerializableObject<?> parentObject) throws Throwable {
		super(provider, type, keyType, keySize, valueType, valueSize, columnName, parentObject);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Collection<Entry<?, ?>> getEntries(Object value) {
		return ((Map) value).entrySet();
	}

	@Override
	protected Object parseValue(MapEntry[] entries) throws InstantiationException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		Map<Object, Object> instance = (Map<Object, Object>) this.type.newInstance();
		for (MapEntry entry : entries)
			instance.put(entry.getKey(), entry.getValue());
		return instance;
	}
}
