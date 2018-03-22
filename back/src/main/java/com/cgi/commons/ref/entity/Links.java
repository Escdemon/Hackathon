package com.cgi.commons.ref.entity;

import java.util.HashMap;

/**
 * Hashmap of links.
 */
public class Links extends HashMap<String, Link> {

	/** Serial id. */
	private static final long serialVersionUID = 7400010665191645750L;

	/** Entity Model. */
	private final EntityModel entityModel;

	/** Indicates if links are backrefs. */
	private final boolean backRef;

	/**
	 * Constructor.
	 * 
	 * @param mdl Entity Model.
	 * @param backRef Indicates if links are backrefs.
	 */
	public Links(EntityModel mdl, boolean backRef) {
		this.entityModel = mdl;
		this.backRef = backRef;
	}

	@Override
	public Link get(Object key) {
		Link link = super.get(key);
		if (link == null) {
			if (!backRef && entityModel.getLinkNames().contains(key)) {
				link = new Link(entityModel.getLinkModel((String) key));
				put((String) key, link);
			} else if (backRef && entityModel.getBackRefNames().contains(key)) {
				link = new Link(entityModel.getBackRefModel((String) key));
				put((String) key, link);
			}
		}
		return link;
	}
}
