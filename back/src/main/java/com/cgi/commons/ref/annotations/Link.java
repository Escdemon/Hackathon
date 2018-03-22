package com.cgi.commons.ref.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation Link.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Link {

	/** Name. */
	String name();
	/** Name of the target entity. */
	String targetEntity();
	/** Fields of the link. */
	String[] fields();

}
