package com.github.almightysatan.jo2sql.impl.sqlite;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.Table;

public class SqliteTable<T extends SqlSerializable> extends Table<T> {

	SqliteTable(SqliteProviderImpl provider, Class<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(provider, type);
	}

	@Override
	protected String getLastInsertIdFunc() {
		return "last_insert_rowid";
	}

	@Override
	protected void check() throws SQLException {
		if (!this.provider
				.executeQuery("SELECT * FROM sqlite_schema WHERE type='table' AND name = '" + this.name + "' LIMIT 1;")
				.next()) {
			// Table does not exist
			this.provider.getLogger().info("Creating table {}", this.name);

			StringBuilder statement = new StringBuilder().append("CREATE TABLE ").append(this.fullName).append(" (");
			boolean first = true;
			for (FieldColumn column : this.columns.values()) {
				if (first)
					first = false;
				else
					statement.append(",");
				column.appendColumn(statement);
				column.appendIndex(statement, ",");
			}
			if (!this.primaryKey.indexColumns.isEmpty())
				this.primaryKey.appendIndex(statement, ",");
			statement.append(");");

			this.provider.executeUpdate(statement.toString());
		} else {
			// Table exists
			// TODO implement this
		}
	}
}
