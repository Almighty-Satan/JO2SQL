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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ApiTest {

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testReplaceSelect(SqlProvider sql) {
		TestObject object = new TestObject("Hello World", true, 420);
		sql.prepareAiReplace(TestObject.class).object(object).queue();

		TestObject deserialized = sql.prepareSelect(TestObject.class, Selector.eq("string")).values(object.string)
				.completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNestedObject(SqlProvider sql) {
		ChildChildObject childChild = new ChildChildObject("Massive", "Asshole", 12345);
		ChildObject child = new ChildObject("Little", "Prick", 69, childChild);
		ParentObject parent = new ParentObject(child);

		long id = sql.prepareAiReplace(ParentObject.class).object(parent).completeUnsafe();
		parent.id = id;

		ParentObject deserialized = sql.prepareSelect(ParentObject.class, Selector.eq("id")).values(id)
				.completeUnsafe();

		assertEquals(parent, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testInheritance(SqlProvider sql) {
		Subclass object = new Subclass("aa", "bb");
		object.id = sql.prepareAiReplace(Subclass.class).object(object).completeUnsafe();

		Subclass deserialized = sql.prepareSelect(Subclass.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testAi(SqlProvider sql) {
		ParentObject object = new ParentObject();
		long id0 = sql.prepareAiReplace(ParentObject.class).object(object).completeUnsafe();
		long id1 = sql.prepareAiReplace(ParentObject.class).object(object).completeUnsafe();
		long id2 = sql.prepareAiReplace(ParentObject.class).object(object).completeUnsafe();

		assertNotEquals(id0, id1);
		assertNotEquals(id1, id2);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testStringFuckery(SqlProvider sql) {
		TestObject object0 = new TestObject("Hello World   ", true, 69);
		sql.prepareAiReplace(TestObject.class).object(object0).queue();

		TestObject object1 = new TestObject("Hello World", false, 123);
		sql.prepareAiReplace(TestObject.class).object(object1).queue();

		TestObject deserialized = sql.prepareSelect(TestObject.class, Selector.eq("string")).values(object0.string)
				.completeUnsafe();

		assertEquals(object0, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testObjectDelete(SqlProvider sql) {
		TestObject object = new TestObject("DeleteMeDaddy", true, 666);
		sql.prepareAiReplace(TestObject.class).object(object).queue();
		sql.prepareObjectDelete(TestObject.class).object(object).queue();

		TestObject deserialized = sql.prepareSelect(TestObject.class, Selector.eq("string")).values(object.string)
				.completeUnsafe();

		assertNull(deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testStringMap(SqlProvider sql) {
		Map<String, String> map = new HashMap<>();
		map.put("abc", "Hello");
		map.put("def", "World");
		StringMapTest object = new StringMapTest(map);
		object.id = sql.prepareAiReplace(StringMapTest.class).object(object).completeUnsafe();

		StringMapTest deserialized = sql.prepareSelect(StringMapTest.class, Selector.eq("id")).values(object.id)
				.completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNullString(SqlProvider sql) {
		Subclass object = new Subclass(null, null);
		object.id = sql.prepareAiReplace(Subclass.class).object(object).completeUnsafe();

		Subclass deserialized = sql.prepareSelect(Subclass.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNullMap(SqlProvider sql) {
		StringMapTest object = new StringMapTest(null);
		object.id = sql.prepareAiReplace(StringMapTest.class).object(object).completeUnsafe();

		StringMapTest deserialized = sql.prepareSelect(StringMapTest.class, Selector.eq("id")).values(object.id)
				.completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNullMapEntry(SqlProvider sql) {
		Map<String, String> map = new HashMap<>();
		map.put("abc", "Hello");
		map.put("def", null);
		StringMapTest object = new StringMapTest(map);
		object.id = sql.prepareAiReplace(StringMapTest.class).object(object).completeUnsafe();

		StringMapTest deserialized = sql.prepareSelect(StringMapTest.class, Selector.eq("id")).values(object.id)
				.completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testEmptyMap(SqlProvider sql) {
		StringMapTest object = new StringMapTest(new HashMap<>());
		object.id = sql.prepareAiReplace(StringMapTest.class).object(object).completeUnsafe();

		StringMapTest deserialized = sql.prepareSelect(StringMapTest.class, Selector.eq("id")).values(object.id)
				.completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	private static Stream<Arguments> getSqlProviders() {
		if (System.getenv("mysqlUrl") == null)
			return Stream.of(Arguments.of(new SqlBuilder().sqlite()));
		else
			return Stream.of(Arguments.of(new SqlBuilder().sqlite()),
					Arguments.of(new SqlBuilder().mysql(System.getenv("mysqlUrl"), System.getenv("mysqlUser"),
							System.getenv("mysqlPassword"), "jo2sqlTest")));
	}
}
