package com.cgi.commons.utils;

import java.util.ArrayList;
import java.util.List;

import com.cgi.commons.ref.data.Message;

/**
 * Functional Exception.
 */
public class FunctionalException extends RuntimeException {

	/** Serial id. */
	private static final long serialVersionUID = -4392654603736653249L;

	/** Messages. */
	private List<Message> messages = new ArrayList<Message>();

	@Override
	public String getMessage() {
		if (messages.size() > 0) {
			return messages.get(0).getMessage();
		}
		return super.getMessage();
	}

	/**
	 * Getter for messages.
	 * @return the messages.
	 */
	public List<Message> getMessages() {
		return messages;
	}

	/**
	 * Constructor.
	 * 
	 * @param errors List of messages to add.
	 */
	public FunctionalException(List<Message> errors) {
		super();
		messages.addAll(errors);
	}

	/**
	 * Constructor.
	 * 
	 * @param m Message to add.
	 */
	public FunctionalException(Message m) {
		super();
		messages.add(m);
	}

}
