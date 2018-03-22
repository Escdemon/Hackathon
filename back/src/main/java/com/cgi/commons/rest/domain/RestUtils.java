package com.cgi.commons.rest.domain;

import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.cgi.commons.db.DB;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.ref.entity.FileContainer;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.KeyModel;
import com.cgi.commons.rest.EndpointConstants;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.reflect.DomainUtils;

/**
 * Utility class for Web Services Rest.
 */
public class RestUtils {

	/**
	 * Logger.
	 */
	private static final Logger logger = Logger.getLogger(RestUtils.class);

	/**
	 * Retrieve a "normal" entity from a "rest" entity.
	 *
	 * @param rEntity
	 *            The Rest Entity.
	 * @param entity
	 *            The Entity.
	 * @param <E>
	 *            Entity type.
	 */
	public static <E extends Entity> void setEntity(RestEntity rEntity, E entity) {
		if (null == rEntity || null == entity) {
			return;
		}
		for (String fieldName : entity.getModel().getFields()) {
			entity.invokeSetter(fieldName, rEntity.invokeGetter(fieldName));
		}
		if (rEntity.getSearchCriteria() != null) {
		    for (Entry<String, String[]> entry : rEntity.getSearchCriteria().entrySet()) {
		        entity.getSearchCriteria().put(entry.getKey(), entry.getValue());
		    }
		}
	}

	/**
	 * Retrieve a "normal" entity from a "rest" entity.
	 *
	 * @param rEntity
	 *            The Rest Entity.
	 * @return The Entity found.
	 */
	public static Entity getEntity(RestEntity rEntity) {
		if (null == rEntity) {
			return null;
		}
		Entity entity = DomainUtils.newDomain(rEntity.name());
		for (String fieldName : entity.getModel().getFields()) {
			if (entity.getModel().getField(fieldName).getSqlType() == SqlTypes.BLOB && fieldName.endsWith("Container")) {
				if (null != rEntity.invokeGetter(fieldName.substring(0, fieldName.length() - "Container".length()))) {
					FileContainer fileContainer = new FileContainer();
					fileContainer.setNull(false);
					fileContainer.setName(fieldName);
					fileContainer.setContent((byte[]) rEntity.invokeGetter(fieldName.substring(0, fieldName.length() - "Container".length())));
				}
				continue;
			}
			entity.invokeSetter(fieldName, rEntity.invokeGetter(fieldName));
		}
		return entity;
	}

	/**
	 * Gets a RestEntity with only blobs loaded.
	 *
	 * @param entity
	 *            the Database Entity
	 * @param fieldToSerialize
	 *            The unique field to serialize, if any, null if we want all the fields
	 * @param ctx
	 *            Opened context to database
	 * @param restEntity
	 *            into serialize
	 * @param <E>
	 *            type of RestEntity
	 */
	public static <E extends RestEntity> void setEntityBlobs(Entity entity, String fieldToSerialize, RequestContext ctx, E restEntity) {
		for (String fieldName : entity.getModel().getFields()) {
			EntityField field = entity.getModel().getField(fieldName);
			boolean blob = field.getSqlType() == SqlTypes.BLOB;
			boolean container = fieldName.endsWith("Container");
			if (blob && !container) {
				try {
					byte[] data = DB.getLobContent(ctx, entity, fieldName);
					restEntity.invokeSetter(fieldName, data);
				} catch (NullPointerException nullPointer) {
					// Silent Catch if there is no blob to load
					logger.debug("No BLOB to load for field " + fieldName + " in entity " + restEntity.name());
				}
			}
		}
	}

