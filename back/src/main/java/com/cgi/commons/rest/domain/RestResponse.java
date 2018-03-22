package com.cgi.commons.rest.domain;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.data.Message;
import com.cgi.commons.ref.entity.FileContainer;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.TmpFileManager;

/**
 * Response send to the REST client
 */
public class RestResponse {
	/** True request response, wrapped in this object */
	private Object content;
	/** Messages to display to the user */
	private Set<Message> messages;
	/** File name to download */
	private String downloadFilename;

	/**
	 * Init an empty response.
	 * 
	 * @param context
	 *            Current request context
	 */
	public RestResponse(RequestContext context) {
		this.messages = context.getMessages();
		if (context.getAttachment() != null) {
			String fileName;

			if (context.getAttachmentName() != null) {
				fileName = context.getAttachmentName();
			} else {
			    fileName = context.getAttachment().getName();
			}

			// Move file to a temp folder
			try (FileInputStream fis = new FileInputStream(context.getAttachment())) {
			    TmpFileManager manager = new TmpFileManager(fis, fileName);
			    FileContainer fc = manager.createFile(false, true);
				this.downloadFilename = fc.getUuid();

			} catch (IOException e) {
				throw new TechnicalException("Error while moving file " + fileName, e);
			}
		}
	}

	/**
	 * Init a response with the given content.
	 * 
	 * @param content
	 *            Content to send
	 * @param context
	 *            Current request context
	 */
	public RestResponse(Object content, RequestContext context) {
		this(context);
		this.content = content;
	}

	public Set<Message> getMessages() {
		return messages;
	}

	public void setMessages(Set<Message> messages) {
		this.messages = messages;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public String getDownloadFilename() {
		return downloadFilename;
	}

	public void setDownloadFilename(String downloadFilename) {
		this.downloadFilename = downloadFilename;
	}

}
