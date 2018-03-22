package com.cgi.commons.ref.data;

import java.io.Serializable;
import java.util.Map;

import com.cgi.commons.ref.entity.Entity;

/**
 * A link data.
 */
public class LinkData implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = -8032354830494378725L;

	/** The entity name. */
	private String entityName;
	/** The target entity. */
	private Entity targetEntity;
	/** The target description. */
	private String targetDescription;
	/** Action rights. */
	private Map<String, Boolean> actionRights;

	/**
	 * Constructor. 
	 * 
	 * @param entityName The entity name.
	 * @param targetEntity The target entity.
	 * @param targetDescription The target description.
	 */
	public LinkData(String entityName, Entity targetEntity, String targetDescription) {
		this.entityName = entityName;
		this.targetEntity = targetEntity;
		this.targetDescription = targetDescription;
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
	 * Getter for the targetEntity.
	 * @return the targetEntity.
	 */
	public Entity getTargetEntity() {
		return targetEntity;
	}

	/**
	 * Setter for the targetEntity.
	 * @param targetEntity the targetEntity.
	 */
	public void setTargetEntity(Entity targetEntity) {
		this.targetEntity = targetEntity;
	}

	/**
	 * Getter for the targetDescription.
	 * @return the targetDescription.
	 */
	public String getTargetDescription() {
		return targetDescription;
	}

	/**
	 * Setter for the targetDescription.
	 * @param targetDescription the targetDescription.
	 */
	public void setTargetDescription(String targetDescription) {
		this.targetDescription = targetDescription;
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
