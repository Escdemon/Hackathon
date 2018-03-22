package com.cgi.business.application;

import com.cgi.commons.security.DefaultSecurityManager;

/**
 * Extensible class to store Security specific behavior. It must implement AbstractSecurityManager. 
 */
public class SecurityManager extends DefaultSecurityManager {

	/** Serial id. */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean disableSecurity() {
		return true;
	}

}
