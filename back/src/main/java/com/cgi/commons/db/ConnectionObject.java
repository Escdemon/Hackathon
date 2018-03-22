package com.cgi.commons.db;

import java.util.Arrays;

/**
 * A connection object.
 */
public class ConnectionObject {
	
	/** Unique id. */
	private int id;
	
	/** The real connection object. */
	private Object connection;
	/** The stack trace. */
	private StackTraceElement[] stackTrace;

	/** 
	 * Constructor. 
	 * 
	 * @param idConn Unique id.
	 * @param conn The real connection object.
	 * @param stack The stack trace.
	 */
	public ConnectionObject(int idConn, Object conn, StackTraceElement[] stack) {
		this.id = idConn;
		this.connection = conn;
		if (stack != null) {
			this.stackTrace = Arrays.copyOf(stack, stack.length);
		}
	}

	/**
	 * Getter for the unique id.
	 * @return The unique id.
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Getter for the stack trace.
	 * @return The stack trace.
	 */
	public StackTraceElement[] getStackTrace() {
		return this.stackTrace;
	}

	/**
	 * Return the class of the connection object.
	 * @return the class of the connection object.
	 */
	public Class<?> getConnectionClass() {
		return this.connection.getClass();
	}

	/**
	 * Getter of the real connection object.
	 * @return the real connection object.
	 */
	public Object getConnection() {
		return this.connection;
	}

	/**
	 * Close the connection.
	 */
	public void close() {
		if (connection instanceof DbConnection) {
			((DbConnection) connection).close();
		}
	}
}
