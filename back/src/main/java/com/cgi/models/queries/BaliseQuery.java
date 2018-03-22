package com.cgi.models.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.cgi.commons.db.AbstractEntityQuery;
import com.cgi.commons.db.DbQuery;
import com.cgi.models.constants.BaliseConstants;

/**
 * Query class for the entity Balise.
 */
public class BaliseQuery extends AbstractEntityQuery {
	/** Queries for entity Balise. */
	public static final Map<String, DbQuery> QUERIES;

	/** Initialize internal query repository */
	static { 
		QUERIES = new HashMap<String, DbQuery>();
				
		DbQuery balise = new DbQuery(BaliseConstants.ENTITY_NAME, BaliseConstants.Alias.BALISE.T1);
		balise.setName(BaliseConstants.Query.BALISE);
		balise.addColumn(BaliseConstants.Vars.ID, BaliseConstants.Alias.BALISE.T1);
		balise.addColumn(BaliseConstants.Vars.NOM, BaliseConstants.Alias.BALISE.T1);
		balise.addColumn(BaliseConstants.Vars.INTERNAL_CAPTION, BaliseConstants.Alias.BALISE.T1);
		QUERIES.put(BaliseConstants.Query.BALISE, balise);
	

	}
	
	/**
	 * Gets a query from internal query cache.
	 * @param queryName query name
	 * @return Cached query
	 */
	@Override
	public DbQuery getQuery(String queryName) {
		return QUERIES.get(queryName);
	}
	
	/**
	 * Holder for the query names.
	 * @deprecated rather use BaliseConstants.Query
	 */
	@Deprecated
	public interface Query {
		/** Query BALISE. */
		String QUERY_BALISE = BaliseConstants.Query.BALISE;
	}
	
	/**
	 * Holder for the query entities aliases.
	 * @deprecated rather use BaliseConstants.Alias
	 */
	@Deprecated
	public interface Alias {
		/** Aliases for query BALISE. */
		interface QUERY_BALISE {
			/** Alias T1. */
			String BALISE_T1 = BaliseConstants.Alias.BALISE.T1;
		}
	}
	
	@Override
	public Set<String> getQueryNames() {
		return QUERIES.keySet();
	}

	/**
	 * Returns a clone of DbQuery named BALISE.
	 * 
	 * @return clone of DbQuery named BALISE.
	 */
	public static DbQuery getBaliseQuery() {
		return QUERIES.get(BaliseConstants.Query.BALISE).clone();
	}


}

