package com.cgi.commons.security;

import java.io.Serializable;

/**
 * Security Function.
 */
public class SecurityFunction implements Serializable {
	/** Serial Id. */
	private static final long serialVersionUID = 1L;

	/** Entity. */
	private String entite;

	/** Action. */
	private String action;

	/** Query. */
	private String query;
	
	/** Menu. */
	private String menu;
	
	/** Menu Option. */
	private String menuOption;

	public String getEntite() {
		return entite;
	}

	public void setEntite(String entite) {
		this.entite = entite;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
	}

	public String getMenuOption() {
		return menuOption;
	}

	public void setMenuOption(String menuOption) {
		this.menuOption = menuOption;
	}

	public static SecurityFunction getQueryFunction(String entityName, String queryName) {
		SecurityFunction f = new SecurityFunction();
		f.setEntite(entityName);
		f.setQuery(queryName);
		return f;
	}

	public static SecurityFunction getActionFunction(String entityName, String actionName) {
		SecurityFunction f = new SecurityFunction();
		f.setEntite(entityName);
		f.setAction(actionName);
		return f;
	}

	public static SecurityFunction getMenuFunction(String menu) {
		SecurityFunction f = new SecurityFunction();
		f.setMenu(menu);
		return f;
	}

	public static SecurityFunction getMenuOptionFunction(String menuOption) {
		SecurityFunction f = new SecurityFunction();
		f.setMenuOption(menuOption);
		return f;
	}
}
