package com.cgi.commons.rest.domain;

import javax.xml.bind.annotation.XmlElement;

/**
 * Element Key Value.
 */
public class MapElements {
	/** Key. */
	@XmlElement
	public String key;
	/** Value. */
	@XmlElement
	public Object value;

	/** 
	 * Constructor.
	 */
	public MapElements() {
	} // Required by JAXB

	/**
	 * Constructor.
	 * @param key Key.
	 * @param value Value.
	 */
	public MapElements(String key, Object value) {
		this.key = key;
		this.value = value;
	}

}
