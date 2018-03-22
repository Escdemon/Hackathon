package com.cgi.commons.ref.data;

import java.io.Serializable;

import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.utils.reflect.DomainUtils;

/**
 * A criteria for list.
 */
public class ListCriteria <E extends Entity> implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -6641435660404108818L;
	
	/** Field alias (ex: tableAlias_fieldAlias) for order by. */
	public String orderByField;
	/** Direction "ASC" or "DESC" for order by. */ 
	public String orderByDirection;
	
	/** Number minimum of row wanted. */
	public int minRow = 0;
	/** Number maximum of row wanted. */
	public int maxRow = Constants.MAX_ROW;
	
	/** Search criteria (NULL if not globalSearch). */
	public String searchCriteria;
	/** Search Entity (NULL if globalSearch). */
	public E searchEntity;

	/** List Action */
	public Action action;
	
	/** Previous page link name (may be NULL) */
	public String linkName;
	/** Previous page entity (may be NULL) */
	public Entity linkedEntity;

	/**
	 * Default constructor
	 */
	public ListCriteria() {
		super();
	}

	public ListCriteria(E searchEntity, String linkName, Entity linkedEntity) {
		this();
		this.searchEntity = searchEntity;
		this.linkName = linkName;
		this.linkedEntity = linkedEntity;
	}

	public ListCriteria(E searchEntity, Action action, String linkName, Entity linkedEntity) {
		this(searchEntity, linkName, linkedEntity);
		this.action = action;
	}

	public ListCriteria(String searchCriteria) {
		this();
		this.searchCriteria = searchCriteria;
	}

	public ListCriteria(String searchCriteria, Action action) {
		this(searchCriteria);
		this.action = action;
	}

	/**
	 * Constructor for criteria copy
	 * 
	 * @param criteria to copy
	 */
	public ListCriteria(ListCriteria<E> criteria) {
		this();
		this.orderByField = criteria.orderByField;
		this.orderByDirection = criteria.orderByDirection;
		this.minRow = criteria.minRow;
		this.maxRow = criteria.maxRow;
		this.searchCriteria = criteria.searchCriteria;
		this.searchEntity = criteria.searchEntity;
	}

	public boolean isGlobalSearch() {
		return searchEntity == null;
	}

	/**
	 * Create a sort by. <br>
	 * Toggle "ASC" to "DESC" and opposite if the sort by is already put on the same variable.
	 * 
	 * @param field The field to sort.
	 */
	public void sortBy(String field) {
		if (orderByField != null && orderByField.equals(field)) {
			if ("ASC".equals(orderByDirection)) {
				orderByDirection = "DESC";
			} else if ("DESC".equals(orderByDirection)) {
				orderByField = null;
				orderByDirection = null;
			}
		} else {
			orderByField = field;
			orderByDirection = "ASC";
		}
	}

	public void clearGlobalSearch() {
		searchCriteria = null;
	}

	public void clearSearchEntity() {
		searchEntity.resetEntity();
	}
}
