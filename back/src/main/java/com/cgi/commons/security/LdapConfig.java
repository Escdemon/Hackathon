package com.cgi.commons.security;

/**
 * Configuration for Ldap.
 */
public class LdapConfig {
	/** Url. */
	public String url = null;
	/** Type of authentication. */
	public String authentication = "simple";
	/** Principal. */
	public String principal = null;
	/** Credential. */
	public String credentials = null;
	/** Dn Base. */
	public String userBaseDn = "";
	/** Subsearch. */
	public String userSubsearch = "true";
	/** Attribute for login. */
	public String userLoginAttr = "cn";
	/** Attribute for Dn. */
	public String userDnAttr = "dn";
	/** Other Attribute. */
	public String userOtherAttr = "";
}
