package com.cgi.commons.ref.controller;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.Key;

/**
 * A Request.
 *
 * @param <E> The main entity of the request.
 */
public class Request<E extends Entity> implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -3242223539701244408L;

	/** The entity name. */
	private String entityName;
	/** The entity. */
	private E entity;

	/** The action. */
	private Action action;
	/** The keys. */
	private List<Key> keys;
	/** The query name. */
	private String queryName;

	/** The link name. */
	private String linkName;
	/** If the request is back ref. */
	private boolean backRef;
	/** The linked entity. */
	private Entity linkedEntity;

	/** The request context. */
	private RequestContext context;

	/**
	 * Default constructor.
	 */
	public Request() {

	}

	/**
	 * Simple constructor to create a request to an <b>action page</b> without input.
	 * 
	 * @param context
	 *            The current context.
	 * @param entityName
	 *            The entity name.
	 * @param action
	 *            The action.
	 */
	public Request(RequestContext context, String entityName, Action action) {
		this(entityName, action, null, null, null, false);
		this.context = context;
	}

	/**
	 * Simple constructor to create a request to an <b>action page</b>.
	 * 
	 * @param context
	 *            The current context.
	 * @param entityName
	 *            The entity name.
	 * @param action
	 *            The action.
	 * @param keys
	 *            The keys.
	 */
	public Request(RequestContext context, String entityName, Action action, List<Key> keys) {
		this(entityName, action, keys, null, null, false);
		this.context = context;
	}

	/**
	 * Simple constructor to create a request to an <b>action page</b> for one entry.
	 * 
	 * @param context
	 *            The current context.
	 * @param entityName
	 *            The entity name.
	 * @param action
	 *            The action.
	 * @param pk
	 *            The key.
	 */
	public Request(RequestContext context, String entityName, Action action, Key pk) {
		this(context, entityName, action, pk == null ? null : Arrays.asList(pk));
	}

	/**
	 * Advanced constructor to create a request to an <b>action page</b>.<br/>
	 * Given entity will be displayed on the action page.
	 * 
	 * @param context
	 *            The current context.
	 * @param entityName
	 *            The entity name.
	 * @param action
	 *            The action.
	 * @param keys
	 *            The keys.
	 * @param entity
	 *            The entity to use on the page.
	 */
	public Request(RequestContext context, String entityName, Action action, List<Key> keys, E entity) {
		this(entityName, action, keys, null, null, false);
		this.context = context;
		this.entity = entity;
	}

	/**
	 * Advanced constructor to create a request to an <b>action page</b>.<br/>
	 * Given entity will be displayed on the action page.
	 * 
	 * @param context
	 *            The current context.
	 * @param entityName
	 *            The entity name.
	 * @param action
	 *            The action.
	 * @param pk
	 *            The key.
	 * @param entity
	 *            The entity to use on the page.
	 */
	public Request(RequestContext context, String entityName, Action action, Key pk, E entity) {
		this(context, entityName, action, pk == null ? null : Arrays.asList(pk), entity);
	}

	/**
	 * Advanced constructor to create a request to an <b>action page</b>.<br/>
	 * Given entity will be displayed on the action page.
	 * 
	 * @param context
	 *            The current context.
	 * @param entityName
	 *            The entity name.
	 * @param action
	 *            The action.
	 * @param entity
	 *            The entity to use on the page.
	 */
	public Request(RequestContext context, String entityName, Action action, E entity) {
		this(context, entityName, action, (List<Key>) null, entity);
	}

	/**
	 * Simple constructor to create a request to a <b>list page</b>.
	 * 
	 * @param context
	 *            The current context.
	 * @param entityName
	 *            The entity name.
	 * @param queryName
	 *            The query name.
	 */
	public Request(RequestContext context, String entityName, String queryName) {
		this(entityName, Action.getListAction(queryName, null), null, queryName, null, false);
		this.context = context;
	}

	/**
	 * Generic constructor.
	 * 
	 * @param entityName The entity name.
	 * @param action The action.
	 * @param keys The keys.
	 * @param queryName The query name.
	 * @param linkName The link name.
	 * @param backRef The back ref. 
	 */
	public Request(String entityName, Action action, List<Key> keys, String queryName, String linkName, boolean backRef) {
		this.entityName = entityName;
		this.action = action;
		this.keys = keys;
		this.queryName = queryName;
		this.linkName = linkName;
		this.backRef = backRef;
	}

	/**
	 * Constructor.
	 * 
	 * @param response A response.
	 * @param context The current context.
	 */
	public Request(Response<E> response, RequestContext context) {
		this.entityName = response.getEntityName();
		this.action = response.getAction();
		this.keys = response.getKeys();
		this.queryName = response.getQueryName();
		this.linkName = response.getLinkName();
		this.linkedEntity = response.getLinkedEntity();
		this.entity = response.getEntity();
		this.backRef = response.isBackRef();
		this.context = context;
	}

	/**
	 * Getter of the entity name.
	 * @return The entity name.
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * Setter of the entity name.
	 * @param entityName The entity name.
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Getter for the action.
	 * @return The action.
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Setter for the action.
	 * @param action The action.
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * Getter for the keys.
	 * @return The keys.
	 */
	public List<Key> getKeys() {
		return keys;
	}

	/**
	 * Setter for the keys.
	 * @param keys The keys.
	 */
	public void setKeys(List<Key> keys) {
		this.keys = keys;
	}

	/**
	 * Getter for the query name.
	 * @return The query name.
	 */
	public String getQueryName() {
		return queryName;
	}

	/**
	 * Setter for the query name. 
	 * @param queryName The query name.
	 */
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	/**
	 * Getter for the link name.
	 * @return The link name.
	 */
	public String getLinkName() {
		return linkName;
	}

	/**
	 * Setter for the link name.
	 * @param linkName The link name.
	 */
	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	/**
	 * Getter for the linked entity. 
	 * @return The linked entity.
	 */
	public Entity getLinkedEntity() {
		return linkedEntity;
	}

	/**
	 * Setter for the linked entity.
	 * @param linkedEntity The linked entity.
	 */
	public void setLinkedEntity(Entity linkedEntity) {
		this.linkedEntity = linkedEntity;
	}

	/**
	 * Getter for the request context.
	 * @return The request context.
	 */
	public RequestContext getContext() {
		return context;
	}

	/**
	 * Setter for the request context.
	 * @param context The request context.
	 */
	public void setContext(RequestContext context) {
		this.context = context;
	}

	/**
	 * Getter for the entity.
	 * @return The entity.
	 */
	public E getEntity() {
		return entity;
	}

	/**
	 * Setter for the entity.
	 * @param entity The entity.
	 */
	public void setEntity(E entity) {
		this.entity = entity;
	}

	/**
	 * Getter for the back ref.
	 * @return true if back ref, else false.
	 */
	public boolean isBackRef() {
		return backRef;
	}

	/**
	 * Setter for the back ref.
	 * @param backRef The back ref.
	 */
	public void setBackRef(boolean backRef) {
		this.backRef = backRef;
	}

}
