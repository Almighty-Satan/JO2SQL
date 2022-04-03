package com.github.almightysatan.jo2sql.impl.mysql;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import com.github.almightysatan.jo2sql.SqlSerializable;
import com.github.almightysatan.jo2sql.impl.CachedStatement;
import com.github.almightysatan.jo2sql.impl.Table;

public class MysqlTable<T extends SqlSerializable> extends Table<T> {

	MysqlTable(MysqlProviderImpl provider, Class<T> type) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		super(provider, type);
	}

	@Override
	protected String getFullName(String name) {
		return "`" + ((MysqlProviderImpl) this.provider).getSchema() + "`.`" + this.name + "`";
	}

	@Override
	protected String getLastInsertIdFunc() {
		return "LAST_INSERT_ID";
	}

	@Override
	protected void check() throws SQLException {
		String schema = ((MysqlProviderImpl) this.provider).getSchema();

		if (!this.provider.executeQuery("SELECT * FROM information_schema.tables WHERE table_schema = '" + schema
				+ "' AND table_name = '" + this.name + "' LIMIT 1;").next()) {
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
			CachedStatement checkRowStatement = this.provider.prepareStatement(
					"SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1;");

			checkRowStatement.setParameter(0, MysqlProviderImpl.STRING_DATA_TYPE, schema);
			checkRowStatement.setParameter(1, MysqlProviderImpl.STRING_DATA_TYPE, this.name);

			for (FieldColumn column : this.columns.values()) {
				checkRowStatement.setParameter(2, MysqlProviderImpl.STRING_DATA_TYPE, column.getName());

				if (!this.provider.executeQuery(checkRowStatement).next()) {
					// Column does not exist
					this.provider.getLogger().info("Adding column {} to tabel {}", column.getName(), this.name);

					StringBuilder statement = new StringBuilder();
					statement.append("ALTER TABLE `").append(schema).append("`.`").append(this.name).append("`")
							.append(" ADD ");
					column.appendColumn(statement);
					// TODO properly implement this
					// column.appendIndex(statement, ",ADD ");
					// this.primaryKey.appendIndex(statement, ",ADD ");
					statement.append(";");

					this.provider.executeUpdate(statement.toString());
				}
			}
		}
	}
}