	/**
	 * Gets a RestEntity with only blobs loaded.
	 *
	 * @param entity
	 *            the Database Entity
	 * @param fieldToSerialize
	 *            The unique field to serialize, if any, null if we want all the fields
	 * @param ctx
	 *            Opened context to database
	 * @return The RestEntity
	 */
	public static RestEntity getEntityBlobs(Entity entity, String fieldToSerialize, RequestContext ctx) {
		RestEntity rEntity = RestUtils.newRestEntity(entity.name());
		for (String fieldName : entity.getModel().getFields()) {
			EntityField field = entity.getModel().getField(fieldName);
			boolean blob = field.getSqlType() == SqlTypes.BLOB;
			boolean container = fieldName.endsWith("Container");
			if (blob && !container) {
				try {
					byte[] data = DB.getLobContent(ctx, entity, fieldName);
					rEntity.invokeSetter(fieldName, data);
				} catch (NullPointerException nullPointer) {
					// Silent Catch if there is no blob to load
					logger.debug("No BLOB to load for field " + fieldName + " in entity " + rEntity.name());
				}
			}
		}
		return rEntity;
	}

	/**
	 * Serializes an entity WITHOUT blobs.
	 *
	 * @param entity
	 *            The entity to serialize
	 * @param fieldToSerialize
	 *            The unique field to serialize, null if all fields are required
	 * @param restEntity
	 *            into serialize
	 * @param <E>
	 *            type of RestEntity
	 * @return A serializable RestEntity
	 */
	public static <E extends RestEntity> void setRestEntity(Entity entity, String fieldToSerialize, E restEntity) {
		if (null == entity || null == restEntity) {
			return;
		}
		restEntity.setPrimaryKey(entity.getPrimaryKey());
		restEntity.setInternalCaption(entity.description());

		for (String fieldName : entity.getModel().getFields()) {
			if (null == fieldToSerialize || fieldName.equals(fieldToSerialize)) {
				restEntity.invokeSetter(fieldName, entity.invokeGetter(fieldName));
			}
		}
	}

	/**
	 * Serializes an entity WITHOUT blobs.
	 *
	 * @param entity
	 *            The entity to serialize
	 * @param fieldToSerialize
	 *            The unique field to serialize, null if all fields are required
	 * @return A serializable RestEntity
	 */
	public static RestEntity getRestEntity(Entity entity, String fieldToSerialize) {
		if (entity == null) {
			return null;
		}
		RestEntity rEntity = RestUtils.newRestEntity(entity.name());
		for (String fieldName : entity.getModel().getFields()) {
			if (null == fieldToSerialize || fieldName.equals(fieldToSerialize)) {
				rEntity.invokeSetter(fieldName, entity.invokeGetter(fieldName));
			}
		}
		rEntity.setInternalCaption(entity.description());
		return rEntity;
	}

	/**
	 * Create an instance of a RestEntity by its name.
	 *
	 * @param domainName
	 *            The entity name.
	 * @return The new instance.
	 */
	public static RestEntity newRestEntity(String domainName) {
		if (null == domainName || "".equals(domainName)) {
			throw new TechnicalException("Rest Domain object not found " + domainName);
		}
		String className = "com.cgi.models.rest." + domainName.substring(0, 1).toUpperCase() + domainName.substring(1) + "Rest";

		try {
			return (RestEntity) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			throw new TechnicalException("Rest Domain object not found " + className);
		} catch (IllegalAccessException e) {
			throw new TechnicalException("Rest Domain object not found " + className);
		} catch (ClassNotFoundException e) {
			throw new TechnicalException("Rest Domain object not found " + className);
		}
	}

	/**
	 * Format the given key to it's json form.
	 * 
	 * @param key
	 *            Key to format
	 * @return values separated by :::
	 */
	public static String keyToJson(Key key) {
		if (key == null)
			return "";

		String keyStr = "";
		KeyModel keyModel = key.getModel();
		boolean first = true;
		for (String field : keyModel.getFields()) {
			if (!first) {
				keyStr += EndpointConstants.FIELD_SEPARATOR;
			}
			keyStr += key.getValue(field);
			first = false;
		}
		return keyStr;
	}
}
