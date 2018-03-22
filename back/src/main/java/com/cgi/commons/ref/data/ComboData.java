package com.cgi.commons.ref.data;

import java.io.Serializable;
import java.util.Map;

import com.cgi.commons.ref.entity.Key;

/**
 * A combo data.
 */
public class ComboData implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 4405265421014571902L;

	/** The entity name. */
	private String entityName;

	/** The combo values. */
	private Map<Key, String> comboValues;

	/**
	 * Constructor.
	 * 
	 * @param entityName The entity name.
	 * @param comboValues The combo values.
	 */
	public ComboData(String entityName, Map<Key, String> comboValues) {
		this.entityName = entityName;
		this.comboValues = comboValues;
	}

	/**
	 * Getter for the entityName.
	 * @return the entityName.
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * Setter for the entityName.
	 * @param entityName the entityName.
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Getter for the comboValues.
	 * @return the comboValues.
	 */
	public Map<Key, String> getComboValues() {
		return comboValues;
	}

	/**
	 * Setter for the comboValues.
	 * @param comboValues the comboValues.
	 */
	public void setComboValues(Map<Key, String> comboValues) {
		this.comboValues = comboValues;
	}

}
