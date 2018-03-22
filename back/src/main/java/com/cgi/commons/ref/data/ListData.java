package com.cgi.commons.ref.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.KeyModel;

/**
 *  A data for list.
 */
public class ListData implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -5861359143964068300L;

	/** The entity name. */
	protected final String entityName;
	/** The key model. */
	protected final KeyModel keyModel;
	/** The list of rows. */
	protected List<Row> rows;
	/** The total row count. */
	protected int totalRowCount;
	/** The column datas. */
	protected Map<String, ColumnData> columns;
	/** Indicates if data is protected. */
	protected boolean isProtected = false;
	/** Indicates if data is read only. */
	protected boolean isReadOnly = false;
	/** Action rights. */
	protected Map<String, Boolean> actionRights;

	/**
	 * Constructor.
	 * 
	 * @param entityName The entity name.
	 */
	public ListData(String entityName) {
		rows = new ArrayList<Row>();
		columns = new HashMap<String, ColumnData>();
		this.entityName = entityName;
		this.keyModel = EntityManager.getEntityModel(entityName).getKeyModel();
	}

	/**
	 * Add a row.
	 * 
	 * @param row The row to add.
	 */
	public void add(Row row) {
		rows.add(row);
	}

	/**
	 * Return all the rows.
	 * @return The rows.
	 */
	public List<Row> getRows() {
		return rows;
	}

	/**
	 * Return the key fields.
	 * @return the keyFields
	 */
	public KeyModel getKeyModel() {
		return keyModel;
	}

	/**
	 * Return the result count.
	 * @return the resultCount
	 */
	public Integer getResultCount() {
		return rows.size();
	}
	
	/**
	 * Getter for the columns.
	 * @return the columns.
	 */
	public Map<String, ColumnData> getColumns() {
		return columns;
	}

	/**
	 * Setter for the columns.
	 * @param columns the columns.
	 */
	public void setColumns(Map<String, ColumnData> columns) {
		this.columns = columns;
	}

	/**
	 * Getter for the entityName.
	 * @return the entityName.
	 */
	public String getEntityName() {
		return entityName;
	}
	
	/**
	 * Getter for the totalRowCount.
	 * @return the totalRowCount.
	 */
	public int getTotalRowCount() {
		return totalRowCount;
	}

	/**
	 * Setter for the totalRowCount.
	 * @param totalRowCount the totalRowCount.
	 */
	public void setTotalRowCount(int totalRowCount) {
		this.totalRowCount = totalRowCount;
	}

	/**
	 * Getter for the Protected.
	 * @return the Protected.
	 */
	public boolean isProtected() {
		return isProtected;
	}

	/**
	 * Setter for the Protected.
	 * @param isProtected the Protected.
	 */
	public void setProtected(boolean isProtected) {
		this.isProtected = isProtected;
	}

	/**
	 * Getter for the readOnly.
	 * @return the readOnly.
	 */
	public boolean isReadOnly() {
		return isReadOnly;
	}

	/**
	 * Setter for the readOnly.
	 * @param readOnly the readOnly.
	 */
	public void setReadOnly(boolean readOnly){
		isReadOnly = readOnly;
	}

	/**
	 * Getter for the actionRights.
	 * @return the actionRights.
	 */
	public Map<String, Boolean> getActionRights() {
		return actionRights;
	}

	/**
	 * Setter for the actionRights.
	 * @param actionRights the actionRights.
	 */
	public void setActionRights(Map<String, Boolean> actionRights) {
		this.actionRights = actionRights;
	}

}
