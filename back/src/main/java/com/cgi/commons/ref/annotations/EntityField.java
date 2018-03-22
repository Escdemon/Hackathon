package com.cgi.commons.ref.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.cgi.commons.ref.entity.EntityField.Memory;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;

/**
 * Annotation EntityField.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EntityField {
	/** SQL field name. */
	String sqlName();

	/** SQL type. */
	SqlTypes sqlType();
	
	/** SQL field size. */
	int sqlSize();

	/** SQL decimal accuracy. */
	int sqlAccuracy() default 0;

	/** Default Value. */
	String defaultValue() default "$-$";

	/** Memory variable type. */
	Memory memory() default Memory.NO;

	/** SQL expression. */
	String sqlExpr() default "";

	/** Field is mandatory. */
	boolean isMandatory() default false;

	/** Field is a lookup field. */
	boolean isLookupField() default false;

	/** Field is autoincremented. */
	boolean isAutoIncrementField() default false;

}
