package com.cgi.business.application;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Utility class to serialize User objects
 */
public class UserSerializer extends JsonSerializer<User> {

	/**
	 * Serializes a User instance into JSON 
	 * Default implementation will serialize login only
	 */
	@Override
	public void serialize(User user, JsonGenerator gen, SerializerProvider arg2) throws IOException, JsonProcessingException {
		String userString = user.getLogin();
		gen.writeString(userString);
	}

}
