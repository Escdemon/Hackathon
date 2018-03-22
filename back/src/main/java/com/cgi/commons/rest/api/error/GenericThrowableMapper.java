package com.cgi.commons.rest.api.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.cgi.commons.ref.data.Message.Severity;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * Transform {@link java.lang.Throwable Throwable} to json in response.
 */
@Provider
public class GenericThrowableMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable throwable) {
        ErrorMessage errorMessage = new ErrorMessage(INTERNAL_SERVER_ERROR.getStatusCode(), 10001L, throwable.getMessage(), Severity.ERROR.toString());
        return Response
                .status(INTERNAL_SERVER_ERROR)
                .entity(errorMessage)
                .type(APPLICATION_JSON_TYPE)
                .build();
    }
}
