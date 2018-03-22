package com.cgi.commons.ref.entity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.annotations.EntityDef;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.utils.DateUtils;
import com.cgi.commons.utils.TechnicalException;

/**
 * Entity Manager.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class EntityManager {

	/** Map of Entity models by Entity name. */
	private final static Map<String, EntityModel> models;
	/** All domains of the application. */
	public static final Set<String> domains;

	public enum SpecialValue {
		NOW("*NOW"), TODAY("*TODAY"), BLANK("*BLANK"), TYPE("*TYPE");

		private final String value;

		private SpecialValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static SpecialValue from(Object s) {
			if (s instanceof String)
				for (SpecialValue sv : values()) {
					if (sv.getValue().equals(s)) {
						return sv;
					}
				}
			return null;
		}
	}

	static {
		Reflections reflections = new Reflections(Constants.DOMAIN_OBJECT_PACKAGE);
		Set<Class<?>> entities = reflections.getTypesAnnotatedWith(EntityDef.class);

		domains = new HashSet<String>(entities.size());
		models = new HashMap<String, EntityModel>(entities.size());

		// Load all models
		Map<String, LinkModel> backRefs = new HashMap<String, LinkModel>();
		for (Class entityClass : entities) {
			// Class name is same as entity name with upper first letter
			String entityClassName = entityClass.getSimpleName();
			String entityName = entityClassName.substring(0, 1).toLowerCase() + entityClassName.substring(1);
			domains.add(entityName);

			// Get main annotation
			EntityDef entityDef = (EntityDef) entityClass.getAnnotation(EntityDef.class);
			if (entityDef != null) {
				// Init EntityModel with class properties
				EntityModel entityModel = new EntityModel(entityName, entityDef);

				// Get links
				com.cgi.commons.ref.annotations.Links entityLinks = (com.cgi.commons.ref.annotations.Links) entityClass
						.getAnnotation(com.cgi.commons.ref.annotations.Links.class);
				for (com.cgi.commons.ref.annotations.Link link : entityLinks.value()) {
					// Add Link to model
					LinkModel linkModel = new LinkModel(link.name(), entityName, link.targetEntity(), Arrays.asList(link.fields()), false);
					entityModel.addNewLink(linkModel);

					// Save backRef for later
					backRefs.put(link.name(), linkModel);
				}

				// Get actions
				com.cgi.commons.ref.annotations.Actions entityActions = (com.cgi.commons.ref.annotations.Actions) entityClass
						.getAnnotation(com.cgi.commons.ref.annotations.Actions.class);
				for (com.cgi.commons.ref.annotations.Action action : entityActions.value()) {
					String queryName = action.queryName();
					if ("".equals(queryName)) {
						// If no query, we should use null value
						queryName = null;
					}
					String pageName = action.pageName();
					if ("".equals(pageName)) {
						// No page name, get the default one
						pageName = getPageName(entityName);
					}

					// Add the action to the model
					entityModel.addNewAction(
							action.code(),
							queryName,
							pageName,
							action.input(),
							action.persistence(),
							action.ui(),
							action.process(),
							action.subActions());
				}

				// Get fields
				for (Field classField : entityClass.getDeclaredFields()) {
					com.cgi.commons.ref.annotations.EntityField entityField = classField
							.getAnnotation(com.cgi.commons.ref.annotations.EntityField.class);
					if (entityField != null) {
						// Its a defined field
						String fieldName = classField.getName();
						String className = classField.getType().getName();
						// Create field from annotation values
						EntityField field = new EntityField(entityField.sqlName(),
								entityField.sqlType(),
								entityField.sqlSize(),
								entityField.sqlAccuracy(),
								entityField.memory(),
								entityField.isMandatory(),
								entityField.isLookupField());
						if (!"".equals(entityField.sqlExpr())) {
							field.setSqlExpr(entityField.sqlExpr());
						}
						boolean hasDefaultValue = false;
						if (!"$-$".equals(entityField.defaultValue())) {
							field.setDefaultValue(convert(entityField.defaultValue(), className));
							hasDefaultValue = true;
						}

						// Add field to model
						entityModel.addNewField(fieldName,
								field,
								entityField.isLookupField(),
								entityField.isAutoIncrementField());

						// Check for defined values
						com.cgi.commons.ref.annotations.DefinedValues definedValues = classField
								.getAnnotation(com.cgi.commons.ref.annotations.DefinedValues.class);
						if (definedValues != null) {
							// Field as defined value

							for (com.cgi.commons.ref.annotations.DefinedValue definedValue : definedValues.value()) {
								Object val = convert(definedValue.value(), className);
								field.getDefinedValues().add(new DefinedValue(definedValue.code(), definedValue.label(), val));
							}

							if (!hasDefaultValue) {
								throw new TechnicalException("Field " + fieldName + " for entity " + entityName
										+ " has defined values but no default one !");
							}
						}
					}
				}

				models.put(entityName, entityModel);
			}
		}

		// Store BackRefs
		for (Map.Entry<String, LinkModel> backRef : backRefs.entrySet()) {
			LinkModel linkModel = backRef.getValue();
			EntityModel entityModel = models.get(linkModel.getRefEntityName());

			// Add Link to model
			entityModel.addNewBackRef(linkModel);
		}
	}

	/**
	 * Convert entityName to a page name. (internal use only).
	 * 
	 * @param entityName The Entity Name.
	 * @return The Page Name.
	 */
	private static String getPageName(String entityName) {
		if (entityName == null || "".equals(entityName))
			return entityName;

		char[] strName = entityName.toCharArray();
		char[] strUpper = entityName.toUpperCase().toCharArray();
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < strName.length; i++) {
			if (Character.isUpperCase(strName[i]) && i > 0) {
				result.append('_');
			}

			result.append(strUpper[i]);
		}

		return result.toString();
	}

	/**
	 * Convert the given String to the correct Object type. (internal use only).
	 * 
	 * @param from The given String.
	 * @param className The className to convert into.
	 * @return The given String converted to the class.
	 */
	private static Object convert(String from, String className) {
		if (from == null || "".equals(from))
			return null;

		if (SpecialValue.from(from) != null) {
			// Value is a special keywork (*BLANK, *NOW, *TODAY, *TYPE)
			// must be evaluated when used
			return from;
		}

		try {
			if (String.class.getName().equals(className)) {
				return from;
			} else if (Date.class.getName().equals(className)) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				return sdf.parse(from);
			} else if (Timestamp.class.getName().equals(className)) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
				return new Timestamp(sdf.parse(from).getTime());
			} else if (Time.class.getName().equals(className)) {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				return new Time(sdf.parse(from).getTime());
			} else if (Integer.class.getName().equals(className)) {
				return Integer.valueOf(from);
			} else if (Long.class.getName().equals(className)) {
				return Long.valueOf(from);
			} else if (BigDecimal.class.getName().equals(className)) {
				return BigDecimal.valueOf(Double.valueOf(from));
			} else if (Boolean.class.getName().equals(className)) {
				return "true".equalsIgnoreCase(from);
			}
		} catch (ParseException pe) {
			return null;
		}

		return from; // Should not happen
	}

	/**
	 * 
	 * @param sqlType
	 *            type of the value to be returned
	 * @param sv
	 *            special value code
	 * @return effective value
	 */
	public static Object getSpecialValue(SqlTypes sqlType, int accuracy, SpecialValue sv) {
		Object specialValue = null;

		switch (sv) {
		case NOW:
		case TODAY:
			if (sqlType == SqlTypes.DATE) {
				specialValue = DateUtils.today();
			} else if (sqlType == SqlTypes.TIME) {
				specialValue = DateUtils.now();
			} else if (sqlType == SqlTypes.TIMESTAMP) {
				specialValue = DateUtils.todayNow();
			}
			break;
		case BLANK:
			if (sqlType == SqlTypes.CHAR || sqlType == SqlTypes.VARCHAR || sqlType == SqlTypes.VARCHAR2) {
				specialValue = " ";
			}
			break;
		case TYPE:
			specialValue = getTypeValue(sqlType, accuracy);
			break;
		}

		return specialValue;
	}

	/**
	 * @param sqlType
	 * @return non zero value for known types else null
	 */
	public static Object getTypeValue(SqlTypes sqlType, int accuracy) {
		Calendar nDate = Calendar.getInstance();
		nDate.set(1, 0, 1, 0, 0, 0); // 01/01/0001
		nDate.set(Calendar.MILLISECOND, 0);
		long nullDate = nDate.getTime().getTime();

		if (sqlType == SqlTypes.CHAR || sqlType == SqlTypes.VARCHAR || sqlType == SqlTypes.VARCHAR2)
			return new String(" ");
		else if (sqlType == SqlTypes.CLOB)
			return new String("");
		else if (sqlType == SqlTypes.BLOB)
			return new byte[] {};
		else if (sqlType == SqlTypes.INTEGER)
			return Integer.valueOf(0);
		else if (sqlType == SqlTypes.BOOLEAN)
			return Boolean.FALSE;
		else if (sqlType == SqlTypes.DATE)
			return new java.sql.Date(nullDate);
		else if (sqlType == SqlTypes.TIME)
			return new java.sql.Time(nullDate);
		else if (sqlType == SqlTypes.TIMESTAMP)
			return new java.sql.Timestamp(nullDate);
		else if (sqlType == SqlTypes.DECIMAL) {
			if (accuracy > 0)
				return BigDecimal.ZERO;
			else
				return 0L;
		} else
			return null;
	}

	/**
	 * Get the EntityModel class for the given model name.<br/>
	 * EntityModels are loaded from entity's annotations upon application start.
	 * 
	 * @param entityName the name of the entity
	 * @return the current entityModel
	 */
	public static EntityModel getEntityModel(String entityName) {
		if (entityName == null || "".equals(entityName)) {
			return null;
		}
		if (models.get(entityName) == null) {
			throw new TechnicalException("Impossible de récupérer le Model de la classe " + entityName);
		}
		return models.get(entityName);
	}

	/**
	 * Build a Foreign Key of the Entity, based on a primary key of a linked entity.
	 * 
	 * @param entityName Entity name.
	 * @param refPrimaryKey Primary key of another Entity, linked to the current entity.
	 * @param linkModel The Link Model between the two entities.
	 * @return Foreign Key with same values than refPrimaryKey.
	 */
	public static Key buildForeignKey(String entityName, Key refPrimaryKey, LinkModel linkModel) {
		Key fk = new Key(linkModel);
		for (int i = 0; i < fk.getModel().getFields().size(); i++) {
			fk.setValue(fk.getModel().getFields().get(i),
					refPrimaryKey.getValue(refPrimaryKey.getModel().getFields().get(i)));
		}
		return fk;
	}

	/**
	 * Build a Foreign Key of the Entity, based on a primary key of a linked entity.
	 * 
	 * @param entityName Entity name.
	 * @param refPrimaryKey Primary key of another Entity, linked to the current entity.
	 * @param linkName The name of the link between the two entities.
	 * @return Foreign Key with same values than refPrimaryKey.
	 */
	public static Key buildForeignKey(String entityName, Key refPrimaryKey, String linkName) {
		EntityModel model = EntityManager.getEntityModel(entityName);
		LinkModel linkModel = model.getLinkModel(linkName);
		if (linkModel == null)
			linkModel = model.getBackRefModel(linkName);
		return buildForeignKey(entityName, refPrimaryKey, linkModel);
	}

	/**
	 * Return a set containing all domains of the application.
	 * 
	 * @return A set containing all domains of the application.
	 */
	public static Set<String> getDomains() {
		return domains;
	}
}
