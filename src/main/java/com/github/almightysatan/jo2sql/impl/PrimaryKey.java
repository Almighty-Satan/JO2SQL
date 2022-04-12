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

import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.almightysatan.jo2sql.Selector;
import com.github.almightysatan.jo2sql.impl.fields.AnnotatedField;

public class PrimaryKey extends AbstractIndex {

	private Selector selector;

	public PrimaryKey(AnnotatedField... indexFields) {
		super(indexFields);
	}

	@Override
	public void appendIndex(StringBuilder builder, String delimiter) {
		builder.append(delimiter).append("PRIMARY KEY (`").append(Arrays.stream(this.getIndexFields())
				.map(AnnotatedField::getColumnName).collect(Collectors.joining("`,`"))).append("`)");
	}

	public Selector getSelector() {
		if (this.selector == null)
			this.selector = Selector.eqAnd(
					Arrays.stream(this.getIndexFields()).map(AnnotatedField::getColumnName).toArray(String[]::new));
		return this.selector;
	}
}
