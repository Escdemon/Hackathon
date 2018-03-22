package com.cgi.commons.ref.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.utils.TechnicalException;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Key.
 */
public class Key implements Serializable {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(Key.class);

	/** Serial id. */
	private static final long serialVersionUID = 3861723578927708930L;

	/** Field separator. */
	private static final String FIELD_SEPARATOR = ",,,";
	/** Key-value separator. */
	private static final String KEY_VALUE_SEPARATOR = ":::";

	/** Model of the key. */
	private KeyModel model;
	/** Map key-value of the key. */
	private Map<String, Object> values = new HashMap<String, Object>();

	/**
	 * Default constructor used by Jersey (Web Services). Use with caution.
	 */
	public Key() {
	}

	/**
	 * Constructor.
	 * 
	 * @param entityName
	 *            Entity name.
	 */
	public Key(String entityName) {
		model = new KeyModel(entityName);
	}

	/**
	 * Constructor.
	 * 
	 * @param entityName
	 *            Entity name.
	 * @param encodedKey
	 *            Encoded Key (key1:::value1,,,key2:::value2 etc etc)
	 */
	public Key(String entityName, String encodedKey) {
		model = new KeyModel(entityName);
		setEncodedValue(encodedKey);
	}

	/**
	 * Constructor.
	 * 
	 * @param keyModel
	 *            Model of the Key.
	 */
	public Key(KeyModel keyModel) {
		model = keyModel;
	}

	/**
	 * Return the value for a field.
	 * 
	 * @param field
	 *            Field name.
	 * @return The value associated.
	 */
	public Object getValue(String field) {
		return values.get(field);
	}

	/**
	 * Fixe les valeurs de la clé courante avec les valeurs d'une autre clé dont les noms de champs sont potentiellement différents. Cette
	 * méthode sert à fabriquer une clé primaire à partir d'une clé étrangère qui la référence ou l'inverse.
	 * 
	 * @param key
	 *            Clé étrangère qui référence la clé primaire, ou l'inverse.
	 */
	public void setValue(Key key) {
		if (key.getModel().getFields().size() != getModel().getFields().size()) {
			throw new TechnicalException("Les clés n'ont pas le même nombre de champ.");
		}
		for (int i = 0; i < getModel().getFields().size(); i++) {
			setValue(getModel().getFields().get(i), key.getValue(key.getModel().getFields().get(i)));
		}
	}

	/**
	 * Save a value for a field.
	 * 
	 * @param field
	 *            Field Name.
	 * @param val
	 *            Value to Save.
	 */
	public void setValue(String field, Object val) {
		values.put(field, val);
	}

	/**
	 * Retrieve the Model of the Key.
	 * 
	 * @return The Key Model.
	 */
	public KeyModel getModel() {
		return model;
	}

	/**
	 * Generate the encoded value of the Key. (key1:::value1,,,key2:::value2 etc etc)
	 * 
	 * @return the encoded value of the Key.
	 */
	@JsonValue
	public String getEncodedValue() {
		StringBuilder encodedKey = new StringBuilder();
		boolean first = true;
		for (String fieldName : model.getFields()) {
			if (!first) {
				encodedKey.append(FIELD_SEPARATOR);
			}
			encodedKey.append(fieldName);
			encodedKey.append(KEY_VALUE_SEPARATOR);

			Object v = values.get(fieldName);
			String sValue = String.valueOf(v);
			// TODO remove use of type qualifier in a serialized key
			// see also app.tabedit.js line 804
			if (v == null) {
				encodedKey.append("N");
				sValue = "";
			} else {
				EntityModel mEntity = EntityManager.getEntityModel(getModel().getEntityName());
				EntityField efield = mEntity.getField(fieldName);
				SqlTypes sqlType = efield.getSqlType();
				switch (sqlType) {
				case BOOLEAN:
					encodedKey.append("B");
					break;
				case DATE:
					encodedKey.append("D");
					sValue = String.valueOf(((Date)v).getTime());
					break;
				case DECIMAL:
					if (efield.getSqlAccuracy() == 0)
						encodedKey.append("L");
					else
						encodedKey.append("F");
					break;
				case INTEGER:
					encodedKey.append("I");
					break;
				case TIME:
					encodedKey.append("H");
					sValue = String.valueOf(((Date)v).getTime());
					break;
				case TIMESTAMP:
					encodedKey.append("T");
					sValue = String.valueOf(((Date)v).getTime());
					break;
				case CHAR:
				case VARCHAR:
				case VARCHAR2:
					encodedKey.append("S");
					break;
				default:
					LOGGER.warn("A BLOB or a CLOB is used in a key from entity " + getModel().getEntityName());
				}
			}
			encodedKey.append(sValue);
			first = false;
		}
		return encodedKey.toString();
	}

