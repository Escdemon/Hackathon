package com.cgi.models.endpoint;

import com.cgi.business.application.SecurityManager;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.rest.auth.WsUserMgr;
import com.cgi.commons.security.SecurityFunction;
import org.apache.log4j.Logger;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.jwt.profile.JwtProfile;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.List;

import static java.lang.String.format;

@Path("auth/security-functions")
public class SecurityFunctionEndpoint {

    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger(SecurityFunctionEndpoint.class);

    /**
     * Give all security function define into {@link SecurityManager#getSecurity(com.cgi.business.application.User ,RequestContext)}.
     *
     * @param httpRequest the http request.
     * @return Rest response with the list of the security function.
     */
    @GET
    public List<SecurityFunction> get(@Context HttpServletRequest httpRequest) {
        try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
            return new SecurityManager().getSecurity(context.getUser(), context);
        } catch (Exception ex) {
            JwtProfile jwtProfile = (JwtProfile) httpRequest.getAttribute(Pac4jConstants.USER_PROFILE);
            logger.error(format("Cannot get security function for user %s", jwtProfile.getUsername()), ex);
            throw ex;
        }
    }
}
