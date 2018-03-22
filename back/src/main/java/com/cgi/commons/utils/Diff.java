package com.cgi.commons.utils;

import java.io.Serializable;

/** 
 * Difference between two element. Used to compare a user's modification with modifications already persisted.
 */
public class Diff implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 1L;

	/** Label. */
	private final String label;
	/** First element to compare : named "mine". */
	private final Serializable mine;
	/** Second element to compare : named "their". */
	private final Serializable theirs;
	/** Say if mine and their has values. */
	private final boolean hasValues;

	/**
	 * Difference noted, but not storing the values (they are BLOBs or otherwise too heavy).
	 * 
	 * @param label Name of the diff.
	 */
	public Diff(String label) {
		this.label = label;
		this.mine = null;
		this.theirs = null;
		this.hasValues = false;
	}

	/** 
	 * Difference with the associated values.
	 * 
	 * @param label Name of the diff.
	 * @param mine First element to compare.
	 * @param theirs Second element to compare.
	 */
	public Diff(String label, Serializable mine, Serializable theirs) {
		this.label = label;
		this.mine = mine;
		this.theirs = theirs;
		this.hasValues = true;
	}

	/** 
	 * Difference with the associated values.
	 * 
	 * @param label Name of the diff.
	 * @param mine First element to compare.
	 * @param theirs Second element to compare.
	 */
	public Diff(String label, Object mine, Object theirs) {
		this(label, toSerializable(mine), toSerializable(theirs));
	}

	/**
	 * Getter of the name of the diff.
	 * @return The name of the diff.
	 */
	public Serializable getLabel() {
		return label;
	}

	/**
	 * Getter of the first element to compare, mine.
	 * @return Mine.
	 */
	public Serializable getMine() {
		return mine;
	}

	/**
	 * Getter of the first element to compare, mine.
	 * @return Mine.
	 */
	public Serializable getTheirs() {
		return theirs;
	}

	/**
	 * Say if mine and their has values.
	 * @return True if they have, else no.
	 */
	public boolean hasValues() {
		return hasValues;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(label);
		builder.append(" : ").append(mine).append(" <=> ").append(theirs);
		return builder.toString();
	}

	/**
	 * Serialize an object.
	 * @param value The object.
	 * @return The Serializable.
	 */
	private static Serializable toSerializable(Object value) {
		if (value == null) {
			return null;
		} else if (value instanceof Serializable) {
			return (Serializable) value;
		} else {
			return value.toString();
		}
	}

}
