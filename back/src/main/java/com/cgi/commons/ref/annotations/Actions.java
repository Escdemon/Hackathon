package com.cgi.commons.ref.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation Actions.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Actions {

	/** List of Action annotations. */
	Action[] value() default {};

}
