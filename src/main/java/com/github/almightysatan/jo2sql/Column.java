package com.github.almightysatan.jo2sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

	String value();

	boolean notNull() default true;

	boolean autoIncrement() default false;

	boolean unique() default false;

	boolean primary() default false;

	boolean index() default false;

	int size() default -1;
}
