package com.cgi.commons.utils;

import com.cgi.business.application.ApplicationLogic;
import com.cgi.commons.ref.context.ApplicationContext;

/**
 * Utility class used to retrieve the {@link AbstractApplicationLogic} instance.
 */
public class ApplicationUtils {

	/** Private constructor (utility class). */
	private ApplicationUtils() {
		// Do not instantiate this.
	}

	/** Application logic object. */
	private static ApplicationLogic appLogic = new ApplicationLogic();

	/** Application context. There can be only one */
	private static ApplicationContext appContext = new ApplicationContext();

	/**
	 * Return the application logic object.
	 * 
	 * @return The application logic object.
	 */
	public static ApplicationLogic getApplicationLogic() {
		return appLogic;
	}

	/**
	 * Return the application context.
	 * 
	 * @return The application context.
	 */
	public static ApplicationContext getApplicationContext() {
		return appContext;
	}
}
