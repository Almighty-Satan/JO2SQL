package com.github.almightysatan.jo2sql;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface DatabaseAction<T> {

	T complete() throws AsyncDatabaseException;

	T completeUsafe();

	void queue(ExecutorService callbackExecutor, Consumer<T> success, Consumer<Throwable> error);

	void queue(ExecutorService callbackExecutor, Consumer<T> success);

	void queue(Consumer<T> success, Consumer<Throwable> error);

	void queue(Consumer<T> success);

	void queue();
}
