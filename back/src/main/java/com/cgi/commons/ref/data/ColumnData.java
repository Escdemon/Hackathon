package com.cgi.commons.ref.data;

import java.io.Serializable;

/**
 * Column properties.
 */
public class ColumnData implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -6488987032635586234L;

	/**
	 * Column title.
	 */
	private String title;

	/**
	 * Indicates whether this column is visible.
	 */
	private boolean visible;

	/**
	 * Getter for the title.
	 * @return the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter for the title.
	 * @param title the title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Getter for the visible.
	 * @return the visible.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Setter for the visible.
	 * @param visible the visible.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
