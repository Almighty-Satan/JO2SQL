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

package com.github.almightysatan.jo2sql.logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public interface Logger {

	void debug(String message);

	void info(String message);

	void error(String message);

	default void debug(String message, Object... args) {
		this.debug(String.format(message, args));
	}

	default void info(String message, Object... args) {
		this.info(String.format(message, args));
	}

	default void error(String message, Object... args) {
		this.error(String.format(message, args));
	}

	default void error(String message, Throwable throwable, Object... args) {
		if (args.length == 0)
			this.error(message);
		else
			this.error(message, args);

		try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
			throwable.printStackTrace(printWriter);
			this.error(stringWriter.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
