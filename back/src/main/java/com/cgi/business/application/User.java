package com.cgi.business.application;

import java.io.Serializable;
import java.util.Locale;
import org.pac4j.jwt.profile.JwtProfile;

import com.cgi.commons.security.DefaultUser;

/**
 * Extensible class to store User specific information. If used, this class must be instantiated by SecurityManager in
 * the getUser() method.
 */
public class User extends DefaultUser implements Serializable {

	/**
	 * Create a new User for the given login.
	 * 
	 * @param login
	 *            The login of the new user.
	 * @param locale
	 *            user locale
	 */
	public User(String login, Locale locale) {
		super(login, locale);
	}

	/**
	 * Create a new User for the given login and the default locale.
	 * 
	 * @param login
	 *            The login of the new user.
	 */
	public User(String login) {
		super(login, null);
	}

	/**
	 * Create a user from a received UserProfile JWT token.
	 * 
	 * @param profile
	 *            jwt profile
	 * @param locale
	 *            user locale
	 */
	public User(JwtProfile profile, Locale locale) {
		super(profile, locale);
	}

	/**
	 * Convert this user to a UserProfile for the JWT token.
	 * 
	 * @return the profile
	 */
	@Override
	public JwtProfile toJwtProfile() {
		JwtProfile profile = super.toJwtProfile();
		// Add custom attributes

		return profile;
	}

}

