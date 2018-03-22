package com.cgi.commons.ref.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A list category data.
 */
public class ListCategoryData extends ListData implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -5861359143964068300L;

	/**
	 * Names of columns that are category breaks for categorized lists.
	 */
	private List<String> categoryBreak = new ArrayList<String>();

	/**
	 * Constructor. 
	 * 
	 * @param entityName The entity name.
	 */
	public ListCategoryData(String entityName) {
		super(entityName);
		categoryBreak = new ArrayList<String>();
	}

	/**
	 * Constructor that may be used to build ListCategoryData from a ListData, this may prevent problems in UI when trying to display a
	 * categorized list with no category in the query.
	 * 
	 * @param listData the datas.
	 */
	public ListCategoryData(ListData listData) {
		super(listData.getEntityName());
		categoryBreak = new ArrayList<String>();
		this.setColumns(listData.getColumns());
		this.setTotalRowCount(listData.getTotalRowCount());
		for (Row row : listData.getRows()) {
			this.add(row);
		}
		this.setProtected(listData.isProtected());
	}

	/**
	 * Getter for the categoryBreak.
	 * @return the categoryBreak.
	 */
	public List<String> getCategoryBreak() {
		return categoryBreak;
	}

	/**
	 * Setter for the categoryBreak.
	 * @param categoryBreak the categoryBreak.
	 */
	public void setCategoryBreak(List<String> categoryBreak) {
		this.categoryBreak = categoryBreak;
	}
}
