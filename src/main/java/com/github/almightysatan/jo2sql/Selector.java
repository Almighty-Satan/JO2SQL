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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Selector {

	private static final String EQ = "=";
	private static final String NEQ = "!=";
	private static final String CMPL = "<";
	private static final String CMPG = ">";
	private static final String CMPLE = "<=";
	private static final String CMPGE = ">=";
	private static final String LIKE = "LIKE";
	private static final String AND = " AND ";
	private static final String OR = " OR ";

	private String command;
	private String[] keys;

	private Selector(String command, String... keys) {
		this.command = command;
		this.keys = keys;
	}

	public static Selector eq(String key) {
		return keyOp(EQ, key);
	}

	public static Selector eqAnd(String... keys) {
		return multiKeyOp(EQ, AND, keys);
	}

	public static Selector eqOr(String... keys) {
		return multiKeyOp(EQ, OR, keys);
	}

	public static Selector neq(String key) {
		return keyOp(NEQ, key);
	}

	public static Selector cmpl(String key) {
		return keyOp(CMPL, key);
	}

	public static Selector cmpg(String key) {
		return keyOp(CMPG, key);
	}

	public static Selector cmple(String key) {
		return keyOp(CMPLE, key);
	}

	public static Selector cmpge(String key) {
		return keyOp(CMPGE, key);
	}

	public static Selector like(String key) {
		return keyOp(LIKE, key);
	}

	public static Selector and(Selector... selectors) {
		return selectorOp(AND, selectors);
	}

	public static Selector or(Selector... selectors) {
		return selectorOp(OR, selectors);
	}

	private static Selector keyOp(String keyOperation, String key) {
		return new Selector("`" + key + "` " + keyOperation + " ?", key);
	}

	private static Selector multiKeyOp(String keyOperation, String logicOperator, String... keys) {
		if (keys.length == 0)
			throw new IllegalArgumentException();
		else {
			StringBuilder stringBuilder = new StringBuilder().append("`").append(keys[0]).append("`")
					.append(keyOperation).append("?");
			for (int i = 1; i < keys.length; i++)
				stringBuilder.append(logicOperator).append("`").append(keys[i]).append("`").append(keyOperation)
						.append("?");
			return new Selector(stringBuilder.toString(), keys);
		}
	}

	private static Selector selectorOp(String logicOperator, Selector... selectors) {
		if (selectors.length == 0)
			throw new IllegalArgumentException();
		else {
			List<String> keys = new ArrayList<>();
			StringBuilder stringBuilder = new StringBuilder().append("(").append(selectors[0].command).append(")");
			keys.addAll(Arrays.asList(selectors[0].keys));
			for (int i = 1; i < selectors.length; i++) {
				stringBuilder.append(logicOperator).append("(").append(selectors[i].command).append(")");
				keys.addAll(Arrays.asList(selectors[i].keys));
			}
			return new Selector(stringBuilder.toString(), keys.toArray(new String[keys.size()]));
		}
	}

	public String getCommand() {
		return this.command;
	}

	public String[] getKeys() {
		return this.keys;
	}
}
