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

public class StringUtil {

	private static final int UNICODE_ZERO = 0x30;
	private static final int UNICODE_NINE = 0x39;
	private static final int UNICODE_UPPERCASE_A = 0x41;
	private static final int UNICODE_UPPERCASE_Z = 0x5A;
	private static final int UNICODE_LOWERCASE_A = 0x61;
	private static final int UNICODE_LOWERCASE_Z = 0x7A;

	public static boolean isAlphanumeric(String input) {
		for (char c : input.toCharArray())
			if ((c < UNICODE_ZERO || c > UNICODE_NINE) && (c < UNICODE_UPPERCASE_A || c > UNICODE_UPPERCASE_Z)
					&& (c < UNICODE_LOWERCASE_A || c > UNICODE_LOWERCASE_Z))
				return false;
		return true;
	}

	public static void assertAlphanumeric(String input) {
		if (!isAlphanumeric(input))
			throw new IllegalArgumentException(String.format("String '%s' is not alphanumeric", input));
	}
}
