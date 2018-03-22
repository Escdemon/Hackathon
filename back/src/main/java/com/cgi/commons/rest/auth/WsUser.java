package com.cgi.commons.rest.auth;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.cgi.business.application.User;
import com.cgi.business.application.UserSerializer;
import com.cgi.commons.security.SecurityFunction;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Class User for Web Services.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class WsUser implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 1L;

	/** User. */
	@JsonSerialize(using = UserSerializer.class)
	private User user;
	/** Token for authentication. */
	private String token;
	/** Date of token validity. */
	private Date tokenDate;
	/** Security functions authorized for the user. */
	private List<SecurityFunction> functions;

	/**
	 * Constructor.
	 */
	public WsUser() {
	}

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            User.
	 * @param token
	 *            Token for authentication.
	 * @param tokenDate
	 *            Date of token validity.
	 * @param list
	 *            Security functions authorized for the user.
	 */
	public WsUser(User user, String token, Date tokenDate, List<SecurityFunction> list) {
		this.user = user;
		this.token = token;
		this.tokenDate = tokenDate;
		this.functions = list;
	}

	/**
	 * Returns the User.
	 * 
	 * @return the User.
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Set the User.
	 * 
	 * @param user
	 *            the User.
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Returns the Token for authentication.
	 * 
	 * @return the Token for authentication.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Set the Token for authentication.
	 * 
	 * @param token
	 *            the Token for authentication.
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Returns the Date of token validity.
	 * 
	 * @return the Date of token validity.
	 */
	public Date getTokenDate() {
		return tokenDate;
	}

	/**
	 * Set the Date of token validity.
	 * 
	 * @param tokenDate
	 *            the Date of token validity.
	 */
	public void setTokenDate(Date tokenDate) {
		this.tokenDate = tokenDate;
	}

	/**
	 * Returns the Security functions authorized for the user.
	 * 
	 * @return the Security functions authorized for the user.
	 */
	public List<SecurityFunction> getFunctions() {
		return functions;
	}

	/**
	 * Set the Security functions authorized for the user.
	 * 
	 * @param functions
	 *            the Security functions authorized for the user.
	 */
	public void setFunctions(List<SecurityFunction> functions) {
		this.functions = functions;
	}

}
