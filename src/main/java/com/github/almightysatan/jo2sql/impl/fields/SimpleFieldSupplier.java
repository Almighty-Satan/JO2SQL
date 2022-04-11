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

package com.github.almightysatan.jo2sql.impl.fields;

import java.lang.reflect.Field;

import com.github.almightysatan.jo2sql.Column;
import com.github.almightysatan.jo2sql.impl.SqlProviderImpl;

public class SimpleFieldSupplier implements FieldSupplier {

	private Constructor constructor;
	private Class<?>[] types;

	public SimpleFieldSupplier(Constructor constructor, Class<?>... types) {
		this.constructor = constructor;
		this.types = types;
	}

	@Override
	public boolean isType(Field field) {
		for (Class<?> type : this.types)
			if (type == field.getType())
				return true;
		return false;
	}

	@Override
	public AnnotatedField createField(SqlProviderImpl provider, Field field, Column annotation) throws Throwable {
		return this.constructor.newInstance(provider, field, annotation);
	}

	@FunctionalInterface
	public interface Constructor {
		AnnotatedField newInstance(SqlProviderImpl provider, Field field, Column column) throws Throwable;
	}
}
