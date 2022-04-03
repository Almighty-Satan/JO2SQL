package com.github.almightysatan.jo2sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonTest {

	static final Logger LOGGER = LoggerFactory.getLogger("JO2SQL_TEST");

	public static void testApi(SqlProvider sql) throws AsyncDatabaseException {
		TestObject object = new TestObject("Hello World", true, 420);
		sql.prepareAiReplace(TestObject.class).object(object).queue();

		TestObject deserialized = sql.prepareSelect(TestObject.class, "string").values(object.string).complete();

		LOGGER.info("" + object.equals(deserialized));
		assertEquals(object, deserialized);
	}
}
