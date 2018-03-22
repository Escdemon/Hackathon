package com.cgi.commons.utils;

import java.util.List;

import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.data.Message;
import com.cgi.commons.ref.data.Message.Severity;
import com.cgi.commons.utils.FunctionalException;

/**
 * The {@code EntityNotFoundException} exception is thrown if an object is not found into the database.
 * <p>
 * The cause can be a {@code SQLException} exception but it is not a requirement.
 * </p>
 */
public class EntityNotFoundException extends FunctionalException {

	/**
     * SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    
    
    /**
	 * Constructor.
	 * 
	 * @param errors List of messages to add.
	 */
	public EntityNotFoundException(List<Message> errors) {
		super(errors);
	}

	/**
	 * Constructor.
	 * 
	 * @param m Message to add.
	 */
	public EntityNotFoundException(Message m) {
		super(m);
	}
	
    /**
	 * Constructor
	 * The exception is created with its default message
	 * @param ctx context
	 */
	public EntityNotFoundException(RequestContext ctx){
		super(new Message(MessageUtils.getInstance(ctx).getMessage("db.entity.not.found", new Object[] {}), Severity.ERROR));
	}

}
