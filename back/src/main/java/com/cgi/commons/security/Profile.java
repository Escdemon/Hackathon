package com.cgi.commons.security;

import java.io.Serializable;

/**
 * Profile.
 */
public class Profile implements Serializable {
	/** Serial Id. */
	private static final long serialVersionUID = 1L;

	/** Profile. */
	private String profile;

	/**
	 * Returns profile.
	 * @return profile.
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * Set the profile.
	 * @param profile The profile.
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}


}
