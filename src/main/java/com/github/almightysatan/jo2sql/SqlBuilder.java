package com.github.almightysatan.jo2sql;

import java.io.File;
import java.util.ArrayList;

import com.github.almightysatan.jo2sql.impl.mysql.MysqlProviderImpl;
import com.github.almightysatan.jo2sql.impl.sqlite.SqliteProviderImpl;

public class SqlBuilder {

	public SqlProvider mysql(String url, String user, String password, String schema) {
		return new MysqlProviderImpl(new ArrayList<>(), url, user, password, schema);
	}

	public SqlProvider sqlite() {
		return new SqliteProviderImpl(new ArrayList<>());
	}

	public SqlProvider sqlite(File file) {
		return new SqliteProviderImpl(new ArrayList<>(), file);
	}
}
