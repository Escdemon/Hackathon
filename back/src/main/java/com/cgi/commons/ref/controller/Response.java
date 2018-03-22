package com.cgi.commons.ref.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.data.UiAccess;

/**
 * A Response.
 *
 * @param <E> The main entity of the request.
 */
public class Response<E extends Entity> implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -3242223539701244408L;

	/** The entity name. */
	protected String entityName;
	/** The entity. */
	protected E entity;
	
	/** The action. */
	protected Action action;
	/** The keys. */
	protected List<Key> keys;
	/** The page name. */
	protected String pageName;
	/** The query name. */
	protected String queryName;
	/** The title. */
	protected String title;
	/** The UI access of the variables of the entity. */
	protected Map<String, UiAccess> uiAccess;
	/** The custom datas. */
	protected Map<String, Object> customData;

	/** The link name. */
	protected String linkName;
	/** The linked entity. */
	protected Entity linkedEntity;
	/** If the request is back ref. */
	protected boolean backRef;

	/** The entity name for the next action. */
	protected String remEntityName;
	/** The next action. */
	protected Action remAction;
	/** The keys for the next action. */
	protected List<Key> remKeys;

	/** 
	 * Constructor.
	 */
	public Response() {

	}
	
	private Response(String entityName, Action action, List<Key> keys, String queryName, String linkName, boolean backRef) {
		this.entityName = entityName;
		this.action = action;
		this.keys = keys;
		this.queryName = queryName;
		this.linkName = linkName;
		this.backRef = backRef;
	}

	/**
	 * Simple constructor to create a response to an <b>action page</b>.
	 * 
	 * @param entityName The entity name.
	 * @param action The action.
	 * @param keys The keys.
	 */
	public Response(String entityName, Action action, List<Key> keys) {
		this(entityName, action, keys, null, null, false);
	}

	/**
	 * Advanced constructor to create a response to an <b>action page</b>.<br/>
	 * Given entity will be displayed on the action page.
	 * 
	 * @param entityName The entity name.
	 * @param action The action.
	 * @param keys The keys.
	 * @param entity The entity to use on the page.
	 */
	public Response(String entityName, Action action, List<Key> keys, E entity) {
		this(entityName, action, keys, null, null, false);
		this.entity = entity;
	}

	/**
	 * Simple constructor to create a response to a <b>list page</b>.
	 * 
	 * @param entityName The entity name.
	 * @param queryName The query name.
	 */
	public Response(String entityName, String queryName) {
		this(entityName, null, null, queryName, null, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param request A request.
	 */
	public Response(Request<E> request) {
		this.entityName = request.getEntityName();
		this.action = request.getAction();
		this.entity = request.getEntity();
		this.keys = request.getKeys();
		this.queryName = request.getQueryName();
		this.linkName = request.getLinkName();
		this.linkedEntity = request.getLinkedEntity();
		this.backRef = request.isBackRef();
	}

	/**
	 * Creates a request for business controller to ask server validation of the current view.
	 * 
	 * @param context The requestContext to put in the request
	 * @return A Request containing view metadata and possible user input.
	 */
	public Request<E> toValidationRequest(RequestContext context) {
		Request<E> request = new Request<E>(entityName, action, keys, queryName, linkName, backRef);
		request.setEntity(entity);
		request.setLinkedEntity(linkedEntity);
		request.setContext(context);
		return request;
	}

	/**
	 * Getter for the entityName.
	 * @return the entityName.
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * Setter for the entityName.
	 * @param entityName the entityName.
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Getter for the entity.
	 * @return the entity.
	 */
	public E getEntity() {
		return entity;
	}

	/**
	 * Setter for the entity.
	 * @param entity the entity.
	 */
	public void setEntity(E entity) {
		this.entity = entity;
	}

	/**
	 * Getter for the action.
	 * @return the action.
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Setter for the action.
	 * @param action the action.
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * Getter for the keys.
	 * @return the keys.
	 */
	public List<Key> getKeys() {
		return keys;
	}

	/**
	 * Setter for the keys.
	 * @param keys the keys.
	 */
	public void setKeys(List<Key> keys) {
		this.keys = keys;
	}

	/**
	 * Add a key to the keys list
	 * @param key the key to add
	 */
	public void addKey(Key key) {
		if (this.keys == null) {
			this.keys = new ArrayList<Key>();
		}
		this.keys.add(key);
	}

	/**
	 * Getter for the pageName.
	 * @return the pageName.
	 */
	public String getPageName() {
		return pageName;
	}

	/**
	 * Setter for the pageName.
	 * @param pageName the pageName.
	 */
	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	/**
	 * Getter for the queryName.
	 * @return the queryName.
	 */
	public String getQueryName() {
		return queryName;
	}

	/**
	 * Setter for the queryName.
	 * @param queryName the queryName.
	 */
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	/**
	 * Getter for the title.
	 * @return the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter for the title.
	 * @param title the title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Getter for the uiAccess.
	 * @return the uiAccess.
	 */
	public Map<String, UiAccess> getUiAccess() {
		return uiAccess;
	}

	/**
	 * Setter for the uiAccess.
	 * @param uiAccess the uiAccess.
	 */
	public void setUiAccess(Map<String, UiAccess> uiAccess) {
		this.uiAccess = uiAccess;
	}

	/**
	 * Getter for the customData.
	 * @return the customData.
	 */
	public Map<String, Object> getCustomData() {
		return customData;
	}

	/**
	 * Setter for the customData.
	 * @param customData the customData.
	 */
	public void setCustomData(Map<String, Object> customData) {
		this.customData = customData;
	}

	/**
	 * Getter for the linkName.
	 * @return the linkName.
	 */
	public String getLinkName() {
		return linkName;
	}

	/**
	 * Setter for the linkName.
	 * @param linkName the linkName.
	 */
	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	/**
	 * Getter for the linkedEntity.
	 * @return the linkedEntity.
	 */
	public Entity getLinkedEntity() {
		return linkedEntity;
	}

	/**
	 * Setter for the linkedEntity.
	 * @param linkedEntity the linkedEntity.
	 */
	public void setLinkedEntity(Entity linkedEntity) {
		this.linkedEntity = linkedEntity;
	}

	/**
	 * Getter for the backRef.
	 * @return the backRef.
	 */
	public boolean isBackRef() {
		return backRef;
	}

	/**
	 * Setter for the backRef.
	 * @param backRef the backRef.
	 */
	public void setBackRef(boolean backRef) {
		this.backRef = backRef;
	}

	/**
	 * Getter for the remEntityName.
	 * @return the remEntityName.
	 */
	public String getRemEntityName() {
		return remEntityName;
	}

	/**
	 * Setter for the remEntityName.
	 * @param remEntityName the remEntityName.
	 */
	public void setRemEntityName(String remEntityName) {
		this.remEntityName = remEntityName;
	}

	/**
	 * Getter for the remAction.
	 * @return the remAction.
	 */
	public Action getRemAction() {
		return remAction;
	}

	/**
	 * Setter for the remAction.
	 * @param remAction the remAction.
	 */
	public void setRemAction(Action remAction) {
		this.remAction = remAction;
	}

	/**
	 * Getter for the remKeys.
	 * @return the remKeys.
	 */
	public List<Key> getRemKeys() {
		return remKeys;
	}

	/**
	 * Setter for the remKeys.
	 * @param remKeys the remKeys.
	 */
	public void setRemKeys(List<Key> remKeys) {
		this.remKeys = remKeys;
	}

}
