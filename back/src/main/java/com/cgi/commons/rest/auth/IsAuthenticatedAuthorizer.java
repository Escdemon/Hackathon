package com.cgi.commons.rest.auth;

import java.util.Date;

import org.pac4j.core.authorization.Authorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.jwt.JwtConstants;
import org.pac4j.jwt.profile.JwtProfile;

/**
 * Verify if user is authenticated.
 */
public class IsAuthenticatedAuthorizer implements Authorizer<HttpProfile> {
    @Override
	public boolean isAuthorized(WebContext webContext, HttpProfile userProfile) {
    	boolean ok = null != userProfile;
    	if (ok && userProfile instanceof JwtProfile) {
    		Date expiration = (Date) userProfile.getAttribute(JwtConstants.EXPIRATION_TIME);
    		ok = null != expiration && expiration.compareTo(new Date()) >= 0;
    	}
		return ok;
    }
}
