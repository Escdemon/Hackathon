package com.cgi.commons.ref.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.cgi.commons.ref.annotations.EntityDef;
import com.cgi.commons.ref.entity.Action.Input;
import com.cgi.commons.ref.entity.Action.Persistence;
import com.cgi.commons.ref.entity.Action.Process;
import com.cgi.commons.ref.entity.Action.UserInterface;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TechnicalException;

/**
 * This class contains all metadata from an entity (fields, links, ...).<br/>
 * This class is instanciated on application start using entity annotations.
 * 
 * 
 */
public class EntityModel {

	/** Table name for this entity or REST class name for external entity */
	private final String dbTableName;
	
	/** Database schema name. */
	private final String dbSchemaName;

	/** Name for this entity */
	private final String entityName;

	/** Is an associative entity */
	private final boolean isAssociative;

	/** Is an external entity */
	private final boolean isExternal;

	/** Name of the sequence */
	private final String sequenceName;
	
	/** name of the versionning field if any */
	private String versioningField;
	
	/** PK definition */
	private final KeyModel primaryKeyModel;
	/** Constraints definitions */
	// TODO : private Map<String, ForeignKeyModel> constraints = new HashMap<String, ForeignKeyModel>();
	/** Links definitions */
	private Map<String, LinkModel> links = new LinkedHashMap<String, LinkModel>(4);
	/** Back-Links definitions */
	private Map<String, LinkModel> backRefs = new LinkedHashMap<String, LinkModel>(4);
	/** Entity fields definitions */
	private Map<String, EntityField> fields = new LinkedHashMap<String, EntityField>(12);
	/** Entity autoincrement-fields definitions */
	private Set<String> autoIncrementFields = new LinkedHashSet<String>(1);
	/** Entity lookup fields definitions */
	private Set<String> lookupFields = new LinkedHashSet<String>(2);
	/** Entity actions definitions */
	private Map<String, Action> actions = new LinkedHashMap<String, Action>(6);

	EntityModel(String entityName, EntityDef entityDef) {
		this.entityName = entityName;
		this.dbTableName = entityDef.dbName();
		this.dbSchemaName = entityDef.schemaId();
		this.sequenceName = entityDef.sequenceName();
		this.isAssociative = entityDef.isAssociative();
		this.isExternal = entityDef.isExternal();
		this.versioningField = entityDef.versionField();

		this.primaryKeyModel = new KeyModel(entityName, Arrays.asList(entityDef.primaryKey()), true);
	}

	/**
	 * Retreive the name of the entity.
	 * 
	 * @return the name of the entity.
	 */
	public String name() {
		return entityName;
	}

	/**
	 * Retreive the name of the entity DB table.
	 * 
	 * @return the name of the entity DB table.
	 */
	public String dbName() {
		return dbTableName;
	}

	/**
	 * Retrieves the database schema where the table is located.
	 * 
	 * @return A schema name or an empty string.
	 */
	public String getDbSchemaName() {
		return dbSchemaName;
	}

	/**
	 * Retrieves the entity primary key meta model.
	 * 
	 * @return primary key model
	 */
	public KeyModel getKeyModel() {
		return primaryKeyModel;
	}

	/**
	 * Retrieves the entity model.
	 * 
	 * @return the entity model.
	 */
	protected EntityModel getEntityModel() {
		return EntityManager.getEntityModel(name());
	}

	/**
	 * Get a Link by it's name.
	 * 
	 * @param linkName
	 *            : the name of the link
	 * @return the LinkModel
	 */
	public LinkModel getLinkModel(String linkName) {
		return links.get(linkName);
	}

	/**
	 * Get a Back-Link by it's name.
	 * 
	 * @param linkName
	 *            : the name of the back-link
	 * @return the LinkModel
	 */
	public LinkModel getBackRefModel(String linkName) {
		return backRefs.get(linkName);
	}

	/**
	 * Get all the links (direct and backref) names.
	 * 
	 * @return a list of link names
	 */
	public List<String> getAllLinkNames() {
		List<String> linkNames = new ArrayList<String>();
		linkNames.addAll(links.keySet());
		linkNames.addAll(backRefs.keySet());
		return linkNames;
	}

	/**
	 * Get all the links (direct only) names.
	 * 
	 * @return a list of link names
	 */
	public List<String> getLinkNames() {
		return new ArrayList<String>(links.keySet());
	}

