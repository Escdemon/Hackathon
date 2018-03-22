package com.cgi.commons.logic;

import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.SHORT;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import com.cgi.business.application.User;
import com.cgi.commons.db.ConnectionLogger;
import com.cgi.commons.db.ConnectionObject;
import com.cgi.commons.db.DbQuery;
import com.cgi.commons.db.SqlBuilder;
import com.cgi.commons.ref.context.ApplicationContext;
import com.cgi.commons.ref.context.RequestContext;

/**
 * Default implementation for the application logic methods.
 */
public class DefaultApplicationLogic {

	/**
	 * Return formatter used to display date objects.
	 *
	 * @param context context to retrieve locale used to format date.
	 * @return The formatter used to display date objects.
	 */
	public DateFormat getDateFormat(RequestContext context) {
		return SimpleDateFormat.getDateInstance(SHORT, getLocale(context));
	}

	/**
	 * Return the format used to display date objects with time.
	 *
	 * @param context context to retrieve locale used to format date.
	 * @return The format used to display date objects with time.
	 */
	public DateFormat getDatetimeFormat(RequestContext context) {
		return SimpleDateFormat.getDateTimeInstance(SHORT, SHORT, getLocale(context));
	}

	/**
	 * Return the format used to display time objects.
	 *
	 * @param context context to retrieve locale used to format date.
	 * @return The format used to display time objects.
	 */
	public DateFormat getTimeFormat(RequestContext context) {
		return SimpleDateFormat.getTimeInstance(SHORT, getLocale(context));
	}

	/**
	 * Return the format used to display timestamp objects.
	 *
	 * @param context context to retrieve locale used to format date.
	 * @return The format used to display timestamp objects.
	 */
	public DateFormat getTimestampFormat(RequestContext context) {
		return SimpleDateFormat.getDateTimeInstance(SHORT, MEDIUM, getLocale(context));
	}

	/**
	 * Get the Locale of the current user or the default one
	 * 
	 * @param context
	 *            Current request context
	 * @return Locale (never null)
	 */
	protected Locale getLocale(RequestContext context) {
		final User user = context.getUser();
		final Locale locale = null == user ? getDefaultLocale() : user.getLocale();
		return null == locale ? getDefaultLocale() : locale;
	}

	/**
	 * @return language to be used if client requested language is not supported
	 */
	public Locale getDefaultLocale() {
		return Locale.forLanguageTag("fr-FR");
	}

	/**
	 * Set Export format to xlsx (need enableXlsExport() to true)
	 * 
	 * @return true if enabled
	 */
	public boolean useXslxFormat() {
		return true;
	}

	/**
	 * Initializes application context. This method is called on application startup.
	 * 
	 * @param context
	 *            The application context. This context persists as long as the application runs on application server.
	 */
	public void initializeApplication(ApplicationContext context) {

	}

	/**
	 * Finalizes application context. This method is called on application shutdown.
	 * 
	 * @param context
	 *            The application context to finalize. This method should close all resources stored in application context and ensure that no
	 *            system lock remains.
	 */
	public void finalizeApplication(ApplicationContext context) {
		List<ConnectionObject> connections = ConnectionLogger.getInstance().getListConnections();
		// Close connections
		for (ConnectionObject c : connections) {
			c.close();
		}
	}

	/**
	 * Get SqlBuilder instance initialized with given query.
	 * 
	 * @param query associated DbQuery
	 * @return SqlBuilder instance
	 */
	public SqlBuilder getSqlBuilder(DbQuery query) {
		return new SqlBuilder(query);
	}
}
