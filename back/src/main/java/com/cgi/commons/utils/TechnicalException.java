package com.cgi.commons.utils;

/**
 * Technical Exception.
 */
public class TechnicalException extends RuntimeException {

	/** Serial id. */
	private static final long serialVersionUID = 5051522312026416092L;

	/**
	 * Constructor.
	 * 
	 * @param message The message of the exception.
	 */
	public TechnicalException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message The message of the exception.
	 * @param t The cause.
	 */
	public TechnicalException(String message, Throwable t) {
		super(message, t);
	}
}
