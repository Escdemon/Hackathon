package com.cgi.commons.rest.api.error;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

/**
 * A runtime application error indicating a client request error (HTTP {@code 403} Forbidden).
 */
public class ForbiddenRequestException extends RestException {
    public ForbiddenRequestException(long code, String message) {
        super(FORBIDDEN, code, message);
    }
}
