package com.cgi.commons.security;

import java.io.Serializable;
import java.util.List;

import com.cgi.business.application.User;
import com.cgi.commons.ref.context.RequestContext;

/**
 * Abstract class in order to implement a Security Manager.
 */
public abstract class AbstractSecurityManager implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = -5307524579033289661L;

	/**
	 * Login and returns the user logged.
	 * 
	 * @param login The login.
	 * @param password The Password.
	 * @param ctx Current Context.
	 * @return The User if couple login/password is correct.
	 */
	public abstract User getUser(String login, String password, RequestContext ctx);

	/**
	 * Returns the security functions allowed to an user.
	 * 
	 * @param user The User.
	 * @param ctx Current Context.
	 * @return The List of the security functions allowed.
	 */
	public abstract List<SecurityFunction> getSecurity(User user, RequestContext ctx);

	/**
	 * Indicates if security is enabled or disabled.
	 * 
	 * @return <code>true</code> if security is disabled.
	 */
	public abstract boolean disableSecurity();

	/**
	 * Indicates if list is rendered for the current User.
	 * 
	 * @param queryName Name of the Query.
	 * @param ctx Current Context.
	 * @return <code>true</code> if list is rendered.
	 */
	public abstract boolean isListRendered(String queryName, RequestContext ctx);

	/**
	 * Indicates if action is rendered for the current User.
	 * 
	 * @param entityName Name of the Entity.
	 * @param action Code of the Action.
	 * @param context Current Context.
	 * @return <code>true</code> if action is rendered.
	 */
	public abstract boolean isActionRendered(String entityName, String action, RequestContext context);

	/**
	 * Indicates if display action is rendered for the current User.
	 * 
	 * @param entityName Name of the Entity.
	 * @param action Code of the Action.
	 * @param context Current Context.
	 * @return <code>true</code> if display action is rendered.
	 */
	public abstract boolean isDisplayActionRendered(String entityName, String action, RequestContext context);

	/**
	 * Indicates if default action is rendered for the current User.
	 * 
	 * @param entityName Name of the Entity.
	 * @param action Code of the Action.
	 * @param context Current Context.
	 * @return <code>true</code> if default action is rendered.
	 */
	public abstract boolean isNoDefaultActionRendered(String entityName, String action, RequestContext context);

	/**
	 * Indicates if Option Menu is rendered for the current User.
	 * 
	 * @param optMenuName Name of the Option Menu.
	 * @param ctx Current Context.
	 * @return <code>true</code> if Option Menu is rendered.
	 */
	public abstract boolean isOptionMenuRendered(String optMenuName, RequestContext ctx);

	/**
	 * Initialize Access Rights for the current User.
	 * 
	 * @param context Current Context.
	 */
	public abstract void initializeAccessRights(RequestContext context);
}
