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

public interface PreparedPushAction<T extends PreparedPushAction<?>> {

	/**
	 * If enabled nested objects will be deleted or overwritten
	 * 
	 * @param overwrite If nested objects will be deleted or overwritten. This could
	 *                  possibly delete nested objects that are still referenced
	 *                  somewhere else. (Default: {@code false})
	 * @return This {@link PreparedPushAction} instance
	 */
	T overwriteNestedObjects(boolean overwrite);

	/**
	 * Equivalent to {@code overwriteNestedObjects(true)}
	 * 
	 * @return This {@link PreparedPushAction} instance
	 */
	default T overwriteNestedObjects() {
		return this.overwriteNestedObjects(true);
	}
}
