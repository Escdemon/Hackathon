package com.cgi.commons.rest.api.error;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.cgi.commons.ref.data.Message.Severity;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Transform {@link javax.ws.rs.WebApplicationException WebApplicationException} to json in response.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        Response exceptionResponse = exception.getResponse();
        ErrorMessage errorMessage = new ErrorMessage(exceptionResponse.getStatus(), 30001L, String.format("Error %s", exception.getMessage()),Severity.ERROR.toString());
        return Response.fromResponse(exceptionResponse)
                .entity(errorMessage)
                .type(APPLICATION_JSON_TYPE)
                .build();
    }
}
