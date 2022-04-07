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
