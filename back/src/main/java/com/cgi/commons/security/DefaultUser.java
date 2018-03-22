package com.cgi.commons.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pac4j.jwt.profile.JwtProfile;

import com.cgi.commons.utils.ApplicationUtils;

/**
 * Extensible class to store User specific information. If used, this class must be instantiated by SecurityManager in
 * the getUser() method.
 */
public class DefaultUser implements Serializable {

	/** Serial Id. */
	private static final long serialVersionUID = 5435236179352893354L;

	/** Access rights on lists. **/
	protected Set<String> lists;

	/** Access rights on Actions. **/
	protected Map<String, Set<String>> actions;

	/** Access rights on Menus. **/
	protected Set<String> menus;

	/** Access rights on Menu Options. **/
	protected Set<String> menusOptions;

	/** The locale of this user (Never null). */
	protected final Locale locale;

	/** Login. */
	protected String login;

	/** Map of User Datas. */
	protected HashMap<String, Object> userData = new HashMap<String, Object>();

	/**
	 * Create a new User for the given login.
	 * 
	 * @param login
	 *            The login of the new user.
	 * @param locale
	 *            user locale
	 */
	public DefaultUser(String login, Locale locale) {
		this.login = login;
		this.locale = locale != null ? locale : ApplicationUtils.getApplicationLogic().getDefaultLocale();
		this.lists = new HashSet<String>();
		this.actions = new HashMap<String, Set<String>>();
		this.menus = new HashSet<String>();
		this.menusOptions = new HashSet<String>();
	}

	/**
	 * Create a new User from a received UserProfile JWT token.
	 * 
	 * @param profile
	 *            jwt profile
	 * @param locale
	 *            user locale
	 */
	public DefaultUser(JwtProfile profile, Locale locale) {
		this(profile.getId(), locale);
	}

	/**
	 * Set Login.
	 * @param login Login.
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * Returns Login.
	 * @return Login.
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * Getter for the locale.
	 * 
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Returns the Map of User Datas.
	 * 
	 * @return the Map of User Datas.
	 */
	public HashMap<String, Object> getUserData() {
		return userData;
	}

	/**
	 * Set the Map of User Datas.
	 * @param userData the Map of User Datas.
	 */
	public void setUserData(HashMap<String, Object> userData) {
		this.userData = userData;
	}

	/**
	 * Returns the Access rights on Actions.
	 * @return the Access rights on Actions.
	 */
	public Map<String, Set<String>> getActions(){
		return actions;
	}

	/**
	 * Returns the Access rights on Lists.
	 * @return the Access rights on Lists.
	 */
	public Set<String> getLists(){
		return lists;
	}

	/**
	 * Returns the Access rights on Menu Options.
	 * @return the Access rights on Menu Options.
	 */
	public Set<String> getMenusOptions() {
		return menusOptions;
	}

	/**
	 * Returns the Access rights on Menus.
	 * @return the Access rights on Menus.
	 */
	public Set<String> getMenus() {
		return menus;
	}

	/**
	 * Returns access rights on menus, menu options, lists and actions.
	 * @return access rights on menus, menu options, lists and actions.
	 */
	public Collection<String> getPermissions() {
		Collection<String> permissions = new ArrayList<String>();
		for (Entry<String, Set<String>> action : actions.entrySet()) {
			for(String actionValue : action.getValue()) {
				permissions.add("action-" + action.getKey() + "-" + actionValue);
			}
		}
		for(String list : lists) {
			permissions.add("list-" + list);
		}
		for(String menusOption : menusOptions) {
			permissions.add("menu-option-" + menusOption);
		}
		for(String menu : menus) {
			permissions.add("menu-" + menu);
		}
		return permissions;
	}

	/**
	 * Convert this user to a UserProfile for the JWT token.
	 * 
	 * @return the profile
	 */
	public JwtProfile toJwtProfile() {
		JwtProfile profile = new JwtProfile();
		profile.setId(this.login);
		profile.addAttribute(JwtProfile.USERNAME, this.login);

		return profile;
	}
}
