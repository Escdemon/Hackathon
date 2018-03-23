package com.cgi.models.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.cgi.commons.db.AbstractEntityQuery;
import com.cgi.commons.db.DbQuery;
import com.cgi.models.constants.PhotoConstants;

/**
 * Query class for the entity Photo.
 */
public class PhotoQuery extends AbstractEntityQuery {
	/** Queries for entity Photo. */
	public static final Map<String, DbQuery> QUERIES;

	/** Initialize internal query repository */
	static { 
		QUERIES = new HashMap<String, DbQuery>();
				
		DbQuery photo = new DbQuery(PhotoConstants.ENTITY_NAME, PhotoConstants.Alias.PHOTO.T1);
		photo.setName(PhotoConstants.Query.PHOTO);
		photo.addColumn(PhotoConstants.Vars.ID, PhotoConstants.Alias.PHOTO.T1);
		photo.addColumn(PhotoConstants.Vars.IMAGE, PhotoConstants.Alias.PHOTO.T1);
		QUERIES.put(PhotoConstants.Query.PHOTO, photo);
	

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
	 * @deprecated rather use PhotoConstants.Query
	 */
	@Deprecated
	public interface Query {
		/** Query PHOTO. */
		String QUERY_PHOTO = PhotoConstants.Query.PHOTO;
	}
	
	/**
	 * Holder for the query entities aliases.
	 * @deprecated rather use PhotoConstants.Alias
	 */
	@Deprecated
	public interface Alias {
		/** Aliases for query PHOTO. */
		interface QUERY_PHOTO {
			/** Alias T1. */
			String PHOTO_T1 = PhotoConstants.Alias.PHOTO.T1;
		}
	}
	
	@Override
	public Set<String> getQueryNames() {
		return QUERIES.keySet();
	}

	/**
	 * Returns a clone of DbQuery named PHOTO.
	 * 
	 * @return clone of DbQuery named PHOTO.
	 */
	public static DbQuery getPhotoQuery() {
		return QUERIES.get(PhotoConstants.Query.PHOTO).clone();
	}


}