	/**
	 * Get all the links (backref only) names.
	 * 
	 * @return a list of link names
	 */
	public List<String> getBackRefNames() {
		return new ArrayList<String>(backRefs.keySet());
	}

	/**
	 * Get the field definition for the given name.
	 * 
	 * @param fieldname
	 *            the name of the field
	 * @return an EntityField
	 */
	public EntityField getField(String fieldname) {
		return fields.get(fieldname);
	}

	/**
	 * Get all the fields names.
	 * 
	 * @return a set of field names
	 */
	public Set<String> getFields() {
		return fields.keySet();
	}

	/**
	 * Check if a field is of type AutoIncrement.
	 * 
	 * @param name
	 *            : the name of the field
	 * @return true if AutoIncrement, false otherwise
	 */
	public boolean isAutoIncrementField(String name) {
		return autoIncrementFields.contains(name);
	}

	/**
	 * Get all the actions for this entity.
	 * 
	 * @return a Collection of Action
	 */
	public Collection<Action> getActions() {
		return actions.values();
	}

	/**
	 * Get an action by it's code.
	 * 
	 * @param code
	 *            : the code of the action
	 * @return the Action
	 */
	public Action getAction(String code) {
		return actions.get(code);
	}

	/**
	 * Get fields used by search queries.
	 * 
	 * @return A set of field names.
	 */
	public Set<String> getLookupFields() {
		return lookupFields;
	}

	/**
	 * Get the defined values.
	 * 
	 * @param enumName
	 *            Name of the enum.
	 * @param l
	 *            Locale.
	 * @return A map of the values.
	 */
	public Map<String, Object> enumValues(String enumName, Locale l) {
		// On utilise une LinkedHashMap uniquement pour le style, parce que personne ne le fait jamais.
		// Et accessoirement, ça conserve l'ordre d'entrée dans la map.

		// On va utiliser l'ordre pour mettre les valeurs à null en premier, comme ça, on aura l'impression que ce truc marche correctement
		// lorsque "null" est une valeur possible pour les données. Si la valeur de la variable est "null", le tag JSF selectOneValue va se
		// placer sur le premier élément de la liste même si c'est le 3° élément qui correspond à la valeur "null".
		String realEnumName = enumName.substring(0, 1).toLowerCase() + enumName.substring(1);
		EntityField entityField = getField(realEnumName);
		MessageUtils msgUtil = MessageUtils.getInstance(l);

		int nbDefVal = entityField.nbDefinedValues();
		Map<String, Object> mapNull = new LinkedHashMap<String, Object>(nbDefVal);
		Map<String, Object> mapNotNull = new LinkedHashMap<String, Object>(nbDefVal);
		for (int i = 0; i < nbDefVal; i++) {
			Object val = entityField.getDefValValue(i);
			String label = msgUtil.getGenLabel(entityField.getDefValLabel(i), (Object[]) null);
			if (val == null) {
				mapNull.put(label, null);
			} else {
				mapNotNull.put(label, val);
			}
		}

		// Contact maps
		mapNull.putAll(mapNotNull);

		return mapNull;
	}

	/**
	 * Is this entity an associative entity.
	 * 
	 * @return true|false
	 */
	public boolean isAssociative() {
		return isAssociative;
	}

	/**
	 * Get the associative link name by it's name.
	 * 
	 * @param linkName
	 *            : the name of the link
	 * @return null if the entity isn't associative
	 */
	public String getAssociatedLink(String linkName) {
		if (!isAssociative()) {
			return null;
		}
		for (String lName : getLinkNames()) {
			if (!linkName.equals(lName)) {
				return lName;
			}
		}
		return null;
	}

	/**
	 * Check is the given link key is a Strong key.<br>
	 * (ie all fields are mandatory)
	 * 
	 * @param linkName
	 *            the name of the link
	 * @return true if all fields are mandatory, false otherwise
	 */
	public boolean isStrongKey(String linkName) {
		// FIXME : Rework this with constraints ?
		LinkModel linkModel = getLinkModel(linkName);
		if (linkModel == null) {
			throw new TechnicalException("Link " + linkName + " is not a link of entity " + name());
		}
		for (String field : linkModel.getFields()) {
			EntityField entityField = getField(field);
			// if the field is mandatory => strong key
			// if the field is not mandatory and has defined values and null is not a defined value => strong key
			if (!entityField.isMandatory() && entityField.hasNullAsDefinedValues()) {
				// if the field is not mandatory and doesn't have defined values => weak key
				// if the field is not mandatory and has defined values and null is a defined value => weak key
				return false;
			}
		}
		return true;
	}

