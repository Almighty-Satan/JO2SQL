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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;

import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.Selector;

public class SerializableMapEntryAttribute<T extends Map<?, ?>> implements SerializableAttribute {

	private final SqlProviderImpl provider;
	private final Class<T> type;
	private ColumnData[] columnData;
	private TableImpl<MapEntry> table;
	private SerializableObject<?> parentObject;
	private TableImpl<?> parent;
	private PreparedReplace<MapEntry, Long> replace;
	private PreparedSelect<MapEntry[]> primarySelect;
	private String columnName;

	public SerializableMapEntryAttribute(SqlProviderImpl provider, Class<T> type, Class<?> keyType, int keySize,
			Class<?> valueType, int valueSize, String columnName, SerializableObject<?> parentObject) throws Throwable {
		this.provider = provider;
		this.type = type;
		this.columnName = columnName;
		this.parentObject = parentObject;
		this.table = this.provider.newTable(
				new SerializableMap(this.provider, parentObject.getName() + INTERNAL_COLUMN_DELIMITER + this.columnName,
						keyType, keySize, valueType, valueSize));
		this.replace = this.table.prepareAiReplace();
		this.primarySelect = this.table.prepareMultiSelect(Selector.eq(SerializableMap.ID_COLUMN_NAME));

		this.columnData = new ColumnData[] {
				new ColumnData(this.getColumnName(), SqlProviderImpl.LONG_TYPE.getSqlType(-1),
						SqlProviderImpl.LONG_TYPE.getPreparedReplaceSql(columnName, parentObject.getName())) };
	}

	@Override
	public void appendIndex(StringBuilder builder, String delimiter) {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void serialize(PreparedStatement statement, int startIndex, Object value) throws Throwable {
		if (this.parent == null)
			this.parent = this.provider.getOrCreateTable((Class) this.parentObject.getType());

		// TODO Delete old (now unused) data
		long mapId;
		if (value == null)
			mapId = -1;
		else {
			mapId = 0;
			for (Entry<?, ?> entry : ((T) value).entrySet()) {
				long lastInsertId = this.provider
						.runDatabaseAction(this.replace.object(new MapEntry(mapId, entry.getKey(), entry.getValue())));
				if (mapId == 0)
					mapId = lastInsertId;
			}
		}

		SqlProviderImpl.LONG_TYPE.serialize(statement, startIndex, mapId);
	}

	@Override
	public Object deserialize(String prefix, ResultSet result) throws Throwable {
		long mapId = (long) SqlProviderImpl.LONG_TYPE.deserialize(prefix + this.columnName, result);
		if (mapId != -1) {
			@SuppressWarnings("unchecked")
			Map<Object, Object> instance = (Map<Object, Object>) this.type.newInstance();
			for (MapEntry entry : this.provider.runDatabaseAction(this.primarySelect.values(mapId)))
				instance.put(entry.getKey(), entry.getValue());
			return instance;
		} else
			return null;
	}

	@Override
	public String getColumnName() {
		return this.columnName;
	}

	@Override
	public ColumnData[] getColumnData() {
		return this.columnData;
	}
}
