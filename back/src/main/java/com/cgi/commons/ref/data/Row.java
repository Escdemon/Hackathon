package com.cgi.commons.ref.data;

import java.util.HashMap;

import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.entity.Key;

/** 
 * One element in a result list, on any list page.
 */
public class Row extends HashMap<String, Object> {

	/** Serial id. */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.<br/>
	 * <b>WARNING</b> : Do not forget to set the PK for this row.
	 */
	public Row() {
		super();
	}
	
	/**
	 * Init a new row with the given PK.
	 * 
	 * @param pk : the Key object representing the row PK.
	 */
	public Row(Key pk) {
		super();
		setPk(pk);
	}

	/**
	 * Current row primary key.
	 * 
	 * @return Primary key of this row main entity
	 */
	public Key getPk() {
		return (Key) this.get(Constants.RESULT_PK);
	}

	/**
	 * Set a new row primary key.
	 * 
	 * @param pk Primary key of this row main entity
	 */
	public void setPk(Key pk) {
		this.put(Constants.RESULT_PK, pk);
	}

	/**
	 * Tests if the current row instance has been "checked" in UI.
	 * 
	 * @return true when the current row is "checked" or "selected by user", false otherwise
	 */
	public boolean checked() {
		if (get("checked") != null && get("checked") instanceof Boolean) {
			return ((Boolean) get("checked")).booleanValue();
		}
		return false;
	}
}
