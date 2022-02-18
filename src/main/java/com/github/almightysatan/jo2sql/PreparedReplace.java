package com.github.almightysatan.jo2sql;

public interface PreparedReplace<T, R> {

	DatabaseAction<R> object(T value);
}
