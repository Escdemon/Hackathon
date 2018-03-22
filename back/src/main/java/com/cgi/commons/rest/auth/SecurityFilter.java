package com.cgi.commons.rest.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.log4j.Logger;
import org.pac4j.core.exception.TechnicalException;

/**
 * Security filter.
 * Avoid security for options method.
 */
public class SecurityFilter extends org.pac4j.j2e.filter.RequiresAuthenticationFilter {

	private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = new JsonErrorResponse((HttpServletResponse) response);

		if (!"options".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
			try {
				super.doFilter(request, httpResponse, chain);
			} catch (TechnicalException e) {
				LOGGER.warn(e);
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		}
	}

	/**
	 * Class {@code org.pac4j.core.context.J2EContext}, used by {@codeRequiresAuthenticationFilter} if the authentication fails, calls
	 * {@code HttpServletResponse.sendError}. So this method is overridden to send a JSON response if status code is {@code 401} or {@code 403}.
	 */
	private static class JsonErrorResponse extends HttpServletResponseWrapper {
		public JsonErrorResponse(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendError(int sc) throws IOException {
			sendError(sc, null);
		}

		@Override
		public void sendError(int sc, String msg) throws IOException {
			if (HttpServletResponse.SC_FORBIDDEN == sc || HttpServletResponse.SC_UNAUTHORIZED == sc) {
				HttpServletResponse httpResponse = (HttpServletResponse) getResponse();
				httpResponse.setStatus(sc);
				StringBuilder sb = new StringBuilder(80);
				sb.append("{\"status\":").append(sc).append(",");
				sb.append("\"code\":").append(sc).append(",");
				if (msg != null) {
					sb.append("\"message\":").append("\"").append(msg).append("\"").append(",");
				} else {
					sb.append("\"message\":").append(sc == HttpServletResponse.SC_FORBIDDEN ? "\"Forbidden\"" : "\"Unauthorized\"").append(",");
				}
				sb.append("\"severity\":\"danger\"}");
				httpResponse.getWriter().println(sb.toString());
			} else {
				super.sendError(sc);
			}
		}

	}

}
