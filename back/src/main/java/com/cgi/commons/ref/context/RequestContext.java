package com.cgi.commons.ref.context;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.cgi.business.application.User;
import com.cgi.commons.db.DbConnection;
import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.controller.BusinessController;
import com.cgi.commons.ref.data.Message;
import com.cgi.commons.utils.ApplicationUtils;


/**
 * The request context.
 */
public class RequestContext implements Serializable, Closeable {

	/** Serial id. */
	private static final long serialVersionUID = 8336236497355320836L;

	/** Specific attribute used to force postLoad calls when loading entities in lists */
	public static final String FORCE_POST_LOAD = "FORCE_POST_LOAD";

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(RequestContext.class);


	/** The application context associated. This is the "parent" context. */
	private final ApplicationContext appContext;

	/** The user logged on this request. */
	private final User user;

	/** Messages (information and errors). This is a LinkedHashSet because we want to keep the message order, but they should be unique. */
	private final Set<Message> messages = new LinkedHashSet<Message>();

	/** Request attributes. This may be used to store anything during request */
	private final Map<String, Object> attributes = new HashMap<String, Object>();

	/** Current HTTP Request. */
	private transient HttpServletRequest httpServletRequest;

	/** Database connection. Lazy opening. */
	private transient DbConnection dbConnection;

	/** File attached to the Request. If a File object goes up to the UI layer of the application, it should start a download of this file. */
	private File attachment = null;
	/** Attachement name is the filename to use if there's a file to download. */
	private String attachmentName = null;

	/** Count queries executed with this context */
	private int queryNbRequests = 0;

	/**
	 * Creates a new Request Context linked to a Session Context.
	 */
	public RequestContext(User user) {
		super();
		this.appContext = ApplicationUtils.getApplicationContext();
		this.user = user;
		attributes.put(FORCE_POST_LOAD, Boolean.FALSE);
	}

	/**
	 * Close the context.
	 */
	@Override
	public void close() {
		if (dbConnection != null) {
			dbConnection.close();
			dbConnection = null;
		}
	}

	/**
	 * Getter of the application context.
	 * 
	 * @return The application context.
	 */
	public ApplicationContext getAppContext() {
		return appContext;
	}

	/**
	 * Getter of the user.
	 * 
	 * @return The user.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Check if a DbConnection object is associated to this request.
	 * 
	 * @return true if a DbConnection object is associated to this request.
	 */
	public boolean hasDbConnection() {
		return dbConnection != null;
	}

	/**
	 * Always return a DbConnection. If needed, a new DbConnection is created.
	 * 
	 * @return never null
	 */
	public DbConnection getDbConnection() {
		if (dbConnection == null) {
			dbConnection = new DbConnection();
		}
		return dbConnection;
	}

	/**
	 * Set a DbConnection to the request context.
	 * 
	 * @param dbConnection
	 *            The DbConnection to set.
	 */
	public void setDbConnection(DbConnection dbConnection) {
		this.dbConnection = dbConnection;
	}

	/**
	 * Getter of the messages.
	 * 
	 * @return The messages.
	 */
	public Set<Message> getMessages() {
		return messages;
	}

	/**
	 * Getter of the attributes.
	 * 
	 * @return The attributes.
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * Removes a user custom data from current request context.
	 * 
	 * @param key
	 *            Unique identifier for custom data
	 */
	public void removeCustomData(String key) {
		String newKey = Constants.CUSTOM_DATA + key;
		attributes.remove(newKey);
	}

	/**
	 * Stores a user custom data inside current context. If this request's response has a User Interface, this custom data will be dump into
	 * final HTML DOM in the custom data div. <br/>
	 * <br/>
	 * 
	 * <pre>
	 * Example: 
	 * - When Domain Logic method uiActionOnLoad() adds custom data: 
	 * 		context.putCustomData("color", "blue")
	 * - Final DOM will contain a div like this: 
	 * 		&lt;div style="display: none;"&gt;
	 * 			&lt;div id="cData_key_color"&gt;blue&lt;/div&gt;
	 * 		&lt;/div&gt;
	 * </pre>
	 * 
	 * Request Context will add "cData_key_" prefix to all stored custom data.
	 * 
	 * @param key
	 *            Unique identifier for custom data
	 * @param data
	 *            Custom data to store inside context
	 */
	public void putCustomData(String key, Object data) {
		String newKey = Constants.CUSTOM_DATA + key;
		attributes.put(newKey, data);
	}

	/**
	 * Gets a user stored custom data inside current context.
	 * 
	 * @param key
	 *            Unique identifier for custom data
	 * @return Custom data stored inside context
	 */
	public Object getCustomData(String key) {
		return attributes.get(Constants.CUSTOM_DATA + key);
	}

	/**
	 * Set an attachment.
	 * 
	 * @param file
	 *            The file to attach.
	 */
	public void setAttachment(File file) {
		this.attachment = file;
	}

