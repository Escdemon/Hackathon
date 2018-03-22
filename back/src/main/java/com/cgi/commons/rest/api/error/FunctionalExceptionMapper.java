package com.cgi.commons.rest.api.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.cgi.commons.ref.data.Message;
import com.cgi.commons.utils.FunctionalException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.ArrayList;
import java.util.List;

/**
 * Transform {@link com.cgi.commons.utils.FunctionalException FunctionalException} to json in response.
 */
@Provider
public class FunctionalExceptionMapper implements ExceptionMapper<FunctionalException> {
    @Override
    public Response toResponse(FunctionalException exception) {
    	List<Message> messages = exception.getMessages();
    	List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
    	for (Message m : messages) {
    		ErrorMessage errorMessage = new ErrorMessage(BAD_REQUEST.getStatusCode(), 20001L, m.getMessage() , m.getSeverity().toString());
    	    errors.add(errorMessage);
    	}
         return Response
                .status(BAD_REQUEST)
                .entity(errors)
                .type(APPLICATION_JSON_TYPE)
                .build();
    }
}
