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
import java.util.Collection;
import java.util.Map.Entry;

import com.github.almightysatan.jo2sql.PreparedDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.Selector;

public abstract class AbstractSerializableMapAttribute implements SerializableAttribute {

	private final SqlProviderImpl provider;
	protected final Class<?> type;
	private ColumnData[] columnData;
	private TableImpl<MapEntry> table;
	private SerializableObject<?> parentObject;
	private TableImpl<?> parentTable;
	private PreparedReplace<MapEntry, Long> replace;
	private PreparedDelete delete;
	private PreparedSelect<MapEntry[]> select;
	private String columnName;

	public AbstractSerializableMapAttribute(SqlProviderImpl provider, Class<?> type, Class<?> keyType, int keySize,
			Class<?> valueType, int valueSize, String columnName, SerializableObject<?> parentObject) throws Throwable {
		this.provider = provider;
		this.type = type;
		this.columnName = columnName;
		this.parentObject = parentObject;
		this.table = this.provider.newTable(
				new SerializableMap(this.provider, parentObject.getName() + INTERNAL_COLUMN_DELIMITER + this.columnName,
						keyType, keySize, valueType, valueSize));
		this.replace = this.table.prepareAiReplace();
		SelectorImpl idSelector = (SelectorImpl) Selector.eq(SerializableMap.ID_COLUMN_NAME);
		this.delete = this.table.prepareDelete(idSelector);
		this.select = this.table.prepareMultiSelect(idSelector, 0, Integer.MAX_VALUE);

		this.columnData = new ColumnData[] {
				new ColumnData(this.getColumnName(), SqlProviderImpl.LONG_TYPE.getSqlType(-1),
						SqlProviderImpl.LONG_TYPE.getPreparedReplaceSql(columnName, parentObject.getName())) };
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int serialize(CachedStatement statement, int startIndex, Object value, ResultSet prevValues)
			throws Throwable {
		if (this.parentTable == null)
			this.parentTable = this.provider.getOrCreateTable((Class) this.parentObject.getType());

		if (prevValues != null)
			// This doesn't have to be executed immediately
			this.delete.values(SqlProviderImpl.LONG_TYPE.deserialize(this.columnName, prevValues)).queue();

		long mapId;
		if (value == null)
			mapId = -1;
		else {
			mapId = 0;
			for (Entry<?, ?> entry : this.getEntries(value)) {
				long lastInsertId = this.provider
						.runDatabaseAction(this.replace.object(new MapEntry(mapId, entry.getKey(), entry.getValue())));
				if (mapId == 0)
					mapId = lastInsertId;
			}
		}

		statement.setParameter(startIndex, SqlProviderImpl.LONG_TYPE, mapId);
		return 1; // This attribute only needs one column
	}

	protected abstract Collection<Entry<?, ?>> getEntries(Object value);

	@Override
	public Object deserialize(String prefix, ResultSet result) throws Throwable {
		long mapId = (long) SqlProviderImpl.LONG_TYPE.deserialize(prefix + this.columnName, result);
		return mapId == -1 ? null : this.parseValue(this.provider.runDatabaseAction(this.select.values(mapId)));
	}

	protected abstract Object parseValue(MapEntry[] entries) throws InstantiationException, IllegalAccessException;

	@Override
	public void deleteNested(Object value) throws Throwable {
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public String getColumnName() {
		return this.columnName;
	}

	@Override
	public ColumnData[] getColumnData() {
		return this.columnData;
	}

	@Override
	public Index[] getIndices() {
		return new Index[0];
	}

	@Override
	public boolean needsPrevValue() {
		return true;
	}
}
