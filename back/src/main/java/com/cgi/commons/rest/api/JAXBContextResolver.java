package com.cgi.commons.rest.api;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.cgi.commons.rest.EndpointConstants;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * Context Resolver for JAXB.
 */
@Provider
public class JAXBContextResolver implements ContextResolver<ObjectMapper> {

	/** JAXB Context. */
	private ObjectMapper context;

	/**
	 * Constructor.
	 * @throws Exception If error.
	 */
	public JAXBContextResolver() {
		AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
		this.context = new ObjectMapper();
		this.context.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
		this.context.configure(SerializationFeature.INDENT_OUTPUT, true);
		this.context.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		this.context.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
		this.context.setConfig(this.context.getDeserializationConfig().withInsertedAnnotationIntrospector(jacksonIntrospector));
		this.context.setConfig(this.context.getSerializationConfig().withInsertedAnnotationIntrospector(jacksonIntrospector));
	}

	@Override
	public ObjectMapper getContext(Class<?> objectType) {
		return context;

	}
}
