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

import java.sql.ResultSet;

public class SerializableMap implements SerializableObject<MapEntry> {

	static final String ID_COLUMN_NAME = "id";
	static final String KEY_COLUMN_NAME = "key";
	static final String VALUE_COLUMN_NAME = "value";

	private final String name;
	private final SerializableAttribute idAttribute;
	private final SerializableAttribute keyAttribute;
	private final SerializableAttribute valueAttribute;
	private final SerializableAttribute attributes[];
	private final PrimaryKey primaryKey;

	public SerializableMap(SqlProviderImpl provider, String name, Class<?> key, int keySize, Class<?> value,
			int valueSize) throws Throwable {
		this.name = name;
		this.idAttribute = new SimpleSerializableAttribute(provider.getAiLongType(), name, ID_COLUMN_NAME, -1);
		this.keyAttribute = provider.createSerializableAttribute(key, name, KEY_COLUMN_NAME, keySize);
		this.valueAttribute = provider.createSerializableAttribute(value, VALUE_COLUMN_NAME, valueSize, false, this);
		this.attributes = new SerializableAttribute[] { this.idAttribute, this.keyAttribute, this.valueAttribute };
		this.primaryKey = new PrimaryKey(this.idAttribute, this.keyAttribute);
	}

	@Override
	public int serialize(CachedStatement statement, int startIndex, MapEntry value, ResultSet prevValues)
			throws Throwable {
		startIndex += this.idAttribute.serialize(statement, startIndex, value.getId(), null);
		startIndex += this.keyAttribute.serialize(statement, startIndex, value.getKey(), null);
		startIndex += this.valueAttribute.serialize(statement, startIndex, value.getValue(), null);
		return startIndex;
	}

	@Override
	public MapEntry deserialize(String prefix, ResultSet result) throws Throwable {
		Object key = this.keyAttribute.deserialize("", result);
		Object value = this.valueAttribute.deserialize("", result);
		return new MapEntry(key, value);
	}

	@Override
	public boolean needsPrevValue() {
		return false;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Class<MapEntry> getType() {
		return MapEntry.class;
	}

	@Override
	public SerializableAttribute[] getAttributes() {
		return this.attributes;
	}

	@Override
	public SerializableAttribute[] getAttributes(String... keys) {
		// I am aware that this is not a great implementation but lets hope this method
		// is never actually invoked :)
		SerializableAttribute[] attributes = new SerializableAttribute[keys.length];
		loop: for (int i = 0; i < keys.length; i++) {
			for (SerializableAttribute attribute : this.getAttributes())
				if (attribute.getColumnName().equals(keys[i])) {
					attributes[i] = attribute;
					continue loop;
				}
			throw new Error(String.format("Unknown key '%s' for table %s", keys[i], this.name));
		}
		return attributes;
	}

	@Override
	public PrimaryKey getPrimaryKey() {
		return this.primaryKey;
	}
}
