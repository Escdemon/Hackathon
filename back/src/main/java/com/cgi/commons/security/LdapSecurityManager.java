package com.cgi.commons.security;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import com.cgi.business.application.User;
import com.cgi.commons.ref.context.RequestContext;

/**
 * SecurityManager using manual LDAP authentication.
 */
public class LdapSecurityManager extends DefaultSecurityManager {
	/** Serial Id. */
	private static final long serialVersionUID = 1L;
	/** Log4j logger. */
	protected static final Logger LOGGER = Logger.getLogger(LdapSecurityManager.class);

	/** LDAP url (ldap://server:port/basedn). */
	private String ldapUrl = null;
	/** LDAP bind Username (if anonymous bind is not allowed, use null otherwise). */
	private String ldapPrincipal = null;
	/** LDAP bind Password (not used if username is null). */
	private String ldapCredentials = null;
	/** LDAP users base DN (used as root for user seach). */
	private String ldapUserBaseDn = "";
	/** Search LDAP users in subtrees. */
	private boolean ldapUserSubsearch = true;
	/** LDAP user attribute matching the login. */
	private String ldapUserLoginAttribute = "cn";
	/** LDAP user attribute matching the ldap ID (change only of not working). */
	private String ldapUserDnAttribute = "distinguishedName";
	/** LDAP user attributes to get from ldap entry. */
	private List<String> ldapUserOtherAttributes = new ArrayList<String>();

	/** Context Root. */
	public static final String CONTEXT_ROOT = "java:comp/env";

	/**
	 * Default constructor.
	 */
	public LdapSecurityManager() {
		try {
			// Load config
			Context context = (Context) new InitialContext().lookup(CONTEXT_ROOT);
			LdapConfig config = (LdapConfig) context.lookup("ldap/config");

			ldapUrl = config.url;
			ldapPrincipal = config.principal;
			ldapCredentials = config.credentials;
			ldapUserBaseDn = config.userBaseDn;
			ldapUserSubsearch = Boolean.parseBoolean(config.userSubsearch);
			ldapUserLoginAttribute = config.userLoginAttr;
			ldapUserDnAttribute = config.userDnAttr;
			if (config.userOtherAttr != null && !"".equals(config.userOtherAttr)) {
				ldapUserOtherAttributes = Arrays.asList(config.userOtherAttr.split(","));
			}
			
			try {
				// Silent close
				context.close();
			} catch (Exception e) {}
		} catch (Exception e) {
			// Error loading config, fail ?
			LOGGER.fatal("Error loading LDAP configuration", e);
		}
	}

	@Override
	public User getUser(String pLogin, String password, RequestContext ctx) {
		// Escape login invalid characters
		LOGGER.debug("Login attempt with login=" + pLogin);
		String login = sanitize(pLogin);
		LOGGER.debug("Login sanitized into login=" + login);

		try {
			// Search for user
			Map<String, String> userData = getUserInfosFromLdap(login, ldapUserOtherAttributes);
			String bindDn = userData.get(ldapUserDnAttribute);

			if (bindDn != null) {
				// login found, try password

				// Clean DN from userData
				userData.remove(ldapUserDnAttribute);

				try {
					// Get new env with userDN and userPassword
					Hashtable<String, String> env = getEnv();
					env.put(javax.naming.Context.SECURITY_PRINCIPAL, bindDn);
					env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);

					// Try to log-in
					DirContext dctx = new InitialDirContext(env);
					dctx.close();
					// Login success
					LOGGER.info("User log-in successfull for '" + login + "'");

					// Return an ApplicationUser object
					User user = new User(login);
					// With other attributes as custom data
					user.getUserData().putAll(userData);
					return user;
				} catch (AuthenticationException ae) {
					// Login failed
					LOGGER.warn("User log-in rejected for '" + login + "'", ae);

					// Return null to reject login
					return null;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error during LDAP query", e);
		}

		// Something go wrong, return null to reject login
		return null;
	}

	/**
	 * Returns environnement variables.
	 * @return environnement variables.
	 */
	private Hashtable<String, String> getEnv() {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(javax.naming.Context.PROVIDER_URL, ldapUrl);
		env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
		if (ldapPrincipal != null) {
			env.put(javax.naming.Context.SECURITY_PRINCIPAL, ldapPrincipal);
			env.put(javax.naming.Context.SECURITY_CREDENTIALS, ldapCredentials);
		}

		return env;
	}

	/**
	 * Returns user informations from Ldap.
	 * 
	 * @param cn User login.
	 * @param ldapAttributes Attributes for Ldap. 
	 * @return user informations.
	 */
	public Map<String, String> getUserInfosFromLdap(String cn, List<String> ldapAttributes) {

		Map<String, String> userData = new HashMap<String, String>();
		try {
			// Get LDAP context
			DirContext dctx = new InitialDirContext(getEnv());

			// Prepare query to search the user
			SearchControls sc = new SearchControls();
			sc.setTimeLimit(20000);
			sc.setCountLimit(1);
			String[] attributeFilter = new String[ldapAttributes.size() + 1];
			attributeFilter[0] = ldapUserDnAttribute;
			for (int i = 0; i < ldapAttributes.size(); i++) {
				attributeFilter[i + 1] = ldapAttributes.get(i);
			}
			sc.setReturningAttributes(attributeFilter);
			if (ldapUserSubsearch) {
				sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
			} else {
				sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			}
			String filter = "(" + ldapUserLoginAttribute + "=" + cn + ")";

			NamingEnumeration<SearchResult> results = dctx.search(ldapUserBaseDn, filter, sc);
			if (results.hasMore()) {
				SearchResult sr = (SearchResult) results.next();
				Attributes attrs = sr.getAttributes();

				// Get user ID
				userData.put(ldapUserDnAttribute, (String) attrs.get(ldapUserDnAttribute).get());

				// Get the other attributes
				for (int i = 0; i < ldapAttributes.size(); i++) {
					String attr = ldapAttributes.get(i);
					if (attrs.get(attr) != null) {
						String value = (String) attrs.get(attr).get();
						userData.put(attr, value);
					}
				}
			}

			// Close connection
			results.close();
			dctx.close();
		} catch (Exception e) {
			LOGGER.error("Error during LDAP query", e);
		}

		return userData;
	}

	/**
	 * Escape LDAP invalid characters.
	 * 
	 * @param input
	 *            String to escape
	 * @return escaped String
	 */
	private String sanitize(final String input) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == '*') {
				// escape asterisk
				s.append("\\2a");
			} else if (c == '(') {
				// escape left parenthesis
				s.append("\\28");
			} else if (c == ')') {
				// escape right parenthesis
				s.append("\\29");
			} else if (c == '\\') {
				// escape backslash
				s.append("\\5c");
			} else if (c == '\u0000') {
				// escape NULL char
				s.append("\\00");
			} else if (c <= 0x7f) {
				// regular 1-byte UTF-8 char
				s.append(String.valueOf(c));
			} else if (c >= 0x080) {
				// higher-order 2, 3 and 4-byte UTF-8 chars
				try {
					byte[] utf8bytes = String.valueOf(c).getBytes("UTF8");
					for (byte b : utf8bytes) {
						s.append(String.format("\\%02x", b));
					}
				} catch (UnsupportedEncodingException e) {
					// Ignore char
					LOGGER.debug(e.getMessage(), e);
				}
			}
		}
		return s.toString();
	}
}
