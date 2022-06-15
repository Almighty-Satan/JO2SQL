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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public class SerializableListAttribute extends AbstractSerializableMapAttribute {

	public SerializableListAttribute(SqlProviderImpl provider, Class<?> type, Class<?> valueType, int valueSize,
			String columnName, SerializableObject<?> parentObject) throws Throwable {
		super(provider, type, int.class, -1, valueType, valueSize, columnName, parentObject);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Collection<Entry<?, ?>> getEntries(Object value) {
		List<?> list = (List<?>) value;
		Collection<Entry<?, ?>> entrys = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			final int index = i;
			entrys.add(new Entry() {

				@Override
				public Object getKey() {
					return index;
				}

				@Override
				public Object getValue() {
					return list.get(index);
				}

				@Override
				public Object setValue(Object value) {
					throw new UnsupportedOperationException();
				}
			});
		}

		return entrys;
	}

	@Override
	protected Object parseValue(MapEntry[] entries) throws InstantiationException, IllegalAccessException {
		Object[] values = new Object[entries.length];
		for (MapEntry entry : entries)
			values[(int) entry.getKey()] = entry.getValue();

		@SuppressWarnings("unchecked")
		List<Object> instance = (List<Object>) this.type.newInstance();
		instance.addAll(Arrays.asList(values));
		return instance;
	}
}
