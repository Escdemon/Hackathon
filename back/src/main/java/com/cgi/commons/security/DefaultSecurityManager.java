package com.cgi.commons.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.cgi.business.application.User;
import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.context.RequestContext;

/**
 * Default Security Manager.
 */
public class DefaultSecurityManager extends AbstractSecurityManager {

	/** Serial Id. */
	private static final long serialVersionUID = 4883968451152865426L;

	@Override
	public User getUser(String login, String password, RequestContext ctx) {
		User user = null;

		String hash = SecurityUtils.hash(password);
		String adminHash = SecurityUtils.hash("admin");
		if (adminHash.equals(hash)) {
			user = new User(login);
		}
		return user;
	}

	@Override
	public List<SecurityFunction> getSecurity(User user, RequestContext ctx) {
		List<SecurityFunction> fonctions = new ArrayList<SecurityFunction>();
		return fonctions;
	}

	@Override
	public boolean disableSecurity() {
		return false;
	}

	@Override
	public void initializeAccessRights(RequestContext context) {
		User user = context.getUser();

		// No user => No access
		if (user == null) {
			return;
		}

		List<SecurityFunction> fonctions = getSecurity(user, context);

		for (SecurityFunction f : fonctions) {
			if (f.getAction() != null) {
				HashSet<String> act = (HashSet<String>) user.getActions().get(f.getEntite());
				if (act == null) {
					act = new HashSet<String>();
				}
				act.add(f.getAction());
				user.getActions().put(f.getEntite(), act);
			} else if (f.getQuery() != null) {
				user.getLists().add(f.getQuery());
			} else if (f.getMenu() != null) {
				user.getMenus().add(f.getMenu());
			} else if (f.getMenuOption() != null) {
				user.getMenusOptions().add(f.getMenuOption());
			}
		}
	}

	@Override
	public boolean isListRendered(String queryName, RequestContext ctx) {
		User user = ctx.getUser();
		if (user != null && user.getLists() != null && user.getLists().contains(queryName)) {
			return true;
		}
		return disableSecurity();
	}

	@Override
	public boolean isActionRendered(String entityName, String action, RequestContext context) {
		User user = context.getUser();
		if (action.equals(Constants.SELECT_BR) || action.equals(Constants.DETACH_BR)) {
			// If user is allowed to modify the target entity, he is able to modify its links.
			if (isActionRendered(entityName, Constants.MODIFY, context)) {
				return true;
			}
		}
		if (user != null && user.getActions() != null && user.getActions().get(entityName) != null && user.getActions().get(entityName).contains(action)) {
			return true;
		}
		return disableSecurity();
	}

	@Override
	public boolean isDisplayActionRendered(String entityName, String action, RequestContext context) {
		if (!isActionRendered(entityName, action, context) && isActionRendered(entityName, Constants.DISPLAY, context)) {
			// L'action par défaut est désactivée. On a le droit d'afficher l'action de consultation. On remplace.
			return true;
		}
		return false;
	}

	@Override
	public boolean isNoDefaultActionRendered(String entityName, String action, RequestContext context) {
		if (!isActionRendered(entityName, action, context)
				&& !isActionRendered(entityName, Constants.DISPLAY, context)) {
			// L'action par défaut est désactivée et on a pas le droit d'afficher l'action de consultation.
			// On affiche pas de lien d'action dans la liste.
			return true;
		}
		return false;
	}

	@Override
	public boolean isOptionMenuRendered(String optMenuName, RequestContext ctx) {
		User user = ctx.getUser();
		if (user != null && user.getMenusOptions() != null && user.getMenusOptions().contains(optMenuName)) {
			return true;
		}
		return disableSecurity();
	}

	/**
	 * Get the Security Function for a Query.
	 * 
	 * @param entite Main Entity.
	 * @param query Query Name.
	 * @return The Security Function.
	 */
	protected SecurityFunction getQuerySecurityFunction(String entite, String query) {
		return getSecurityFunction(entite, null, query);
	}

	/**
	 * Get the Security Function for a Action.
	 * 
	 * @param entite Main Entity.
	 * @param action Code of the Action.
	 * @return The Security Function.
	 */
	protected SecurityFunction getActionSecurityFunction(String entite, String action) {
		return getSecurityFunction(entite, action, null);
	}

	/**
	 * Get the Security Function.
	 * 
	 * @param entite Main Entity.
	 * @param action Code of the Action.
	 * @param query Query Name.
	 * @return The Security Function.
	 */
	protected SecurityFunction getSecurityFunction(String entite, String action, String query) {
		SecurityFunction sf = new SecurityFunction();
		sf.setEntite(entite);
		if (query != null)
			sf.setQuery(query);
		if (action != null)
			sf.setAction(action);
		return sf;
	}

	/**
	 * Instantiates a stub context that can be used for authentication purpose. Do not forget to CLOSE IT !
	 * 
	 * @return A request context based on a fake User.
	 */
	public static RequestContext getAuthContext() {
		RequestContext requestContext = new RequestContext(new User("AUTH"));
		return requestContext;
	}
}
