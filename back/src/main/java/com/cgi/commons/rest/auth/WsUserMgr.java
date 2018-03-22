package com.cgi.commons.rest.auth;

import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jwt.JwtConstants;
import org.pac4j.jwt.profile.JwtGenerator;
import org.pac4j.jwt.profile.JwtProfile;

import com.cgi.business.application.User;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.security.AbstractSecurityManager;
import com.cgi.commons.security.DefaultSecurityManager;
import com.cgi.commons.security.SecurityUtils;

/**
 * Manager for Web Services Users.
 */
public class WsUserMgr {

	/** Instance. */
	private static WsUserMgr instance = null;
	/** Constant for token time of validity. */
	private final int tokenTime;

	/**
	 * Constructor.
	 */
	private WsUserMgr() {
		tokenTime = Integer.parseInt(ResourceBundle.getBundle("com.cgi.commons.rest").getString("ws.token.validity.time"));
	}

	/**
	 * Get the existing instance of WsUserMgr or create a new one if no existing.
	 *
	 * @return instance
	 */
	public static synchronized WsUserMgr getInstance() {
		if (instance == null) {
			instance = new WsUserMgr();
		}
		return instance;
	}

	/**
	 * Get the custom user and security functions for params, create a authentification token.
	 *
	 * @param login
	 *            The Login.
	 * @return WSUser containing custom User, security function list, token and token expiration date
	 */
	public WsUser createUser(WsLogin login) {
		AbstractSecurityManager secuMgr = SecurityUtils.getSecurityManager();
		RequestContext requestContext = DefaultSecurityManager.getAuthContext();

		try {
			// get Custom user
			User appUser = secuMgr.getUser(login.login, login.password, requestContext);
			if (appUser == null) {
				return null;
			}
			// create new WsUser
			JwtGenerator<JwtProfile> generator = new JwtGenerator<JwtProfile>(SecurityConfigFactory.JWT_SALT, false);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, tokenTime);
			Date expiration = cal.getTime();
			JwtProfile jwtProfile = appUser.toJwtProfile();
			jwtProfile.addAttribute(JwtConstants.EXPIRATION_TIME, expiration);
			String token = generator.generate(jwtProfile);
			WsUser newUser = new WsUser(appUser, token, expiration, secuMgr.getSecurity(appUser, requestContext));
			return newUser;
		} finally {
			requestContext.close();
		}
	}

	/**
	 * Retrieve the context associated to the request.
	 * 
	 * @param request
	 * @return
	 */
	public RequestContext getRequestContext(HttpServletRequest request) {
		J2EContext pac4JContext = new J2EContext(request, null);
		ProfileManager<JwtProfile> manager = new ProfileManager<JwtProfile>(pac4JContext);
		JwtProfile userProfile = manager.get(false);

		final User user = new User(userProfile, request.getLocale());
		RequestContext ctx = new RequestContext(user);
		ctx.setHttpServletRequest(request);
		return ctx;
	}
}
