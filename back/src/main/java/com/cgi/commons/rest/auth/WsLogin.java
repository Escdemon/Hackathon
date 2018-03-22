package com.cgi.commons.rest.auth;

import java.io.Serializable;

/**
 * Class Login for Web Services.
 */
public class WsLogin implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -1757993341314602632L;

	/** Login. */
	public String login;
	/** Password. */
	public String password;
	/** Key. */
	public String key;

	/**
	 * Constructor.
	 */
	public WsLogin() {

	}

}
