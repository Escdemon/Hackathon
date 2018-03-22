package com.cgi.commons.rest.api;

import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.cgi.commons.rest.api.error.RestException;
import com.cgi.commons.rest.auth.WsLogin;
import com.cgi.commons.rest.auth.WsUser;
import com.cgi.commons.rest.auth.WsUserMgr;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.models.endpoint.AbstractEndpoint;

/**
 * Web Service for Authentication.
 */
@Path("auth")
@Produces({ "application/json" })
@Consumes("application/json")
public class WsAuth {
	private static final Logger LOGGER = Logger.getLogger(AbstractEndpoint.class);

	/**
	 * Method for login.
	 * 
	 * @param login The Login.
	 * @return The User logged.
	 */
	@POST
	@Path("/login")
	public Response login(WsLogin login) {
		WsUser user = null;
		try {
			WsUserMgr userMgr = WsUserMgr.getInstance();
			user = userMgr.createUser(login);
		} catch (Exception e) {
			LOGGER.error("Error during login process for user " + login.login, e);
		}
		if (user == null) {
			LOGGER.error("Login failed for user " + login.login);
			throw new RestException(Status.FORBIDDEN, 403L, MessageUtils.getInstance((Locale) null).getLabel("login.error.wrongInfos"));
		}
		return Response
				.ok()
				.entity(user)
				.header(HttpHeaders.CONTENT_LANGUAGE, user.getUser().getLocale().toLanguageTag())
				.build();
	}

}
