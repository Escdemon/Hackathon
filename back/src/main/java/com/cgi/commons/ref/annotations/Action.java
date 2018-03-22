package com.cgi.commons.ref.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.cgi.commons.ref.entity.Action.Input;
import com.cgi.commons.ref.entity.Action.Persistence;
import com.cgi.commons.ref.entity.Action.Process;
import com.cgi.commons.ref.entity.Action.UserInterface;

/**
 * Annotation Action.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Action {

	/** Action code. */
	String code();
	/** Query name. */
	String queryName() default "";
	/** Page name. */
	String pageName() default "";
	/** Input kind. */
	Input input() default Input.ONE;
	/** Persistence kind. */
	Persistence persistence();
	/** User Interface kind. */
	UserInterface ui() default UserInterface.INPUT;
	/** Process kind. */
	Process process() default Process.STANDARD;

	/** List of the sub actions. */
	String[] subActions() default {};

}
