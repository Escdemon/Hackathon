package com.cgi.commons.rest.auth;

import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.jwt.profile.JwtProfile;

/**
 * Verify if user isn't authenticated.
 */
public class IsAnonymousAuthorizer implements Authorizer<JwtProfile> {
    @Override
	public boolean isAuthorized(WebContext webContext, JwtProfile userProfile) {
		return null == userProfile;
    }
}
