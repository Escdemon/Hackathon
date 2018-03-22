package com.cgi.commons.ref.data;

import java.io.Serializable;
import java.util.Map;

import com.cgi.commons.ref.entity.Entity;

/**
 * A back ref Data.
 *
 */
public class BackRefData implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -8032354830494378725L;

	/** The entity name. */
	private String entityName;

	/** The source entity. */
	private Entity sourceEntity;

	/** The description of the data. */
	private String description;

	/** Action rights. */
	private Map<String, Boolean> actionRights;

	/**
	 * Constructor. 
	 * 
	 * @param entityName The entity name.
	 * @param sourceEntity The source entity.
	 * @param description The description of the data.
	 */
	public BackRefData(String entityName, Entity sourceEntity, String description) {
		this.entityName = entityName;
		this.sourceEntity = sourceEntity;
		this.description = description;
	}

	/**
	 * Getter for the entityName.
	 * @return the entityName.
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * Setter for the entityName.
	 * @param entityName the entityName.
	 */
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	/**
	 * Getter for the sourceEntity.
	 * @return the sourceEntity.
	 */
	public Entity getSourceEntity() {
		return sourceEntity;
	}

	/**
	 * Setter for the sourceEntity.
	 * @param sourceEntity the sourceEntity.
	 */
	public void setSourceEntity(Entity sourceEntity) {
		this.sourceEntity = sourceEntity;
	}

	/**
	 * Getter for the description.
	 * @return the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter for the description.
	 * @param description the description.
	 */
	public void setDescription(String description) {
		this.description = description;
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
