package com.github.almightysatan.jo2sql.impl.sqlite;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.Table;

public class SqliteProviderImpl extends SqlProviderImpl {

	static final DataType STRING_DATA_TYPE = new SqliteStringDataType();

	private final String path;

	private SqliteProviderImpl(List<DataType> types, String path) {
		super(types);
		types.add(STRING_DATA_TYPE);
		this.path = path;

		this.createDatabaseAction(() -> {
			this.executeUpdate("PRAGMA encoding = 'UTF-8';");
			return null;
		}).queue();
	}

	public SqliteProviderImpl(List<DataType> types) {
		this(types, (String) null);
	}

	public SqliteProviderImpl(List<DataType> types, File file) {
		this(types, file.getAbsolutePath());
	}

	@Override
	protected Connection createConnection() throws SQLException {
		return this.path == null ? DriverManager.getConnection("jdbc:sqlite::memory:")
				: DriverManager.getConnection("jdbc:sqlite:" + this.path);
	}

	@Override
	protected <T extends SqlSerializable> Table<T> newTable(Class<T> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		return new SqliteTable<>(this, type);
	}
}
