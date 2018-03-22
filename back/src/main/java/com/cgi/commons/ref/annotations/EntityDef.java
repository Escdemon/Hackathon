package com.cgi.commons.ref.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation EntityDef.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EntityDef {
	/** DB name. */
	String dbName();
	/** PK. */
	String[] primaryKey();
	
	/** Schema. */
	String schemaId() default "";
	
	/** Sequence ID. */
	String sequenceName() default "";
	
	/** Is an associative entity. */
	boolean isAssociative() default false;
	
	/** Is an external entity. */
	boolean isExternal() default false;
	
	/** Version field name */
	String versionField() default "";
}
