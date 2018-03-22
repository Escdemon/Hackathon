package com.cgi.commons.ref.data;

import java.io.Serializable;

/**
 * Ui Access.
 */
public class UiAccess implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 8012941316941268894L;

	/** Name. */
	public String name;
	/** Indicates if it is visible. */
	public boolean visible;
	/** Indicates if it is read only. */
	public boolean readOnly;
	/** Label. */
	public String label;
	/** Indicates if it is mandatory. */
	public boolean mandatory;

	/**
	 * Constructor.
	 * 
	 * @param name Name
	 * @param isVisible Indicates if it is visible
	 * @param isReadOnly Indicates if it is read only
	 * @param label Label
	 * @param mandatory Indicates if it is mandatory
	 */
	public UiAccess(String name, boolean isVisible, boolean isReadOnly, String label, boolean mandatory) {
		this.name = name;
		this.visible = isVisible;
		this.readOnly = isReadOnly;
		this.label = label;
		this.mandatory = mandatory;
	}

}