	/**
	 * Set an attachment. If the filename is null "tmpAttachment" will be set by default.
	 * 
	 * @param data
	 *            The content of the attachment.
	 * @param filename
	 *            The filename of the attachment.
	 */
	public void setAttachment(byte[] data, String filename) {
		if (filename == null) {
			filename = "tmpAttachment";
		}
		setAttachmentName(filename);
		FileOutputStream out;
		try {
			attachment = File.createTempFile(filename, ".tmp");
			out = new FileOutputStream(attachment);
			out.write(data);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Getter of the attachment.
	 * 
	 * @return The attachment.
	 */
	public File getAttachment() {
		return attachment;
	}

	/**
	 * Return an <b>unmodifiable</b> map with custom data. To modify it use <br/>
	 * {@link #putCustomData(String, Object)} or {@link #removeCustomData(String)}
	 * 
	 * @return an unmodifiable map with custom data.
	 */
	public Map<String, Object> getCustomData() {
		Map<String, Object> customAttributes = new HashMap<String, Object>();
		for (String key : attributes.keySet()) {
			if (key.startsWith(Constants.CUSTOM_DATA)) {
				customAttributes.put(key, attributes.get(key));
			}
		}
		customAttributes = Collections.unmodifiableMap(customAttributes);
		return customAttributes;
	}

	/**
	 * Cleans current customData by removing errorFields
	 */
	public void cleanCustomData() {
		Set<String> customDataToClean = new HashSet<String>();
		for (Entry<String, Object> entry : attributes.entrySet()) {
			if (entry.getKey().startsWith("cData_key_errorField")) {
				customDataToClean.add(entry.getKey());
			}
		}
		for (String k : customDataToClean) {
			attributes.remove(k);
		}
	}

	/**
	 * Shortcut method to set FORCE_POST_LOAD boolean in request context attributes. Default behavior is false.
	 * 
	 * @param forcePostLoad
	 *            if <code>true</code>, this will set FORCE_POST_LOAD boolean to true in current request context attributes. This will trigger
	 *            calls to dbPostLoad methods when loading entities via DB.getLinkedEntities() and DB.getList(). <b>In this case, this parameter
	 *            remains set to true as long as you don't re-set it to false in the current RequestContext !</b><br/>
	 *            if <code>false</code>, this will reset the FORCE_POST_LOAD boolean attribute to false.
	 */
	public void forcePostLoad(boolean forcePostLoad) {
		attributes.put(FORCE_POST_LOAD, Boolean.valueOf(forcePostLoad));
	}

	/**
	 * Returns current value of FORCE_POST_LOAD attribute
	 * 
	 * @return <code>true</code> if attributes.get(FORCE_POST_LOAD) is set to true, <code>false</code> otherwise
	 */
	public boolean isForcePostLoad() {
		Boolean forcePostLoad = (Boolean) attributes.get(FORCE_POST_LOAD);
		if (forcePostLoad == null) {
			forcePostLoad = Boolean.FALSE;
		}
		return forcePostLoad;
	}

	/**
	 * Getter of the attachment name.
	 * 
	 * @return The attachment name.
	 */
	public String getAttachmentName() {
		return attachmentName;
	}

	/**
	 * Setter of the attachment name.
	 * 
	 * @param attachmentName
	 *            The attachment name.
	 */
	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	/**
	 * Adds an error field to current custom data
	 * 
	 * @param linkPath
	 *            LinkPath to field
	 * @param entityName
	 *            Field entity
	 * @param varName
	 *            Field varName
	 */
	public void addErrorField(String linkPath, String entityName, String varName) {
		if (linkPath == null) {
			linkPath = "";
		}
		putCustomData("errorField." + (linkPath.length() > 0 ? (linkPath + ".") : "") + entityName + "." + varName, Boolean.TRUE);
	}

	/**
	 * Adds an error field to current custom data based on current linkpath
	 * 
	 * @param entityName
	 *            Field entity
	 * @param varName
	 *            Field varName
	 */
	public void addErrorField(String entityName, String varName) {
		addErrorField((String) getCustomData(BusinessController.ERROR_FIELD_LINK_PATH), entityName, varName);
	}

	/**
	 * Removes an error field to current custom data
	 * 
	 * @param linkPath
	 *            LinkPath to field
	 * @param entityName
	 *            Field entity
	 * @param varName
	 *            Field varName
	 */
	public void removeErrorField(String linkPath, String entityName, String varName) {
		if (linkPath == null) {
			linkPath = "";
		}
		removeCustomData("errorField." + (linkPath.length() > 0 ? (linkPath + ".") : "") + entityName + "." + varName);
	}

	/**
	 * Removes an error field to current custom data based on current linkpath
	 * 
	 * @param entityName
	 *            Field entity
	 * @param varName
	 *            Field varName
	 */
	public void removeErrorField(String entityName, String varName) {
		removeErrorField((String) getCustomData(BusinessController.ERROR_FIELD_LINK_PATH), entityName, varName);
	}

	public int getQueryNbRequests() {
		return queryNbRequests;
	}

	public void incQueryNbRequests() {
		queryNbRequests++;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}
}
