package com.cgi.commons.ref.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Key Model.
 */
public class KeyModel implements Serializable {

	private static final long serialVersionUID = 1594216317013783414L;

	// key's entity
	private final String entityName;
	// key unicity
	private final boolean unique;
	// key's variables
	private final List<String> fields;

	/**
	 * Build a primary key from entity
	 * 
	 * @param entityName
	 */
	public KeyModel(String entityName) {
		this.entityName = entityName;
		KeyModel m = EntityManager.getEntityModel(entityName).getKeyModel();
		fields = m.getFields();
		unique = m.isUnique();
	}

	/**
	 * Build a key
	 * 
	 * @param fields key's variables
	 * @param unique key unicity
	 */
	protected KeyModel(String entityName, List<String> fields, boolean unique) {
		this.entityName = entityName;
		this.fields = fields;
		this.unique = unique;
	}

	/**
	 * @return the entityName
	 */
	public String getEntityName() {
		return entityName;
	}

	/**
	 * @return key unicity
	 */
	public boolean isUnique() {
		return unique;
	}

	/**
	 * @return key's variables
	 */
	public List<String> getFields() {
		return fields;
	}

	/**
	 * Compares fields of current keyModel with another keyModel.
	 * 
	 * @param otherKey
	 *            Key to compare.
	 * @return true if field names are the same in both keys, order does not matter.
	 */
	public boolean hasSameFields(KeyModel otherKey) {
		Set<String> otherKeyFields = new HashSet<String>(otherKey.fields);
		for (String f : fields) {
			if (!otherKeyFields.remove(f)) {
				return false;
			}
		}
		if (otherKeyFields.size() > 0) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		return result;
	}

	/**
	 * keys are equals if they have the same variables
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyModel other = (KeyModel) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "KeyModel [entityName=" + entityName + ", unique=" + unique + ", fields=" + fields + "]";
	}
}
