package com.cgi.commons.rest.api.error;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * A runtime application error indicating a client request error (HTTP {@code 404} status code).
 */
public class NotFoundException extends RestException {
    public NotFoundException(long code, String message) {
        super(NOT_FOUND, code, message);
    }
}