	/**
	 * Check is the given link is a Virtual.<br>
	 * (ie one of the fields is transient)
	 * 
	 * @param linkName
	 *            the name of the link
	 * @return true if one field is transient, false otherwise
	 */
	public boolean isVirtualLink(String linkName) {
		LinkModel linkModel = getLinkModel(linkName);
		if (linkModel == null) {
			throw new TechnicalException("Link " + linkName + " is not a link of entity " + name());
		}
		for (String field : linkModel.getFields()) {
			if (getField(field).isTransient()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retrieves the name of an entity of a Backref by the name of the link.
	 * 
	 * @param linkName
	 *            Name of the link.
	 * @return The name of the backref Entity.
	 */
	public String getBackRefEntityName(String linkName) {
		String brName = getBackRefModel(linkName).getEntityName();
		EntityModel brMdl = EntityManager.getEntityModel(brName);
		if (brMdl.isAssociative()) {
			brName = brMdl.getLinkModel(brMdl.getAssociatedLink(linkName)).getRefEntityName();
		}
		return brName;
	}

	/**
	 * Indicates if a link is associative.
	 * 
	 * @param linkName
	 *            The name of the link.
	 * @return <code>true</code> if the link is associative.
	 */
	public boolean isAssociativeLink(String linkName) {
		LinkModel brModel = getBackRefModel(linkName);
		if (brModel != null) {
			return EntityManager.getEntityModel(brModel.getEntityName()).isAssociative();
		}
		return false;
	}

	/**
	 * Indicates if the entity is external.
	 * 
	 * @return <code>true</code> if the entity is external.
	 */
	public boolean isExternal() {
		return isExternal;
	}

	/**
	 * Returns this entity's sequence name for auto-increment.
	 * 
	 * @return Sequence name if any, null otherwise
	 */
	public String getSequenceName() {
		return sequenceName;
	}

	/**
	 * Returns this entity's versionning field name.
	 * 
	 * @return Sequence field name if any, null otherwise
	 */
	public String getVersioningField() {
		return versioningField;
	}

	/**
	 * Add a new link to the model (used only for internal init).
	 * 
	 * @param linkModel
	 *            The link Model.
	 */
	void addNewLink(LinkModel linkModel) {
		// Create link
		links.put(linkModel.getLinkName(), linkModel);
	}

	/**
	 * Add a new action to the model (used only for internal init).
	 * 
	 * @param code
	 *            The code of the action.
	 * @param queryName
	 *            The name of the query.
	 * @param pageName
	 *            The name of the page.
	 * @param input
	 *            Input.
	 * @param persistence
	 *            Persistence.
	 * @param ui
	 *            User Interface.
	 * @param process
	 *            Process.
	 * @param pSubActions
	 *            List of the codes of the subactions.
	 */
	void addNewAction(String code, String queryName, String pageName, Input input,
			Persistence persistence, UserInterface ui, Process process, String[] pSubActions) {
		Action action = null;
		if (pSubActions.length == 0) {
			action = new Action(entityName, code, queryName, pageName, input, persistence, ui, process);
		} else {
			String[] subActions = new String[pSubActions.length];
			for (int i = 0; i < pSubActions.length; i++) {
				subActions[i] = pSubActions[i];
			}
			action = new Action(entityName, code, queryName, pageName, input, persistence, ui, process, subActions);
		}
		actions.put(code, action);
	}

	/**
	 * Add a new field to the model (used only for internal init).
	 * 
	 * @param fieldName
	 *            Name of the field.
	 * @param field
	 *            Definition of the field.
	 * @param isLookupField
	 *            Indicates if the field is lookup.
	 * @param isAutoIncrementField
	 *            Indicates if the field is auto-increment.
	 */
	void addNewField(String fieldName, EntityField field, boolean isLookupField, boolean isAutoIncrementField) {
		fields.put(fieldName, field);

		if (isLookupField)
			lookupFields.add(fieldName);

		if (isAutoIncrementField)
			autoIncrementFields.add(fieldName);
	}

	/**
	 * Add a new backref to the model (used only for internal init).
	 * 
	 * @param linkModel
	 *            Link Model.
	 */
	public void addNewBackRef(LinkModel linkModel) {
		backRefs.put(linkModel.getLinkName(), linkModel);
	}

	@Override
	public String toString() {
		return "Model for "+entityName;
	}
}
