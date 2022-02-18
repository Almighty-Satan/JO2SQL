package com.github.almightysatan.jo2sql.impl;

@FunctionalInterface
interface ThrowableSupplier<T> {

	T run() throws Throwable;
}
