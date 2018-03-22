package com.cgi.commons.ref.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation DefinedValues.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface DefinedValues {
	
	/** List of DefinedValue annotations. */
	DefinedValue[] value() default {};

}
