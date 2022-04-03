package com.github.almightysatan.jo2sql.impl.mysql;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.github.almightysatan.jo2sql.DataType;
import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.Table;

public class MysqlProviderImpl extends SqlProviderImpl {

	static final DataType STRING_DATA_TYPE = new MysqlStringDataType();

	private final String url;
	private final String user;
	private final String password;
	private final String schema;

	public MysqlProviderImpl(List<DataType> types, String url, String user, String password, String schema) {
		super(types);
		types.add(STRING_DATA_TYPE);
		this.url = url;
		this.user = user;
		this.password = password;
		this.schema = schema;
	}

	@Override
	protected Connection createConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://" + this.url + "?allowMultiQueries=true", this.user,
				this.password);
	}

	@Override
	protected <T extends SqlSerializable> Table<T> newTable(Class<T> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		return new MysqlTable<>(this, type);
	}

	public String getSchema() {
		return this.schema;
	}
}
