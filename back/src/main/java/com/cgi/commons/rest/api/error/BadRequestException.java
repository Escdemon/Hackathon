package com.cgi.commons.rest.api.error;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * A runtime application error indicating a client request error (HTTP {@code 400} Bad request).
 */
public class BadRequestException extends RestException {
    public BadRequestException(long code, String message) {
        super(BAD_REQUEST, code, message);
    }
}
