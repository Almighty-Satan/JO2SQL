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

package com.github.almightysatan.jo2sql.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

	/**
	 * A unique, non-null name for this column. Should only contain alphanumeric
	 * characters.
	 * 
	 * @return The name of this column
	 */
	String value();

	/**
	 * Whether this column can be null.
	 * 
	 * @return True if this column should never be null false otherwise (Default:
	 *         {@code true})
	 */
	boolean notNull() default true;

	/**
	 * Whether the value of this column should be automatically set to a unique
	 * value when 0. This throws an {@link java.lang.Error} if the type of the field
	 * is anything other than {@code long} or {@link Long}. There should only one
	 * auto increment column per table.
	 * 
	 * @return True if the value should be incremented automatically, false
	 *         otherwise (Default: {@code false})
	 */
	boolean autoIncrement() default false;

	/**
	 * Whether the values of this column are unique.
	 * 
	 * @return True if unique, false if not unique (Default: {@code false})
	 */
	boolean unique() default false;

	/**
	 * Whether this column is part if the Primary Key. Every table has to have a
	 * Primary Key that contains at least one column.
	 * 
	 * @return True if part of the Primary Key, false otherwise (Default:
	 *         {@code false})
	 */
	boolean primary() default false;

	int size() default -1;

	Class<?> type() default void.class;
}
