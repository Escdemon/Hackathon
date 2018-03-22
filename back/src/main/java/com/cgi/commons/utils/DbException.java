package com.cgi.commons.utils;

import com.cgi.commons.utils.TechnicalException;

/**
 * The {@code DbException} exception is thrown if an unexpected error occurs in the database layer.
 * <p>
 * The cause can be a {@code SQLException} exception but it is not a requirement.
 * </p>
 */
public class DbException extends TechnicalException {

    /**
     * SerialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new exception with the given message.
     * @param message The detail message of this exception.
     */
    public DbException(String message) {
        super(message);
    }

    /**
     * Create a new exception with the given message and cause.
     * @param message The detail message of this exception.
     * @param cause The cause of this exception.
     */
    public DbException(String message, Throwable cause) {
        super(message, cause);
    }

}
