package com.github.almightysatan.jo2sql;

public interface PreparedDelete {

	DatabaseAction<Void> values(Object... values);
}
