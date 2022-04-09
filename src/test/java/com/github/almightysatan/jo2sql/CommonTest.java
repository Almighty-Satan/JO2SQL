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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.almightysatan.jo2sql.logger.CoutLogger;
import com.github.almightysatan.jo2sql.logger.Logger;

public class CommonTest {

	static final Logger LOGGER = new CoutLogger();

	public static void testApi(SqlProvider sql) {
		TestObject object = new TestObject("Hello World", true, 420);
		sql.prepareAiReplace(TestObject.class).object(object).queue();

		TestObject deserialized = sql.prepareSelect(TestObject.class, Selector.eq("string")).values(object.string)
				.completeUnsafe();

		LOGGER.info("" + object.equals(deserialized));
		assertEquals(object, deserialized);

		sql.terminate();
	}

	public static void testNestedObject(SqlProvider sql) {
		ChildChildObject childChild = new ChildChildObject("Massive", "Asshole", 12345);
		ChildObject child = new ChildObject("Little", "Prick", 69, childChild);
		ParentObject parent = new ParentObject(child);

		long id = sql.prepareAiReplace(ParentObject.class).object(parent).completeUnsafe();
		parent.id = id;

		ParentObject deserialized = sql.prepareSelect(ParentObject.class, Selector.eq("id")).values(id)
				.completeUnsafe();

		LOGGER.info("" + parent.equals(deserialized));
		assertEquals(parent, deserialized);

		sql.terminate();
	}
}
