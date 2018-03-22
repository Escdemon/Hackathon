package com.cgi.commons.ref.entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.cgi.commons.db.DB;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.utils.DateUtils;
import com.cgi.commons.utils.Diff;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TechnicalException;

/**
 * Entity.
 */
public abstract class Entity implements Cloneable {

	/** Logger. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(Entity.class);

	/** Links and related entity currently loaded by user. */
	private Links links = new Links(getModel(), false);
	/** BackRefs and related entity currently loaded by user. */
	private Links backRefs = new Links(getModel(), true);

	/** Copy of Primary Key with "initial" values, just after we got the bean out of database. */
	private Key initialKey = null;

	/**
	 * This map is a generic container used to store search criteria when they don't "fit" in classic class members. For instance : multiple
	 * values for a unique field, 2 dates for between search, etc.<br/>
	 * <br/>
	 * Data will be stored with tableAlias_columnAlias as key
	 */
	private Map<String, Object> searchCriteria = new HashMap<String, Object>();

	/**
	 * Backup instance of current entity. This backup is not intended to business use. Main purpose is internal / technical operations only.
	 */
	protected Entity internalBackup;

	/** 
	 * Entity name.
	 * @return name.
	 */
	public abstract String name();

	/** 
	 * Entity caption for this instance.
	 * @return description.
	 */
	public abstract String description();

	/** Cache for all reflexive access on getters / setters to avoid lookups. */
	private static Map<String, Map<String, Method>> reflectCache = new HashMap<String, Map<String, Method>>();

	/** Default constructor. */
	public Entity() {
		// Default constructor
	}

	/**
	 * Copy constructor - Uses reflexivity.
	 * @param e Entity to duplicates.
	 */
	public Entity(Entity e) {
		for (String f : getModel().getFields()) {
			invokeSetter(f, e.invokeGetter(f));
		}
	}

	/**
	 * Gets entity metadata (field names, field mandatory, database definition, business actions, etc. 
	 * @return	EntityModel class loaded from bean annotations
	 */
	public EntityModel getModel() {
		return EntityManager.getEntityModel(name());
	}

