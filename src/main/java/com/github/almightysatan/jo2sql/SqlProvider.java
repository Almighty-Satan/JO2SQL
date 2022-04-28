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

import java.util.concurrent.TimeUnit;

public interface SqlProvider {

	<T> DatabaseAction<Void> createIfNotExists(Class<T> type);

	<T> DatabaseAction<Void> dropIfExists(Class<T> type);

	<T> PreparedReplace<T, Void> prepareReplace(Class<T> type);

	<T> PreparedReplace<T, Long> prepareAiReplace(Class<T> type);

	<T> PreparedSelect<T> preparePrimarySelect(Class<T> type);

	<T> PreparedSelect<T> prepareSelect(Class<T> type, Selector selector);

	<T> PreparedSelect<T[]> prepareMultiSelect(Class<T> type, Selector selector);

	<T> PreparedObjectDelete<T> prepareObjectDelete(Class<T> type);

	<T> PreparedDelete prepareDelete(Class<T> type, Selector selector);

	<T> Table<T> getTable(Class<T> type);

	void terminate();

	void terminate(long timeout, TimeUnit timeUnit) throws InterruptedException;
}