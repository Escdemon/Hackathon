package com.cgi.commons.ref.entity;

import java.util.List;

import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.KeyModel;

/**
 * Model of link.<br>
 * Extends KeyModel to store data about the link's source key.
 */
public class LinkModel extends KeyModel {

	/** Serial id. */
	private static final long serialVersionUID = 475286302295352786L;

	/** link name */
	private final String name;
	/** source entity name */
	private final String entityName;
	/** target entity name */
	private final String refEntityName;

	/**
	 * Constructor.
	 * 
	 * @param linkName Name of the link.
	 * @param srcEntity Name of the source entity.
	 * @param refEntity Name of the target entity.
	 * @param fkFields List of key fields
	 * @param fkUnique link is based on a unique foreign key
	 */
	public LinkModel(String linkName, String srcEntity, String refEntity, List<String> fkFields, boolean fkUnique) {
		super(srcEntity, fkFields, fkUnique);
		this.name = linkName;
		this.entityName = srcEntity;
		this.refEntityName = refEntity;
	}

	/**
	 * Indicates if the link is mandatory.
	 * @return <code>true</code> if at least one field on the link is mandatory.
	 */
	public boolean isMandatory() {
		EntityModel mdl = EntityManager.getEntityModel(entityName);
		for (String field : getFields()) {
			if (mdl.getField(field).isMandatory()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Indicates if the link is transient.
	 * 
	 * @return <code>true</code> if at least one field on the link is transient. This means there's no physical constraint in the database
	 */
	public boolean isTransient() {
		EntityModel mdl = EntityManager.getEntityModel(entityName);
		for (String field : getFields()) {
			if (mdl.getField(field).isTransient()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the link name
	 */
	public String getLinkName() {
		return name;
	}

	/**
	 * @return the source entity
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * @return the referenced entity
	 */
	public String getRefEntityName() {
		return refEntityName;
	}
}
