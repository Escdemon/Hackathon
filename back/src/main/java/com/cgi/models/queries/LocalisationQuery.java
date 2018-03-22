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
		
		// Initialization of query LOCALISATIONS
		DbQuery localisations = new DbQuery(LocalisationConstants.ENTITY_NAME, LocalisationConstants.Alias.LOCALISATIONS.T1);
		localisations.setName(LocalisationConstants.Query.LOCALISATIONS);
		localisations.addColumn(LocalisationConstants.Vars.ID, LocalisationConstants.Alias.LOCALISATIONS.T1);
		localisations.addColumn(LocalisationConstants.Vars.COORD_X, LocalisationConstants.Alias.LOCALISATIONS.T1);
		localisations.addColumn(LocalisationConstants.Vars.COORD_Y, LocalisationConstants.Alias.LOCALISATIONS.T1);
		localisations.addColumn(LocalisationConstants.Vars.STATUT, LocalisationConstants.Alias.LOCALISATIONS.T1);
		localisations.addColumn(LocalisationConstants.Vars.HEURE, LocalisationConstants.Alias.LOCALISATIONS.T1);
		localisations.addCondEq(LocalisationConstants.Vars.BALISE_ID, LocalisationConstants.Alias.LOCALISATIONS.T1, 1L);
		localisations.addSortBy(LocalisationConstants.Vars.HEURE, LocalisationConstants.Alias.LOCALISATIONS.T1, DbQuery.ASC);
		QUERIES.put(LocalisationConstants.Query.LOCALISATIONS, localisations);
				
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
		/** Query LOCALISATIONS. */
		String QUERY_LOCALISATIONS = LocalisationConstants.Query.LOCALISATIONS;
		/** Query LOCALISATION. */
		String QUERY_LOCALISATION = LocalisationConstants.Query.LOCALISATION;
	}
	
	/**
	 * Holder for the query entities aliases.
	 * @deprecated rather use LocalisationConstants.Alias
	 */
	@Deprecated
	public interface Alias {
		/** Aliases for query LOCALISATIONS. */
		interface QUERY_LOCALISATIONS {
			/** Alias T1. */
			String LOCALISATION_T1 = LocalisationConstants.Alias.LOCALISATIONS.T1;
		}
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
	 * Returns a clone of DbQuery named LOCALISATIONS.
	 * 
	 * @return clone of DbQuery named LOCALISATIONS.
	 */
	public static DbQuery getLocalisationsQuery() {
		return QUERIES.get(LocalisationConstants.Query.LOCALISATIONS).clone();
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

