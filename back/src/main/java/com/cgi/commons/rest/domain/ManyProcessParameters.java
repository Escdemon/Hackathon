package com.cgi.commons.rest.domain;

import java.io.Serializable;

import com.cgi.commons.ref.entity.Key;

/**
 * Parameter.
 */
public class ManyProcessParameters<E extends RestEntity, K extends Key> implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 8777404285740687300L;

	/** List of keys. */
	public K[] keys;
	/** Bean. */
	public E bean;

	/**
	 * Constructor.
	 */
	public ManyProcessParameters() {
		super();
	}

}
