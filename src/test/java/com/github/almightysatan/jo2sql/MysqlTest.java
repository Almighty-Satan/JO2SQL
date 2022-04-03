package com.github.almightysatan.jo2sql;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MysqlTest {

	public static void main(String[] args) throws AsyncDatabaseException {
		try {
			new MysqlTest().testApi();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Test
	public void testApi() throws AsyncDatabaseException {
		if (System.getenv("mysqlUrl") == null) {
			CommonTest.LOGGER.info("Skipped Mysql");
			assertTrue(true);
			return;
		}

		SqlProvider sql = new SqlBuilder().mysql(System.getenv("mysqlUrl"), System.getenv("mysqlUser"),
				System.getenv("mysqlPassword"), "test");

		CommonTest.testApi(sql);
	}
}
