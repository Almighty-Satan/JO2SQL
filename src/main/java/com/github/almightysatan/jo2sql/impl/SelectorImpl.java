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

package com.github.almightysatan.jo2sql.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.almightysatan.jo2sql.Selector;

public final class SelectorImpl implements Selector {

	public static final String EQ = "=";
	public static final String NEQ = "!=";
	public static final String CMPL = "<";
	public static final String CMPG = ">";
	public static final String CMPLE = "<=";
	public static final String CMPGE = ">=";
	public static final String LIKE = " LIKE ";
	public static final String AND = " AND ";
	public static final String OR = " OR ";

	private String command;
	private String[] keys;

	public SelectorImpl(String command, String... keys) {
		this.command = command;
		this.keys = keys;
	}

	public String getCommand() {
		return this.command;
	}

	public String[] getKeys() {
		return this.keys;
	}

	public static Selector keyOp(String keyOperation, String key) {
		return new SelectorImpl("`" + key + "` " + keyOperation + " ?", key);
	}

	public static SelectorImpl multiKeyOp(String keyOperation, String logicOperator, String... keys) {
		if (keys.length == 0)
			throw new IllegalArgumentException();
		else {
			StringBuilder stringBuilder = new StringBuilder().append("`").append(keys[0]).append("`")
					.append(keyOperation).append("?");
			for (int i = 1; i < keys.length; i++)
				stringBuilder.append(logicOperator).append("`").append(keys[i]).append("`").append(keyOperation)
						.append("?");
			return new SelectorImpl(stringBuilder.toString(), keys);
		}
	}

	public static SelectorImpl selectorOp(String logicOperator, SelectorImpl... selectors) {
		if (selectors.length == 0)
			throw new IllegalArgumentException();
		else {
			List<String> keys = new ArrayList<>();
			StringBuilder stringBuilder = new StringBuilder().append("(").append(selectors[0].getCommand()).append(")");
			keys.addAll(Arrays.asList(selectors[0].getKeys()));
			for (int i = 1; i < selectors.length; i++) {
				stringBuilder.append(logicOperator).append("(").append(selectors[i].getCommand()).append(")");
				keys.addAll(Arrays.asList(selectors[i].getKeys()));
			}
			return new SelectorImpl(stringBuilder.toString(), keys.toArray(new String[keys.size()]));
		}
	}
}
