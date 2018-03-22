/**
 * 
 */
package com.cgi.commons.ref.entity;

/**
 * Class representing an allowed value for an EntityField.<br/>
 * This is a treple with a code, a label and a value.
 * 
 * 
 */
public class DefinedValue {
	/** Programmer code for the defined value. */
	final private String code;
	/** Display label (onto screen) for this allowed value. */
	final private String label;
	/** Actual value object for this allowed value. */
	final private Object value;

	/**
	 * Create a new allowed value.
	 * 
	 * @param code
	 *            String representing programmer code
	 * @param label
	 *            String matching property key for screen display
	 * @param value
	 *            Value Object
	 */
	public DefinedValue(String code, String label, Object value) {
		super();
		this.code = code;
		this.label = label;
		this.value = value;
	}

	/**
	 * Getter for the Code.
	 * @return The Code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Getter for the Label.
	 * @return The Label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Getter for the Value.
	 * @return The Value.
	 */
	public Object getValue() {
		return value;
	}

}
