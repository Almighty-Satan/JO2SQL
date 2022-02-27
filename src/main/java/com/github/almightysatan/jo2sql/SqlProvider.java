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