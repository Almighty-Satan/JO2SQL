package com.github.almightysatan.jo2sql.impl;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.almightysatan.jo2sql.AsyncDatabaseException;
import com.github.almightysatan.jo2sql.DatabaseAction;
import com.github.almightysatan.jo2sql.PreparedDelete;
import com.github.almightysatan.jo2sql.PreparedObjectDelete;
import com.github.almightysatan.jo2sql.PreparedReplace;
import com.github.almightysatan.jo2sql.PreparedSelect;
import com.github.almightysatan.jo2sql.SqlProvider;
import com.github.almightysatan.jo2sql.SqlSerializable;

public class SqlProviderImpl implements SqlProvider {

	private final String url;
	private final String user;
	private final String password;
	private final String schema;
	private final ExecutorService exectuor = Executors.newSingleThreadExecutor();
	private final Logger logger = LoggerFactory.getLogger("JO2SQL");
	private final Map<Class<?>, Table<?>> tables = new ConcurrentHashMap<>();
	private Connection connection;

	public SqlProviderImpl(String url, String user, String password, String schema) {
		this.url = url;
		this.user = user;
		this.password = password;
		this.schema = schema;
	}

	@Override
	public <T extends SqlSerializable> DatabaseAction<Void> createIfNecessary(Class<T> type) {
		return this.createDatabaseAction(() -> {
			this.getTable(type).createIfNecessary();
			return null;
		});
	}

	@Override
	public <T extends SqlSerializable> PreparedReplace<T, Void> prepareReplace(Class<T> type) {
		return this.getTable(type).prepareReplace();
	}

	@Override
	public <T extends SqlSerializable> PreparedReplace<T, Long> prepareAiReplace(Class<T> type) {
		return this.getTable(type).prepareAiReplace();
	}

	@Override
	public <T extends SqlSerializable> PreparedSelect<T> prepareSelect(Class<T> type, String... keys) {
		return this.getTable(type).prepareSingleSelect(keys);
	}

	@Override
	public <T extends SqlSerializable> PreparedSelect<T[]> prepareMultiSelect(Class<T> type, String... keys) {
		return this.getTable(type).prepareMultiSelect(keys);
	}

	@Override
	public <T extends SqlSerializable> PreparedObjectDelete<T> prepareObjectDelete(Class<T> type) {
		return this.getTable(type).prepareObjectDelete();
	}

	@Override
	public <T extends SqlSerializable> PreparedDelete prepareDelete(Class<T> type, String... keys) {
		return this.getTable(type).prepareDelete(keys);
	}

	@SuppressWarnings("unchecked")
	private <T extends SqlSerializable> Table<T> getTable(Class<T> type) {
		if (this.tables.containsKey(type))
			return (Table<T>) this.tables.get(type);
		else {
			try {
				Table<T> table = new Table<>(this, type);

				Optional<Table<?>> duplicate = this.tables.values().stream()
						.filter(t -> t.getName().equals(table.getName())).findAny();
				if (duplicate.isPresent())
					throw new Error(String.format("Duplicate table name in classes %s and %s",
							table.getType().getName(), duplicate.get().getType().getName()));
				this.tables.put(type, table);
				return table;
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new Error("Error while loading class", e);
			}
		}
	}

	<T> DatabaseAction<T> createDatabaseAction(ThrowableSupplier<T> action) {
		return new DatabaseAction<T>() {

			@Override
			public T complete() throws AsyncDatabaseException {
				Future<T> future = SqlProviderImpl.this.exectuor.submit(() -> {
					try {
						return action.run();
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				});
				try {
					return future.get();
				} catch (InterruptedException | ExecutionException e) {
					throw new AsyncDatabaseException(e);
				}
			}

			@Override
			public T completeUsafe() {
				try {
					return this.complete();
				} catch (AsyncDatabaseException e) {
					throw new Error("Async error", e);
				}
			}

			@Override
			public void queue(ExecutorService callbackExecutor, Consumer<T> success, Consumer<Throwable> error) {
				SqlProviderImpl.this.exectuor.execute(() -> {
					try {
						T result = action.run();
						if (success != null)
							callbackExecutor.execute(() -> success.accept(result));
					} catch (Throwable t) {
						if (error != null)
							callbackExecutor.execute(() -> error.accept(t));
						else
							SqlProviderImpl.this.logger.error("Mysql error", t);
					}
				});
			}

			@Override
			public void queue(ExecutorService callbackExecutor, Consumer<T> success) {
				this.queue(callbackExecutor, success, null);
			}

			@Override
			public void queue(Consumer<T> success, Consumer<Throwable> error) {
				SqlProviderImpl.this.exectuor.execute(() -> {
					SqlProviderImpl.this.exectuor.execute(() -> {
						try {
							T result = action.run();
							if (success != null)
								success.accept(result);
						} catch (Throwable t) {
							if (error != null)
								error.accept(t);
							else
								SqlProviderImpl.this.logger.error("Mysql error", t);
						}
					});
				});
			}

			@Override
			public void queue(Consumer<T> success) {
				this.queue(success, null);
			}

			@Override
			public void queue() {
				this.queue((Consumer<T>) null, null);
			}
		};
	}

	private Connection getValidConnection() throws SQLException {
		if (this.connection == null || !this.connection.isValid(30)) {
			if (this.connection != null) {
				this.logger.debug("Connection to database lost! Reconnecting...");
				try {
					this.connection.close();
				} catch (Throwable t) {
					// ignore
				}
			}
			this.connection = DriverManager.getConnection("jdbc:mysql://" + this.url + "?allowMultiQueries=true",
					this.user, this.password);
		}
		return this.connection;
	}

	CachedStatement prepareStatement(String sql) throws SQLException {
		this.logger.debug("Preparing statement {}", sql);
		return new CachedStatement(sql, (int) sql.chars().filter(c -> c == '?').count());
	}

	int executeUpdate(CachedStatement cachedStatement) throws SQLException {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this.getValidConnection());
		this.logger.debug("Executing prepared update statement {}", statement.toString());
		return statement.executeUpdate();
	}

	ResultSet executeQuery(CachedStatement cachedStatement) throws SQLException {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this.getValidConnection());
		this.logger.debug("Executing prepared query statement {}", statement.toString());
		return statement.executeQuery();
	}

	ResultSet executeMultiQuery(CachedStatement cachedStatement) throws SQLException {
		PreparedStatement statement = cachedStatement.getValidPreparedStatement(this.getValidConnection());
		this.logger.debug("Executing prepared query statement {}", statement.toString());
		statement.execute();
		if (statement.getMoreResults()) {
			return statement.getResultSet();
		} else
			throw new Error("Missing result");
	}

	int executeUpdate(String sql) throws SQLException {
		this.logger.debug("Executing update statement {}", sql);
		return this.getValidConnection().createStatement().executeUpdate(sql);
	}

	ResultSet executeQuery(String sql) throws SQLException {
		this.logger.debug("Executing query statement {}", sql);
		return this.getValidConnection().createStatement().executeQuery(sql);
	}

	String getSchema() {
		return this.schema;
	}

	Logger getLogger() {
		return this.logger;
	}
}
