package com.cgi.commons.rest.domain;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.cgi.commons.ref.data.Row;

/**
 * List of rows.
 */
@XmlRootElement
public class ResultList {

	/** Number of rows. */
	private int resultSetCount;
	/** List of rows. */
	private List<Row> results;

	/**
	 * Returns the Number of rows.
	 * @return the Number of rows.
	 */
	public int getResultSetCount() {
		return resultSetCount;
	}

	/**
	 * Set the Number of rows.
	 * @param resultSetCount the Number of rows.
	 */
	public void setResultSetCount(int resultSetCount) {
		this.resultSetCount = resultSetCount;
	}

	/**
	 * Returns the List of results.
	 * @return the List of results.
	 */
	@XmlJavaTypeAdapter(value = com.cgi.commons.rest.adapters.RowAdapter.class, type = Row.class)
	public List<Row> getResults() {
		return results;
	}

	/**
	 * Set the List of results.
	 * @param results the List of results.
	 */
	public void setResults(List<Row> results) {
		this.results = results;
	}
}
