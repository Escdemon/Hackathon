/**
 * 
 */
package com.cgi.commons.ref.entity;

import java.io.Serializable;

/**
 * Link.
 */
public class Link implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 2191902039469622194L;

	/** Model of the link. */
	private final LinkModel model;

	/** Entity. */
	private Entity entity;

	/** Key. */
	private Key key;

	/** Indicates if the link is prepared. */
	private boolean prepared;
	/** Indicates if an action is applied on link. */
	private boolean applyActionOnLink;

	/**
	 * Constructor.
	 * 
	 * @param linkModel Model of the link.
	 */
	public Link(LinkModel linkModel) {
		model = linkModel;
	}

	/**
	 * Returns the Entity.
	 * @return The Entity.
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * Setter for the Entity.
	 * @param linkedEntity The Entity.
	 */
	public void setEntity(Entity linkedEntity) {
		this.entity = linkedEntity;
		if (linkedEntity != null) {
			key = entity.getPrimaryKey();
		} else {
			key = null;
		}
	}

	/**
	 * Generate the encoded value of the key.
	 * @return The encoded value.
	 */
	public String getEncodedValue() {
		if (key != null) {
			return key.getEncodedValue();
		}
		return null;
	}

	/**
	 * Set the encoded value into the key.
	 * 
	 * @param encodedValue The encoded value.
	 */
	public void setEncodedValue(String encodedValue) {
		if (encodedValue == null) {
			key = null;
			return;
		}
		if (key == null) {
			key = new Key(model.getRefEntityName());
		}
		key.setEncodedValue(encodedValue);
	}

	/**
	 * Returns the Key.
	 * @return The Key.
	 */
	public Key getKey() {
		return key;
	}

	/**
	 * Returns the Model.
	 * @return The Model.
	 */
	public LinkModel getModel() {
		return model;
	}

	/**
	 * Indicates if the link is prepared.
	 * @return <code>true</code> if the link is prepared.
	 */
	public boolean isPrepared() {
		return prepared;
	}

	/**
	 * Set if the link is prepared.
	 * @param prepared <code>true</code> if the link is prepared.
	 */
	public void setPrepared(boolean prepared) {
		this.prepared = prepared;
	}

	/**
	 * Indicates if an action is applied to the link.
	 * @return <code>true</code> if an action is applied to the link.
	 */
	public boolean isApplyActionOnLink() {
		return applyActionOnLink;
	}

	/**
	 * Set if an action is applied to the link.
	 * @param applyActionOnLink <code>true</code> if an action is applied to the link.
	 */
	public void setApplyActionOnLink(boolean applyActionOnLink) {
		this.applyActionOnLink = applyActionOnLink;
	}

}
