package com.github.almightysatan.jo2sql;

public interface PreparedSelect<T> {

	DatabaseAction<T> values(Object... values);
}
