package com.github.almightysatan.jo2sql;

import org.junit.jupiter.api.Test;

public class SqliteTest {

	public static void main(String[] args) throws AsyncDatabaseException {
		try {
			new SqliteTest().testApi();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Test
	public void testApi() throws AsyncDatabaseException {
		SqlProvider sql = new SqlBuilder().sqlite();

		CommonTest.testApi(sql);
	}
}
