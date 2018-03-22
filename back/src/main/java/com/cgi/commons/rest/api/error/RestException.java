package com.cgi.commons.rest.api.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import com.cgi.commons.ref.data.Message.Severity;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * Exception for Web Services Rest.
 */
public class RestException extends WebApplicationException {

	/** Serial Id. */
	private static final long serialVersionUID = 2655220135210185291L;

	/**
	 * Constructor.
	 *
	 * @param message Message.
	 * @param isTechnical Indicates if the error is technical.
	 */
	@Deprecated
	public RestException(String message, boolean isTechnical) {
		super(Response
				.status(isTechnical ? INTERNAL_SERVER_ERROR : BAD_REQUEST)
				.entity(new ErrorMessage(isTechnical ? INTERNAL_SERVER_ERROR.getStatusCode() : BAD_REQUEST.getStatusCode(),0L, message, Severity.ERROR.toString()))
				.type(APPLICATION_JSON_TYPE)
				.build());
	}

	public RestException(StatusType status, long code, String message) {
		super(Response
				.status(status)
				.entity(new ErrorMessage(status.getStatusCode(),code, message, Severity.ERROR.toString()))
				.type(APPLICATION_JSON_TYPE)
				.build());
	}
}
