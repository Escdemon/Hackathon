package com.cgi.models.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.cgi.commons.db.AbstractEntityQuery;
import com.cgi.commons.db.DbQuery;
import com.cgi.models.constants.LocalisationConstants;

/**
 * Query class for the entity Localisation.
 */
public class LocalisationQuery extends AbstractEntityQuery {
	/** Queries for entity Localisation. */
	public static final Map<String, DbQuery> QUERIES;

	/** Initialize internal query repository */
	static { 
		QUERIES = new HashMap<String, DbQuery>();
				
		DbQuery localisation = new DbQuery(LocalisationConstants.ENTITY_NAME, LocalisationConstants.Alias.LOCALISATION.T1);
		localisation.setName(LocalisationConstants.Query.LOCALISATION);
		localisation.addColumn(LocalisationConstants.Vars.ID, LocalisationConstants.Alias.LOCALISATION.T1);
		localisation.addColumn(LocalisationConstants.Vars.COORD_X, LocalisationConstants.Alias.LOCALISATION.T1);
		localisation.addColumn(LocalisationConstants.Vars.COORD_Y, LocalisationConstants.Alias.LOCALISATION.T1);
		localisation.addColumn(LocalisationConstants.Vars.HEURE, LocalisationConstants.Alias.LOCALISATION.T1);
		localisation.addColumn(LocalisationConstants.Vars.STATUT, LocalisationConstants.Alias.LOCALISATION.T1);
		localisation.addColumn(LocalisationConstants.Vars.BALISE_ID, LocalisationConstants.Alias.LOCALISATION.T1);
		QUERIES.put(LocalisationConstants.Query.LOCALISATION, localisation);
	

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
	 * @deprecated rather use LocalisationConstants.Query
	 */
	@Deprecated
	public interface Query {
		/** Query LOCALISATION. */
		String QUERY_LOCALISATION = LocalisationConstants.Query.LOCALISATION;
	}
	
	/**
	 * Holder for the query entities aliases.
	 * @deprecated rather use LocalisationConstants.Alias
	 */
	@Deprecated
	public interface Alias {
		/** Aliases for query LOCALISATION. */
		interface QUERY_LOCALISATION {
			/** Alias T1. */
			String LOCALISATION_T1 = LocalisationConstants.Alias.LOCALISATION.T1;
		}
	}
	
	@Override
	public Set<String> getQueryNames() {
		return QUERIES.keySet();
	}

	/**
	 * Returns a clone of DbQuery named LOCALISATION.
	 * 
	 * @return clone of DbQuery named LOCALISATION.
	 */
	public static DbQuery getLocalisationQuery() {
		return QUERIES.get(LocalisationConstants.Query.LOCALISATION).clone();
	}


}

