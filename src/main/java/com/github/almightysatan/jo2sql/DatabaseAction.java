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

package com.github.almightysatan.jo2sql;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface DatabaseAction<T> {

	Future<T> queue(ExecutorService callbackExecutor, Consumer<T> success, Consumer<Throwable> error);

	default Future<T> queue(ExecutorService callbackExecutor, Consumer<T> success) {
		return this.queue(callbackExecutor, success, null);
	}

	default Future<T> queue(Consumer<T> success, Consumer<Throwable> error) {
		return this.queue(null, success, error);
	}

	default Future<T> queue(Consumer<T> success) {
		return this.queue(success, (Consumer<Throwable>) null);
	}

	default Future<T> queue() {
		return this.queue((Consumer<T>) null);
	}

	default T complete() throws InterruptedException, ExecutionException {
		return this.queue((Consumer<T>) null, (Consumer<Throwable>) throwable -> {
			throw new RuntimeException(throwable);
		}).get();
	}

	default T completeUnsafe() {
		try {
			return this.complete();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Async error", e);
		}
	}
}
