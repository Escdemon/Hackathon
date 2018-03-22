package com.cgi.commons.rest.api.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represent error message.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorMessage {
    private int status;
    private long code;
    private String message;
    private String severity;

    public ErrorMessage(int status, long code, String message, String severity) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.setSeverity(severity);
    }

    /**
     * Status error.
     * @return status error.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Error code.
     * @return error code.
     */
    public long getCode() {
        return code;
    }

    /**
     * Message of error.
     * @return message of error.
     */
    public String getMessage() {
        return message;
    }

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}
}
