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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.almightysatan.jo2sql.Types.TestEnum;
import com.github.almightysatan.jo2sql.Types.TestIdEnum;

public class ApiTest {

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testReplaceSelect(SqlProvider sql) {
		TestObject object = new TestObject("Hello World", true, 420);
		sql.replace(TestObject.class).object(object).queue();

		TestObject deserialized = sql.select(TestObject.class, Selector.eq("string")).values(object.string)
				.completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testTypes(SqlProvider sql) {
		Types object = new Types(UUID.randomUUID(), TestEnum.DEF, TestIdEnum.DEF);
		sql.replace(Types.class).object(object).queue();

		Types deserialized = sql.select(Types.class, Selector.eq("uuid")).values(object.uuid).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNestedObject(SqlProvider sql) {
		ChildChildObject childChild = new ChildChildObject("Massive", "Asshole", 12345);
		ChildObject child = new ChildObject("Little", "Prick", 69, childChild);
		ParentObject parent = new ParentObject(child);

		long id = sql.replaceAi(ParentObject.class).object(parent).completeUnsafe();
		parent.id = id;

		ParentObject deserialized = sql.select(ParentObject.class, Selector.eq("id")).values(id).completeUnsafe();

		assertEquals(parent, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testInheritance(SqlProvider sql) {
		Subclass object = new Subclass("aa", "bb");
		object.id = sql.replaceAi(Subclass.class).object(object).completeUnsafe();

		Subclass deserialized = sql.select(Subclass.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testAi(SqlProvider sql) {
		ParentObject object = new ParentObject();
		long id0 = sql.replaceAi(ParentObject.class).object(object).completeUnsafe();
		long id1 = sql.replaceAi(ParentObject.class).object(object).completeUnsafe();
		long id2 = sql.replaceAi(ParentObject.class).object(object).completeUnsafe();

		assertNotEquals(id0, id1);
		assertNotEquals(id1, id2);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testStringFuckery(SqlProvider sql) {
		TestObject object0 = new TestObject("Hello World   ", true, 69);
		sql.replaceAi(TestObject.class).object(object0).queue();

		TestObject object1 = new TestObject("Hello World", false, 123);
		sql.replaceAi(TestObject.class).object(object1).queue();

		TestObject deserialized = sql.select(TestObject.class, Selector.eq("string")).values(object0.string)
				.completeUnsafe();

		assertEquals(object0, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testObjectDelete(SqlProvider sql) {
		TestObject object = new TestObject("DeleteMeDaddy", true, 666);
		sql.replace(TestObject.class).object(object).queue();
		sql.deleteObject(TestObject.class).object(object).queue();

		TestObject deserialized = sql.select(TestObject.class, Selector.eq("string")).values(object.string)
				.completeUnsafe();

		assertNull(deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNestedDeleteEnabled(SqlProvider sql) {
		ParentObject object = new ParentObject(new ChildObject("DeleteTestFirst", "DeleteTestLast", 1337,
				new ChildChildObject("DeleteTestTestFirst", "DeleteTestTestFirst", 0__0)));

		object.id = sql.replaceAi(ParentObject.class).object(object).completeUnsafe();
		sql.deleteObject(ParentObject.class).overwriteNestedObjects(true).object(object).queue();

		ParentObject deserialized0 = sql.selectPrimary(ParentObject.class).values(object.id).completeUnsafe();
		ChildObject deserialized1 = sql.selectPrimary(ChildObject.class)
				.values(object.child.firstName, object.child.lastName).completeUnsafe();
		ChildChildObject deserialized2 = sql.selectPrimary(ChildChildObject.class)
				.values(object.child.child.firstName, object.child.child.lastName).completeUnsafe();

		assertNull(deserialized0);
		assertNull(deserialized1);
		assertNull(deserialized2);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNestedDeleteDisabled(SqlProvider sql) {
		ChildObject child = new ChildObject("DeleteTestFirst", "DeleteTestLast", 1337,
				new ChildChildObject("DeleteTestTestFirst", "DeleteTestTestFirst", 0__0));
		ParentObject object = new ParentObject(child);

		object.id = sql.replaceAi(ParentObject.class).object(object).completeUnsafe();
		sql.deleteObject(ParentObject.class).object(object).queue();

		ParentObject deserialized0 = sql.selectPrimary(ParentObject.class).values(object.id).completeUnsafe();
		ChildObject deserialized1 = sql.selectPrimary(ChildObject.class)
				.values(object.child.firstName, object.child.lastName).completeUnsafe();
		ChildChildObject deserialized2 = sql.selectPrimary(ChildChildObject.class)
				.values(object.child.child.firstName, object.child.child.lastName).completeUnsafe();

		assertNull(deserialized0);
		assertEquals(child, deserialized1);
		assertEquals(child.child, deserialized2);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNestedOverwriteEnabled(SqlProvider sql) {
		String oldFirstName = "OverwriteTestTestFirst";
		ParentObject object = new ParentObject(new ChildObject("OverwriteTestFirst", "OverwriteTestLast", 1337,
				new ChildChildObject(oldFirstName, "OverwriteTestTestFirst", 3)));

		object.id = sql.replaceAi(ParentObject.class).object(object).completeUnsafe();

		object.child.child.firstName = "OverwriteTestTestNewFirst";
		object.child.child.age++;

		sql.replace(ParentObject.class).overwriteNestedObjects().object(object).queue();

		ChildChildObject deserializedOld = sql.selectPrimary(ChildChildObject.class)
				.values(oldFirstName, object.child.child.lastName).completeUnsafe();
		ChildChildObject deserializedNew = sql.selectPrimary(ChildChildObject.class)
				.values(object.child.child.firstName, object.child.child.lastName).completeUnsafe();

		assertNull(deserializedOld);
		assertEquals(object.child.child, deserializedNew);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNestedOverwriteDisabled(SqlProvider sql) {
		ChildChildObject oldChild = new ChildChildObject("OverwriteTestTestFirst", "OverwriteTestTestFirst", 5);
		ParentObject object = new ParentObject(
				new ChildObject("OverwriteTestFirst", "OverwriteTestLast", 1337, oldChild));

		object.id = sql.replaceAi(ParentObject.class).object(object).completeUnsafe();

		object.child.child = new ChildChildObject("OverwriteTestTestNewFirst", "OverwriteTestTestNewLast", 6);

		sql.replace(ParentObject.class).object(object).queue();

		ChildChildObject deserializedOld = sql.selectPrimary(ChildChildObject.class)
				.values(oldChild.firstName, oldChild.lastName).completeUnsafe();
		ChildChildObject deserializedNew = sql.selectPrimary(ChildChildObject.class)
				.values(object.child.child.firstName, object.child.child.lastName).completeUnsafe();

		assertEquals(oldChild, deserializedOld);
		assertEquals(object.child.child, deserializedNew);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testStringMap(SqlProvider sql) {
		Map<String, String> map = new HashMap<>();
		map.put("abc", "Hello");
		map.put("def", "World");
		StringMap object = new StringMap(map);
		object.id = sql.replaceAi(StringMap.class).object(object).completeUnsafe();

		StringMap deserialized = sql.select(StringMap.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNestedClassMap(SqlProvider sql) {
		Map<String, ChildChildObject> map = new HashMap<>();
		map.put("abc", new ChildChildObject("John", "Doe", 69));
		map.put("def", new ChildChildObject("Jane", "Doe", 69));
		NestedObjectMap object = new NestedObjectMap(map);
		object.id = sql.replaceAi(NestedObjectMap.class).object(object).completeUnsafe();

		NestedObjectMap deserialized = sql.select(NestedObjectMap.class, Selector.eq("id")).values(object.id)
				.completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testStringMapGC(SqlProvider sql) {
		Map<String, String> map = new HashMap<>();
		map.put("ghi", "Hi");
		map.put("jkl", "Earth");
		StringMap object = new StringMap(map);
		object.id = sql.replaceAi(StringMap.class).object(object).completeUnsafe();

		sql.replace(StringMap.class).object(object).completeUnsafe();

		StringMap deserialized = sql.select(StringMap.class, Selector.eq("id")).values(object.id).completeUnsafe();

		// TODO test if the old rows are actually deleted (right now this test is just
		// useful for debugging)
		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testMapEntryDelete(SqlProvider sql) {
		Map<String, String> map = new HashMap<>();
		map.put("0123", "Hello");
		map.put("01234", "World");
		StringMap object = new StringMap(map);
		object.id = sql.replaceAi(StringMap.class).object(object).completeUnsafe();

		map.remove("01234");

		sql.replace(StringMap.class).object(object).completeUnsafe();

		StringMap deserialized = sql.select(StringMap.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testList(SqlProvider sql) {
		List<ChildChildObject> list = new ArrayList<>();
		list.add(new ChildChildObject("004", "005", 4862596));
		list.add(new ChildChildObject("006", "007", 4862596));
		NestedObjectList object = new NestedObjectList(list);
		object.id = sql.replaceAi(NestedObjectList.class).object(object).completeUnsafe();

		NestedObjectList deserialized = sql.select(NestedObjectList.class, Selector.eq("id")).values(object.id)
				.completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNullString(SqlProvider sql) {
		Subclass object = new Subclass(null, null);
		object.id = sql.replaceAi(Subclass.class).object(object).completeUnsafe();

		Subclass deserialized = sql.select(Subclass.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNullMap(SqlProvider sql) {
		StringMap object = new StringMap(null);
		object.id = sql.replaceAi(StringMap.class).object(object).completeUnsafe();

		StringMap deserialized = sql.select(StringMap.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testNullMapEntry(SqlProvider sql) {
		Map<String, String> map = new HashMap<>();
		map.put("abc", "Hello");
		map.put("def", null);
		StringMap object = new StringMap(map);
		object.id = sql.replaceAi(StringMap.class).object(object).completeUnsafe();

		StringMap deserialized = sql.select(StringMap.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testEmptyMap(SqlProvider sql) {
		StringMap object = new StringMap(new HashMap<>());
		object.id = sql.replaceAi(StringMap.class).object(object).completeUnsafe();

		StringMap deserialized = sql.select(StringMap.class, Selector.eq("id")).values(object.id).completeUnsafe();

		assertEquals(object, deserialized);

		sql.terminate();
	}

	@ParameterizedTest
	@MethodSource("getSqlProviders")
	public void testInvalidSelect(SqlProvider sql) {
		assertThrows(Error.class, () -> sql.select(TestObject.class, Selector.eq("\"")).values("abc").completeUnsafe());
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
