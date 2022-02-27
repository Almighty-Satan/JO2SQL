package com.github.almightysatan.jo2sql;

public interface PreparedObjectDelete<T> {

	DatabaseAction<Void> object(T object);
}