	/**
	 * Modify the fields of the key thanks to an encoded value.
	 * 
	 * @param encodedString
	 *            The encoded value.
	 */
	public void setEncodedValue(String encodedString) {
		if (encodedString == null) {
			values.clear();
			return;
		}
		String[] fieldValues = encodedString.split(FIELD_SEPARATOR);
		values.clear();
		for (int i = 0; i < fieldValues.length; i++) {
			String[] valTab = fieldValues[i].split(KEY_VALUE_SEPARATOR);
			if (valTab.length > 1) {
				String field = valTab[0];
				String sValue = valTab[1];
				if (sValue.length() == 0) {
					LOGGER.error("Unable to deserialize Key value for field " + field);
				} else {
					String sType = sValue.substring(0, 1);
					sValue = sValue.substring(1);

					// Decode value using given type
					Object value = null;
					if ("N".equals(sType)) {
						value = null;
					} else if ("B".equals(sType)) {
						value = Boolean.valueOf(sValue);
					} else if ("D".equals(sType)) {
						value = new Date(Long.parseLong(sValue));
					} else if ("L".equals(sType)) {
						value = Long.parseLong(sValue);
					} else if ("F".equals(sType)) {
						value = new BigDecimal(sValue);
					} else if ("I".equals(sType)) {
						value = Integer.parseInt(sValue);
					} else if ("H".equals(sType)) {
						value = new Time(Long.parseLong(sValue));
					} else if ("T".equals(sType)) {
						value = new Timestamp(Long.parseLong(sValue));
					} else if ("S".equals(sType)) {
						value = sValue;
					} else {
						LOGGER.error("Cannot assign a value to a BLOB or a CLOB from a serialized Key value for field " + field);
					}

					values.put(field, value);
				}
			}
		}
	}

	/**
	 * All key variables are null.
	 * 
	 * @return <code>true</code> if the key is null, every single variable is null. <code>false</code> otherwise
	 */
	public boolean isNull() {
		if (values.isEmpty()) {
			return true;
		}
		for (Object value : values.values()) {
			if (value != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * None of key variables is null.
	 * 
	 * @return <code>true</code> if the key is full, no variable is null. <code>false</code> otherwise
	 */
	public boolean isFull() {
		if (values.size() < model.getFields().size()) {
			return false;
		}
		for (Object value : values.values()) {
			if (value == null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the current key contains all values of the partial key passed in parameter. Null values of the partial key aren't checked. For
	 * instance, if key is (var1='pouet' and var2='toto') and partialKey is (var1=null and var2='toto'), it will return true.
	 * 
	 * 
	 * @param partialKey
	 *            A key with the same fields, but not all values.
	 * @return <code>true</code> if all values of the partial Key are in the current key
	 */
	public boolean contains(Key partialKey) {
		for (String field : model.getFields()) {
			if (partialKey.getValue(field) == null) {
				continue;
			}
			if (!partialKey.getValue(field).equals(getValue(field))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Nullify the key.
	 */
	public void nullify() {
		values.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Key other = (Key) obj;
		if (model == null) {
			if (other.model != null) {
				return false;
			}
		} else if (!model.equals(other.model)) {
			return false;
		}
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
			return false;
		}
		return true;
	}

	/**
	 * Compares values of current key instance with another key values. This method ensures that both keys have the same number of fields and
	 * compares them based on field order. This method can be used to check if a foreign key values equals a primary key values.
	 * 
	 * @param otherKey
	 *            The key to compare
	 * @return true if both keys have the same number of fields and same values, false otherwise
	 */
	public boolean hasSameValues(Key otherKey) {
		if (otherKey == null) {
			return false;
		}
		if (otherKey.getModel().getFields().size() != this.model.getFields().size()) {
			return false;
		}

		for (int i = 0; i < model.getFields().size(); i++) {
			Object value = this.getValue(this.getModel().getFields().get(i));
			Object otherValue = otherKey.getValue(otherKey.getModel().getFields().get(i));
			if (value == null && otherValue != null) {
				return false;
			}
			if (value != null && !value.equals(otherValue)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "Key [model=" + model + ", values=" + values + "]";
	}
}
