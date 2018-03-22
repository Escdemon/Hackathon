package com.cgi.commons.db;

import java.util.Set;

/**
 * Abstract class for an the entities query classes.
 */
public abstract class AbstractEntityQuery {

	/**
	 * Retrieve the query by his name.
	 * 
	 * @param queryName The name of the query.
	 * @return The query finded.
	 */
	public abstract DbQuery getQuery(String queryName);

	/**
	 * Return the list of all queries of this entity.
	 * 
	 * @return The list of all queries of this entity.
	 */
	public abstract Set<String> getQueryNames();
}