	/** 
	 * Returns non final fields of this entity. Useful for reflexive calls on all fields
	 * @return	List of non final java.lang.reflect.Field objects 
	 * 
	 * @deprecated use getModel().getFields() instead
	 */
	@Deprecated
	public List<Field> getAllFields() {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Field> fList = new ArrayList<Field>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!Modifier.isFinal(field.getModifiers())) {
				fList.add(field);
			}
		}
		return fList;
	}

	/** 
	 * Returns non memory, non final fields of this entity. Useful for reflexive calls on all fields
	 * @return	List of non final java.lang.reflect.Field objects 
	 * 
	 * @deprecated use getModel().getFields() instead coupled with Entity field metadata
	 */
	@Deprecated
	public List<Field> getFields() {
		Field[] fields = this.getClass().getDeclaredFields();
		List<Field> fList = new ArrayList<Field>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if (!Modifier.isFinal(field.getModifiers()) && getModel().getField(field.getName()).isFromDatabase()) {
				fList.add(field);
			}
		}
		return fList;
	}

	/** 
	 * Returns the names of all calculated fields on the entity. 
	 * 
	 * @return a list.
	 */
	public List<String> getTransientFields() {
		List<String> fList = new ArrayList<String>();
		for (String fieldName : getModel().getFields()) {
			if (getModel().getField(fieldName).isTransient()) {
				fList.add(fieldName);
			}
		}
		return fList;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("[");
		for (String fieldName : getModel().getFields()) {
			if (ret.length() > 1) {
				ret.append(",");
			}
			ret.append(fieldName).append("=").append(invokeGetter(fieldName));
		}
		ret.append("]");
		return ret.toString();
	}

	/** 
	 * Creates a map from the entity fields.
	 * 
	 * @return a Map.
	 */
	public Map<String, Object> dump() {
		Map<String, Object> dump = new HashMap<String, Object>();
		for (String fieldName : getModel().getFields()) {
			Object v = invokeGetter(fieldName);
			if (v != null) {
				dump.put(fieldName, v);
			}
		}
		return dump;
	}

	/**
	 * Returns current instance primary key.
	 * @return	Key object based on this entity key model with values
	 */
	public final Key getPrimaryKey() {
		KeyModel km = getModel().getKeyModel();
		Key key = new Key(km);
		for (String field : km.getFields()) {
			Object value = null;
			try {
				value = invokeGetter(field);
			} catch (Exception e) {
				value = null;
			}
			key.setValue(field, value);
		}
		return key;
	}

	/**
	 * Returns the foreign key for the link name.
	 * @param linkName The link Name.
	 * @return The Foreign Key, if found.
	 */
	public final Key getForeignKey(String linkName) {
		LinkModel linkModel = getModel().getLinkModel(linkName);
		if (linkModel == null) {
			linkModel = getModel().getBackRefModel(linkName);
		}
		if (linkModel == null) {
			return null;
		}

		String refEntityName = linkModel.getRefEntityName();

		Key key = new Key(refEntityName);
		for (int i = 0; i < linkModel.getFields().size(); i++) {
			Object value = null;
			try {
				value = invokeGetter(linkModel.getFields().get(i));
			} catch (Exception e) {
				value = null;
			}
			key.setValue(key.getModel().getFields().get(i), value);
		}
		return key;
	}

	/**
	 * Save values for fields of the entity which are mapped to a foreign key.
	 * 
	 * @param linkName The link Name.
	 * @param key The Foreign Key with the new values.
	 */
	public void setForeignKey(String linkName, Key key) {
		LinkModel fk = getModel().getLinkModel(linkName);
		for (int i = 0; i < fk.getFields().size(); i++) {
			String varName = fk.getFields().get(i);
			Object value = null;
			if (key != null) {
				value = key.getValue(key.getModel().getFields().get(i));
			}
			invokeSetter(varName, value);
		}
	}

	/**
	 * Save values for fields of the entity which are mapped to the primary key.
	 * 
	 * @param key The Primary Key with the new values.
	 */
	public void setPrimaryKey(Key key) {
		KeyModel pkModel = getModel().getKeyModel();
		for (int i = 0; i < pkModel.getFields().size(); i++) {
			Object value = null;
			if (key != null) {
				value = key.getValue(key.getModel().getFields().get(i));
			}
			invokeSetter(pkModel.getFields().get(i), value);
		}
	}

	/**
	 * Invoke the setter method for the fieldname, with the value into parameters.
	 * 
	 * @param fieldName Field Name.
	 * @param value Value.
	 */
	public void invokeSetter(String fieldName, Object value) {
		try {
			String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			if (reflectCache.get(name()) == null) {
				reflectCache.put(name(), new HashMap<String, Method>());
			}
			if (reflectCache.get(name()).get(methodName) == null) {
				reflectCache.get(name()).put(methodName,
						this.getClass().getMethod(methodName, this.getClass().getDeclaredField(fieldName).getType()));
			}
			reflectCache.get(name()).get(methodName).invoke(this, value);
		} catch (SecurityException e) {
			throw new TechnicalException("Unable to set value " + value + " in field " + fieldName, e);
		} catch (NoSuchMethodException e) {
			throw new TechnicalException("Unable to set value " + value + " in field " + fieldName, e);
		} catch (IllegalArgumentException e) {
			throw new TechnicalException("Unable to set value " + value + " in field " + fieldName, e);
		} catch (IllegalAccessException e) {
			throw new TechnicalException("Unable to set value " + value + " in field " + fieldName, e);
		} catch (InvocationTargetException e) {
			throw new TechnicalException("Unable to set value " + value + " in field " + fieldName, e);
		} catch (NoSuchFieldException e) {
			throw new TechnicalException("Unable to set value " + value + " in field " + fieldName, e);
		}
	}

	/**
	 * Invoke the getter method for a fieldname, and returns the value.
	 * 
	 * @param fieldName Field Name.
	 * @return The Value.
	 */
	public Object invokeGetter(String fieldName) {
		try {
			String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			if (reflectCache.get(name()) == null) {
				reflectCache.put(name(), new HashMap<String, Method>());
			}
			if (reflectCache.get(name()).get(methodName) == null) {
				reflectCache.get(name()).put(methodName, this.getClass().getMethod(methodName));
			}
			return reflectCache.get(name()).get(methodName).invoke(this);
		} catch (SecurityException e) {
			throw new TechnicalException("Unable to get value from field " + fieldName, e);
		} catch (IllegalArgumentException e) {
			throw new TechnicalException("Unable to get value from field " + fieldName, e);
		} catch (IllegalAccessException e) {
			throw new TechnicalException("Unable to get value from field " + fieldName, e);
		} catch (InvocationTargetException e) {
			throw new TechnicalException("Unable to get value from field " + fieldName, e);
		} catch (NoSuchMethodException e) {
			throw new TechnicalException("Unable to get value from field " + fieldName, e);
		}
	}

	/**
	 * Returns the links of the Entity.
	 * @return the links of the Entity.
	 */
	public Links getLinks() {
		return links;
	}

	/**
	 * Retrieves a link by his name.
	 * @param linkName The link Name.
	 * @return the link.
	 */
	public Link getLink(String linkName) {
		return links.get(linkName);
	}
	
	/**
	 * Returns the BackRefs of the Entity.
	 * @return the BackRefs of the Entity.
	 */
	public Links getBackRefs() {
		return backRefs;
	}

	/**
	 * Retrieves a BackRef by his name.
	 * @param linkName The BackRef Name.
	 * @return the BackRef.
	 */
	public Link getBackRef(String linkName) {
		return backRefs.get(linkName);
	}

	/**
	 * Remove the default values.
	 */
	public void removeDefaultValues() {
		for (String fieldName : getModel().getFields()) {
			invokeSetter(fieldName, null);
		}
	}

	/**
	 * Reset entity.
	 */
	public void resetEntity() {
		searchCriteria.clear();
		for (String fieldName : getModel().getFields()) {
			invokeSetter(fieldName, null);
		}
	}

	/**
	 * Loads the current bean with all values of the bean instance given in parameter.
	 * 
	 * @param e
	 *            Bean to load values from
	 */
	public void syncFromBean(Entity e) {
		for (String f : getModel().getFields()) {
			invokeSetter(f, e.invokeGetter(f));
		}
	}

	/**
	 * Compares current instance to internal backup. Returns {@code true} if any of the entity fields has changed since last backup.
	 * 
	 * @return {@code true} if at least one field has changed since backup or if the internal backup is {@code null} and at least one field is
	 *         not {@code null}, {@code false} otherwise
	 */
	public boolean hasChangedSinceBackup() {
		if (internalBackup == null) {
			for (String field : getModel().getFields()) {
				if (invokeGetter(field) != null) {
					return true;
				}
			}
		}
		for (String fieldName : getModel().getFields()) {
			Object thisField = invokeGetter(fieldName);
			Object backupField = internalBackup.invokeGetter(fieldName);
			if (thisField == null && backupField != null || thisField != null && !thisField.equals(backupField)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Compares the current instance to corresponding data in database. This method will access database using context ctx and compare all fields
	 * stored in database. BLOB and CLOB fields are ignored. transient variables are ignored.
	 * 
	 * @param ctx
	 *            Current context with opened database connection.
	 * @return <code>true</code> if some data is different between current Entity and database value, <code>false</code> if they are the same.
	 *         This method returns <code>true</code> if the current instance has no primary key or if there is no matching database instance.
	 */
	public boolean hasChanged(RequestContext ctx) {
		Key pk = getPrimaryKey();
		if (!pk.isFull()) {
			return true;
		}

		Entity dbEntity = DB.get(name(), pk, ctx);
		if (dbEntity == null) { // No matching instance in database
			return true;
		}

		for (String fieldName : getModel().getFields()) {
			EntityField fieldMetadata = getModel().getField(fieldName);
			if (fieldMetadata.isTransient()) {
				continue; // We don't care about transient data
			}
			if (SqlTypes.BLOB == fieldMetadata.getSqlType()) {
				continue; // We don't care about BLOBs
			}
			Object obj = invokeGetter(fieldName);
			Object dbObj = dbEntity.invokeGetter(fieldName);
			if (obj == null && dbObj != null || obj != null && !obj.equals(dbObj)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculate the difference between this entity and another one. The current entity is "mine" and the other entity is "theirs" (in the
	 * {@link Diff} object). Only the fields wich are actually different are present in the map.
	 * 
	 * @param that The Another One Entity.
	 * @param ctx Current Context.
	 * @return A map where each entry associates a field to the difference in value.
	 */
	public Map<String, Diff> difference(Entity that, RequestContext ctx) {
		if (that == null || !that.getClass().equals(this.getClass())) {
			throw new IllegalArgumentException(String.valueOf(that));
		}

		Map<String, Diff> result = new HashMap<String, Diff>();

		for (String fieldName : getModel().getFields()) {

			EntityField fieldMetadata = getModel().getField(fieldName);
			if (fieldMetadata.isTransient()) {
				continue; // We don't care about transient data
			}

			Object thisValue = this.invokeGetter(fieldName);
			Object thatValue = that.invokeGetter(fieldName);
			if (thisValue == null && thatValue != null || thisValue != null && !thisValue.equals(thatValue)) {
				String label = MessageUtils.getInstance(ctx).getGenLabel(this.getModel().name() + "." + fieldName);
				if (SqlTypes.BLOB == fieldMetadata.getSqlType() || SqlTypes.CLOB == fieldMetadata.getSqlType()) {
					result.put(fieldName, new Diff(label));
				} else {
					result.put(fieldName, new Diff(label, thisValue, thatValue));
				}
			}
		}

		return result;
	}

	/**
	 * Serialize this entity.
	 * @return The serialization.
	 */
	public String serialize() {
		StringBuilder serialized = new StringBuilder();
		for (Field field : getAllFields()) {
			Object obj = invokeGetter(field.getName());
			String res = serializeObject(obj);
			if (res != null && !res.isEmpty()) {
				if (serialized.length() > 0) {
					serialized.append(",");
				}
				serialized.append(field.getName()).append("='").append(res.replace("'", "\\'")).append("'");
			}
		}
		for (String key : links.keySet()) {
			Link link = links.get(key);
			if (link != null && link.getEncodedValue() != null && !link.getEncodedValue().isEmpty()) {
				if (serialized.length() > 0) {
					serialized.append(",");
				}
				serialized.append("links.").append(key).append("='").append(link.getEncodedValue().replace("'", "\\'")).append("'");
			}
		}
		for (String key : backRefs.keySet()) {
			Link link = backRefs.get(key);
			if (link != null && link.getEncodedValue() != null && !link.getEncodedValue().isEmpty()) {
				if (serialized.length() > 0) {
					serialized.append(",");
				}
				serialized.append("backRefs.").append(key).append("='").append(link.getEncodedValue().replace("'", "\\'")).append("'");
			}
		}
		return serialized.toString();
	}

	/**
	 * Serialize an object.
	 * @param obj The object to serialize.
	 * @return The serialization.
	 */
	private static String serializeObject(Object obj) {
		if (obj == null) {
			return null;
		}
		String res = null;
		if (obj instanceof String) {
			res = (String) obj;
		} else if (obj instanceof Integer) {
			res = String.valueOf((Integer) obj);
		} else if (obj instanceof Timestamp) {
			res = DateUtils.formatDateHeure((Timestamp) obj);
		} else if (obj instanceof Time) {
			res = DateUtils.formatHeure((Time) obj);
		} else if (obj instanceof Date) {
			res = DateUtils.formatDate((Date) obj);
		} else if (obj instanceof BigDecimal) {
			res = String.valueOf((BigDecimal) obj);
		} else if (obj instanceof Boolean) {
			res = String.valueOf((Boolean) obj);
		}
		return res;
	}

	/**
	 * Deserialize a value of a field.
	 * @param fieldName The name of the field.
	 * @param value The value, serialized.
	 * @return The value, unserialized.
	 */
	public Object deserializeValue(String fieldName, String value) {
		Object val = value;
		Field f;
		try {
			f = this.getClass().getDeclaredField(fieldName);
		} catch (SecurityException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		} catch (NoSuchFieldException e) {
			throw new TechnicalException("Impossible de fixer la valeur " + value + " à " + fieldName, e);
		}
		String convertType = "";
		try {
			if (Timestamp.class.equals(f.getGenericType())) {
				convertType = MessageUtils.getInstance((Locale) null).getMessage("entity.deserializeValue.convert.type.timestamp");
				val = DateUtils.stringToTimestamp(value);
			} else if (Time.class.equals(f.getGenericType())) {
				convertType = MessageUtils.getInstance((Locale) null).getMessage("entity.deserializeValue.convert.type.time");
				val = DateUtils.stringToTime(value);
			} else if (Date.class.equals(f.getGenericType())) {
				convertType = MessageUtils.getInstance((Locale) null).getMessage("entity.deserializeValue.convert.type.date");
				val = DateUtils.stringToDate(value);
			} else if (BigDecimal.class.equals(f.getGenericType())) {
				convertType = MessageUtils.getInstance((Locale) null).getMessage("entity.deserializeValue.convert.type.bigdecimal");
				val = new BigDecimal(value);
			} else if (Integer.class.equals(f.getGenericType())) {
				convertType = MessageUtils.getInstance((Locale) null).getMessage("entity.deserializeValue.convert.type.integer");
				val = new Integer(value);
			} else if (Boolean.class.equals(f.getGenericType())) {
				convertType = MessageUtils.getInstance((Locale) null).getMessage("entity.deserializeValue.convert.type.boolean");
				val = "true".equalsIgnoreCase(value);
			}
		} catch (Exception ex) {
			throw new TechnicalException(MessageUtils.getInstance((Locale) null)
					.getMessage("entity.deserializeValue.convert.error", value, convertType, fieldName), ex);
		}
		return val;
	}

	/**
	 * Loads binary content of given field from database into the FileContainer.
	 * 
	 * @param fieldName
	 *            Field to load content
	 * @param context
	 *            Current request context
	 */
	public void loadLobVariable(String fieldName, RequestContext context) {
		EntityField field = getModel().getField(fieldName);
		if (field.getSqlType() != SqlTypes.BLOB && field.getSqlType() != SqlTypes.CLOB) {
			// Not a binary field
			return;
		}

		FileContainer fieldValue = (FileContainer)invokeGetter(fieldName);
		fieldValue.setContent(DB.getLobContent(context, this, fieldName));
	}

	@Override
	public Entity clone() {
		try {
			return (Entity) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new TechnicalException(e.getMessage(), e);
		}
	}

	/**
	 * Creates a clone of current instance and stores it in property internalBackup. This backup is not intended to business use. Main purpose is
	 * internal / technical operations only.
	 * 
	 * A call on this method will remove previous backup instance before creating a new one.
	 */
	public void backup() {
		internalBackup = clone();
	}

	/**
	 * Access to current internal backup.
	 * 
	 * @return A reference to current internal backup. <b>This is a <u>reference</u> to the backup instance. This is <u>not</u> a clone of
	 *         current backup.</b>
	 */
	public Entity getInternalBackup() {
		return internalBackup;
	}

	/**
	 * Reset Links and BackRefs.
	 */
	public void resetLinksAndBackRefs() {
		links = new Links(getModel(), false);
		backRefs = new Links(getModel(), true);
	}

	/**
	 * Returns the initial Key.
	 * @return The initial Key.
	 */
	public Key getInitialKey() {
		return initialKey;
	}

	/**
	 * Setter for the initial Key.
	 * @param initialKey The initial Key.
	 */
	public void setInitialKey(Key initialKey) {
		this.initialKey = initialKey;
	}
	
	/**
	 * @return the searchCriteria
	 */
	public Map<String, Object> getSearchCriteria() {
		return searchCriteria;
	}

}
