package com.cgi.commons.rest.domain;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.utils.TechnicalException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entity for Web Services Rest.
 */
public abstract class RestEntity {

	/**
	 * Returns the name of the entity.
	 * 
	 * @return the name of the entity.
	 */
	public abstract String name();

	/** PK of the entity */
	// Annotate with @JsonIgnore to prevent using input value
	@JsonIgnore
	private Key primaryKey;

	/** Entity's description. It is read-only. */
	// Annotate with @JsonIgnore to prevent using input value
	@JsonIgnore
	private String internalCaption;

	/**
	 * Additional criterias to perform a research.
	 */
	private Map<String, String[]> searchCriteria = new HashMap<>();

	@JsonProperty
	public Key getPrimaryKey() {
		return primaryKey;
	}

	// Annotate with @JsonIgnore to prevent using input value
	@JsonIgnore
	public void setPrimaryKey(Key primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * @return The additional criterias to perform a research.
	 */
	public Map<String, String[]> getSearchCriteria() {
		return searchCriteria;
	}

	/**
	 * Sets the additional criterias to perform a research.
	 * 
	 * @param searchCriteria
	 *            Additional criterias.
	 */
	public void setSearchCriteria(Map<String, String[]> searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

	@JsonProperty
	public String getInternalCaption() {
		return internalCaption;
	}

	// Annotate with @JsonIgnore to prevent using input value
	@JsonIgnore
	public void setInternalCaption(String internalCaption) {
		this.internalCaption = internalCaption;
	}

	/**
	 * Invoke the setter method for the fieldname, with the value into parameters.
	 * 
	 * @param fieldName
	 *            Field Name.
	 * @param value
	 *            Value.
	 */
	public void invokeSetter(String fieldName, Object value) {
		Object val;
		Field f;
		try {
			f = this.getClass().getDeclaredField(fieldName);
		} catch (SecurityException | NoSuchFieldException e) {
			throw new TechnicalException("Impossible de trouver le champ " + fieldName, e);
		}
		if (Integer.class.equals(f.getGenericType()) && value instanceof String) {
			try {
				val = Integer.parseInt((String) value);
			} catch (NumberFormatException ex) {
				throw new TechnicalException("Impossible de convertir " + value + " en un entier pour l'assigner a "
						+ fieldName);
			}
		} else {
			val = value;
		}
		String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		Method method = null;
		if (val != null) {
			try {
				method = this.getClass().getMethod(methodName, f.getType());
			} catch (SecurityException | NoSuchMethodException e) {
				throw new TechnicalException("Impossible de trouver la méthode " + methodName, e);
			}
		} else {
			for (int i = 0; i < this.getClass().getDeclaredMethods().length; i++) {
				Method m = this.getClass().getDeclaredMethods()[i];
				if (methodName.equals(m.getName())) {
					method = m;
				}
			}
		}
		try {
			method.invoke(this, val);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " dans le champ " + fieldName, e);
		}
	}

	/**
	 * Invoke the getter method for a fieldname, and returns the value.
	 * 
	 * @param fieldName
	 *            Field Name.
	 * @return The Value.
	 */
	public Object invokeGetter(String fieldName) {
		String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		Object result = null;
		try {
			Method method = this.getClass().getMethod(methodName);
			result = method.invoke(this);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new TechnicalException("Impossible d'invoquer la methode " + methodName, e);
		}
		return result;
	}
}
