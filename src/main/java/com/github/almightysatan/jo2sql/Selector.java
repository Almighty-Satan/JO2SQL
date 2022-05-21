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

import com.github.almightysatan.jo2sql.impl.SelectorImpl;

public interface Selector {

	static Selector eq(String key) {
		return SelectorImpl.keyOp(SelectorImpl.EQ, key);
	}

	static Selector eqAnd(String... keys) {
		return SelectorImpl.multiKeyOp(SelectorImpl.EQ, SelectorImpl.AND, keys);
	}

	static Selector eqOr(String... keys) {
		return SelectorImpl.multiKeyOp(SelectorImpl.EQ, SelectorImpl.OR, keys);
	}

	static Selector neq(String key) {
		return SelectorImpl.keyOp(SelectorImpl.NEQ, key);
	}

	static Selector cmpl(String key) {
		return SelectorImpl.keyOp(SelectorImpl.CMPL, key);
	}

	static Selector cmpg(String key) {
		return SelectorImpl.keyOp(SelectorImpl.CMPG, key);
	}

	static Selector cmple(String key) {
		return SelectorImpl.keyOp(SelectorImpl.CMPLE, key);
	}

	static Selector cmpge(String key) {
		return SelectorImpl.keyOp(SelectorImpl.CMPGE, key);
	}

	static Selector like(String key) {
		return SelectorImpl.keyOp(SelectorImpl.LIKE, key);
	}

	static Selector and(Selector... selectors) {
		return SelectorImpl.selectorOp(SelectorImpl.AND, (SelectorImpl[]) selectors);
	}

	static Selector or(Selector... selectors) {
		return SelectorImpl.selectorOp(SelectorImpl.OR, (SelectorImpl[]) selectors);
	}
}
