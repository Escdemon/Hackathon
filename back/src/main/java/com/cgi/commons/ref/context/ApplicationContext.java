package com.cgi.commons.ref.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The application context.
 */
public class ApplicationContext implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -1304368564807700534L;

	/** Name of the application. */
	private String appName = "";

	/** Description of the application. */
	private String appDescription = "";

	/** Version of the application. */
	private String appVersion = "";

	/** Attributes. */
	private Map<String, Object> attributes = new HashMap<String, Object>();

	/**
	 * Getter for the application name.
	 * @return the application name.
	 */
	public String getAppName() {
		return appName;
	}

	/**
	 * Setter for the application name.
	 * @param appName the application name.
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * Getter for the application description.
	 * @return the application description.
	 */
	public String getAppDescription() {
		return appDescription;
	}

	/** 
	 * Setter for the application description.
	 * @param appDescription the application description.
	 */
	public void setAppDescription(String appDescription) {
		this.appDescription = appDescription;
	}

	/**
	 * Getter for the application version.
	 * @return the application version.
	 */
	public String getAppVersion() {
		return appVersion;
	}

	/**
	 * Setter for the application version.
	 * @param appVersion the application version.
	 */
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	/**
	 * Getter for the attributes.
	 * @return The attributes.
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * Setter for the attributes.
	 * @param attributes The attributes.
	 */
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

}
