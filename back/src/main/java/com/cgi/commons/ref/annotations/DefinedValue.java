package com.cgi.commons.ref.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation DefinedValue.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface DefinedValue {

	/** Code. */
	String code();
	/** Label. */
	String label();
	/** Value. */
	String value();

	/** True if default choice. */
	boolean isDefault() default false;

}
