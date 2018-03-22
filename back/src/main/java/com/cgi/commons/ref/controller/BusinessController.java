package com.cgi.commons.ref.controller;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.cgi.commons.db.DB;
import com.cgi.commons.db.DbManager;
import com.cgi.commons.db.DbQuery;
import com.cgi.commons.db.DbQuery.Var;
import com.cgi.commons.logic.DomainLogic;
import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.data.BackRefData;
import com.cgi.commons.ref.data.ColumnData;
import com.cgi.commons.ref.data.ComboData;
import com.cgi.commons.ref.data.LinkData;
import com.cgi.commons.ref.data.ListCategoryData;
import com.cgi.commons.ref.data.ListCriteria;
import com.cgi.commons.ref.data.ListData;
import com.cgi.commons.ref.data.Message;
import com.cgi.commons.ref.data.Message.Severity;
import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Action.Input;
import com.cgi.commons.ref.entity.Action.Persistence;
import com.cgi.commons.ref.entity.Action.Process;
import com.cgi.commons.ref.entity.Action.UserInterface;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.FileContainer;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.Link;
import com.cgi.commons.ref.entity.LinkModel;
import com.cgi.commons.security.AbstractSecurityManager;
import com.cgi.commons.security.SecurityUtils;
import com.cgi.commons.utils.ApplicationUtils;
import com.cgi.commons.utils.DbException;
import com.cgi.commons.utils.FunctionalException;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.export.XlsWriter;
import com.cgi.commons.utils.export.XlsxWriter;
import com.cgi.commons.utils.reflect.DomainUtils;

/**
 * Controller for all business calls.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BusinessController implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 6503760540891178991L;

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(BusinessController.class);

	/** Link path to field with errors */
	public static final String ERROR_FIELD_LINK_PATH = "errorFieldLinkPath";

	/**
	 * Process a request from a web client. This is usually called when the user starts an action. This method may : <br/>
	 * - Call custom code to get the input key if needed <br/>
	 * - load an entity from database <br/>
	 * - split the request in many individual requests <br/>
	 * - override the request with custom code to start a different request instead <br/>
	 * - load User Interface if any and send the response to user<br/>
	 * - start a validation request if there's no user interface to display <br/>
	 * 
	 * @param request
	 *            The request to process.
	 * @return The Response to display if any, null otherwise.
	 */
	public Response process(Request request) {
		RequestContext context = request.getContext();

		// Initialization of response to send to this request
		Response response = new Response(request);

		// If response's main entity is not loaded, we load it
		if (response.getEntity() == null) {
			response.setEntity(getEntity(response, context));
		}

		// When action starts through links, we may attach related entity to the current main entity
		attachLinkedEntity(response);

		// When action is an INPUT_ONE, we'll handle ONE key at a time. Other selected keys, if any, are kept for later in response and they will
		// be processed when current request processing ends.
		if (response.getAction().getInput() == Input.ONE && response.getKeys() != null && response.getKeys().size() > 1) {
			splitProcessedKeys(response);
		}

		// Call custom method doCtrlOverrideAction until there's no override to do. Response may be completely different.
		response = overrideResponse(response, context);

		// Response is now initialized. If response has a UserInterface, we'll load it and send it back to UI level.
		if (response.getAction().getUi() != UserInterface.NONE) {
			// Loads UI components of the view (title, visible and protected attributes, etc.)
			return response;
		}

		// Response has no UI, we start validation right now
		Request validationRequest = new Request(response, context);
		validate(validationRequest);
		if (response.getRemKeys() != null && response.getRemKeys().size() > 0) {
			// We finished current action, there are remaining keys that were selected but not processed (typically, with a INPUT ONE / DISPLAY
			// NONE action). We create a new request and start processing.
			Request remRequest = new Request<Entity>(response.getRemEntityName(), response.getRemAction(), response.getRemKeys(),
					request.getQueryName(), request.getLinkName(), request.isBackRef());
			remRequest.setLinkedEntity(request.getLinkedEntity());
			remRequest.setContext(context);
			return process(remRequest);
		} else {
			// Nothing more to do.
			return null;
		}
	}

	/**
	 * When request has been launched through a (non associative) back reference, we'll link the response entity to its parent entity
	 * "in memory". This will allow domain logic code to access parent memory objects.
	 * 
	 * @param response
	 *            Response to send to the user.
	 */
	private void attachLinkedEntity(Response<?> response) {
		if (response.getLinkName() != null && response.isBackRef() && response.getLinkedEntity() != null
				&& !response.getLinkedEntity().getModel().isAssociativeLink(response.getLinkName())
				&& (response.getAction().getInput() == Input.ONE || response.getAction().getInput() == Input.NONE)) {
			// Avoid N-N links
			if (response.getEntity() == null) {
				LOGGER.error("Null response entity");
			} else if (response.getEntity().getLink(response.getLinkName()) == null) {
				LOGGER.error("Link "+ response.getLinkName() +" not registered for entity "+ response.getEntity().name());
			} else {
				response.getEntity().getLink(response.getLinkName()).setEntity(response.getLinkedEntity());
			}
		}
	}

	/**
	 * Splits a list of keys in one key + remaining keys. This method is used when response's action is an INPUT_ONE action (one entity processed
	 * at a time), and there are more than one key selected. We'll keep only the first selected key as selected key, and store remaining keys for
	 * later processing.
	 * 
	 * @param response
	 *            A response with more than on key in keys list and an INPUT_ONE action. Method will keep only one key in its keys list, and
	 *            other keys in remaining keys list (remKeys). remAction and remEntityName will store initial response information that could
	 *            will allow to process these keys later.
	 */
	private void splitProcessedKeys(Response response) {
		response.setRemAction(response.getAction());
		response.setRemEntityName(response.getEntityName());
		List<Key> remKeys = response.getKeys().subList(1, response.getKeys().size());
		response.setRemKeys(remKeys);
		response.setKeys(response.getKeys().subList(0, 1));
	}

	/**
	 * Call the custom method doCtrlOverrideAction. The new Response can be totally different.
	 * 
	 * @param response The current response
	 * @param context The request context
	 * @return The new response if overriden
	 */
	private Response overrideResponse(Response response, RequestContext context) {
		DomainLogic domainLogic = DomainUtils.getLogic(response.getEntityName(), context);
		Response overrideResponse = domainLogic.internalDoCtrlOverrideAction(response, context);
		while (overrideResponse != null) {
			response = overrideResponse;
			if (response.getEntity() == null) {
				response.setEntity(getEntity(response, context));
			}
			domainLogic = DomainUtils.getLogic(overrideResponse.getEntityName(), context);
			overrideResponse = domainLogic.internalDoCtrlOverrideAction(response, context);
		}
		return response;
	}

	/**
	 * Validates a user request. This method will call : <br/>
	 * - onValidation domain logic method<br/>
	 * - store main entity backup<br/>
	 * - validation on inner link entities if any<br/>
	 * - a validation sub-method (validateStandard, validateCustom, validateWebService) according to action type<br/>
	 * - validation on inner back ref entities if any<br/>
	 * - commit transaction if action persistence type is not NONE<br/>
	 * 
	 * When there's an error during validation, transaction is not commited and entities are rollbacked.
	 * 
	 * @param <E> The Entity class
	 * @param request
	 *            The request to validate
	 */
	public <E extends Entity> void validate(Request<E> request) {
		RequestContext context = request.getContext();
		Action action = request.getAction();

		E mainEntityBackup = (E) request.getEntity().clone();
		backupInner(request.getEntity(), mainEntityBackup, null);

		try {
			if (action.getProcess() == Process.STANDARD || action.getProcess() == Process.LINK) {
				String linkPath = "";
				validateInner(request, request.getEntity(), false, linkPath, null);
				request.getContext().putCustomData(ERROR_FIELD_LINK_PATH, linkPath);
				if (action.getProcess() == Process.STANDARD) {
					validateStandard(request);
				} else {
					validateLink(request);
				}
				validateInner(request, request.getEntity(), true, linkPath, null);
			} else if (action.getProcess() == Process.CUSTOM) {
				validateCustom(request);
			}

		} catch (RuntimeException e) {
			restoreBackup(request, mainEntityBackup);
			throw e;
		} finally { 
			request.getContext().removeCustomData(ERROR_FIELD_LINK_PATH);
		}
		if (action.getPersistence() != Persistence.NONE) {
			try {
				context.getDbConnection().commit();
			} catch (DbException sqlEx) {
				restoreBackup(request, mainEntityBackup);
				throw new TechnicalException(sqlEx.getMessage(), sqlEx);
			}
		}
	}

	/**
	 * Backups main entity values and linked entities values. This method is used to backup entity to the state it is before dbOnSave call.
	 * 
	 * @param <E> The Entity class
	 * @param entity
	 *            Entity to backup
	 * @param entityBackup
	 *            Backup
	 */
	private <E extends Entity> void backupInner(E entity, E entityBackup, String parentLinkName) {
		for (String linkName : entity.getBackRefs().keySet()) {
			Link backRef = entity.getBackRef(linkName);
			if (backRef.getEntity() != null && !linkName.equals(parentLinkName)) {
				Entity backRefBackup = backRef.getEntity().clone();
				entityBackup.getBackRef(linkName).setEntity(backRefBackup);
				backupInner(backRef.getEntity(), backRefBackup, linkName);
			}
		}
		for (String linkName : entity.getLinks().keySet()) {
			Link link = entity.getLink(linkName);
			if (link.getEntity() != null && !linkName.equals(parentLinkName)) {
				Entity linkBackup = link.getEntity().clone();
				entityBackup.getLink(linkName).setEntity(linkBackup);
				backupInner(link.getEntity(), linkBackup, linkName);
			}
		}
	}

	/**
	 * Restores main entity values and linked entities values with backup values. This method is used to "reset" entity to the state it was
	 * before dbOnSave call.
	 * 
	 * @param <E> The Entity class
	 * @param request
	 *            Current validation request that provoke an error
	 * @param mainEntityBackup
	 *            Main page entity backup to restore
	 */
	private <E extends Entity> void restoreBackup(Request<E> request, E mainEntityBackup) {
		request.getEntity().syncFromBean(mainEntityBackup);
		restoreInnerBackup(request, request.getEntity(), mainEntityBackup, null);
	}

	/**
	 * Restores innerLinks and innerBackrefs backups. This method is called recursively.
	 * 
	 * @param <E> The Entity class
	 * @param request
	 *            Current validation request that provoke an error
	 * @param entity
	 *            Entity to reset
	 * @param entityBackup
	 *            Entity backup to restore
	 */
	private <E extends Entity> void restoreInnerBackup(Request<?> request, E entity, E entityBackup, String parentLinkName) {
		for (String linkName : entity.getLinks().keySet()) {
			Link link = entity.getLink(linkName);
			if (link.getEntity() != null && !linkName.equals(parentLinkName)) {
				Entity linkBackup = entityBackup.getLink(linkName).getEntity();
				link.getEntity().syncFromBean(linkBackup);
				restoreInnerBackup(request, link.getEntity(), linkBackup, linkName);
			}
		}
		for (String linkName : entity.getBackRefs().keySet()) {
			Link backRef = entity.getBackRef(linkName);
			if (backRef.getEntity() != null && !linkName.equals(parentLinkName)) {
				Entity backRefBackup = entityBackup.getBackRef(linkName).getEntity();
				backRef.getEntity().syncFromBean(backRefBackup);
				restoreInnerBackup(request, backRef.getEntity(), backRefBackup, linkName);
			}
		}
	}

	/**
	 * Validate a standard input (not inner template) and call DB for persistence.
	 * 
	 * @param <E> The Entity class
	 * @param request Current validation request
	 */
	public <E extends Entity> void validateStandard(Request<E> request) {
		RequestContext context = request.getContext();
		Action action = request.getAction();
		Entity entity = request.getEntity();
		Entity linkedEntity = request.getLinkedEntity();
		String linkName = request.getLinkName();

		if (Persistence.INSERT.equals(action.getPersistence())) {
			DB.insert(entity, action, context);
		} else if (Persistence.DELETE.equals(action.getPersistence())) {
			DB.remove(entity, action, context);
		} else if (Persistence.UPDATE.equals(action.getPersistence())) {
			DB.update(entity, action, context);
		}

		// Action has been started through a link, we may need to update linked entity based on what have been done on the current entity.
		// (created an element, modified the primary key, attached or detached an element)
		if (linkName != null && !request.isBackRef()) {
			if (action.getPersistence() == Persistence.INSERT) {
				linkedEntity.setForeignKey(linkName, entity.getPrimaryKey());
			}
			// Mark linked entity as dirty in context. View controller will set dirty state on next view to trigger popup in case of cancel
			request.getContext().getAttributes().put(Constants.MARK_LINKED_ENTITY_DIRTY, Boolean.TRUE);
		}
	}

	/**
	 * Validate a link input (not inner template) and call DB for persistence.
	 *
	 * @param <E> The Entity class
	 * @param request Current validation request
	 */
	public <E extends Entity> void validateLink(Request<E> request) {
		RequestContext context = request.getContext();
		Action action = request.getAction();
		List<Key> keys = request.getKeys();
		Entity linkedEntity = request.getLinkedEntity();
		String linkName = request.getLinkName();

		if (request.isBackRef() && action.isAttach()) {
			selectBackRef(linkedEntity, action, linkName, keys, context);
		} else if (request.isBackRef() && action.isDetach()) {
			detachBackRef(linkedEntity, action, linkName, keys, context);
		}

		// Action has been started through a link, we may need to update linked entity based on what have been done on the current entity.
		// (created an element, modified the primary key, attached or detached an element)
		if (linkName != null && !request.isBackRef()) {
			if (action.isAttach()) {
				validateAttachLink(linkedEntity, linkName, keys.get(0));
				// Invalidate linked entity cache because we may have changed the target.
				linkedEntity.getLink(linkName).setEntity(null);
			} else if (action.isDetach()) {
				validateDetachLink(linkedEntity, linkName);
				// Invalidate linked entity after delete / detach.
				linkedEntity.getLink(linkName).setEntity(null);
			}
			// Mark linked entity as dirty in context. View controller will set dirty state on next view to trigger popup in case of cancel
			request.getContext().getAttributes().put(Constants.MARK_LINKED_ENTITY_DIRTY, Boolean.TRUE);
		}
	}

	/**
	 * Calculate fields that need to be set to null when detatching a link
	 * 
	 * @param linkedEntity
	 *            Source entity
	 * @param linkName
	 *            Current link
	 */
	public void validateDetachLink(Entity linkedEntity, String linkName) {
		EntityModel model = linkedEntity.getModel();
		LinkModel linkModel = model.getLinkModel(linkName);

		// We want to find all "non-enclosing" links. An non-enclosing link is a link based on a foreign key that completely contains the current
		// link foreign key.
		// For example, in a structure like this
		// COUNTRY (PK: COUNTRY_CODE)
		// STATE (PK: COUNTRY_CODE, STATE_CODE; FK to COUNTRY: COUNTRY_CODE)
		// CITY (PK: COUNTRY_CODE, STATE_CODE, CITY_CODE; FK to COUNTRY: COUNTRY_CODE, FK to STATE: STATE_CODE, CITY_CODE)
		//
		// Current entity references COUNTRY, STATE and CITY, in the detach STATE link process, the link to CITY is considered as an enclosing
		// link because the set of its variables (COUNTRY_CODE, STATE_CODE, CITY_CODE) contains all the STATE link variables (COUNTRY_CODE,
		// STATE_CODE). The country link is a non enclosing link because it doesn't contain the STATE_CODE variable.
		Set<LinkModel> nonEnclosingLinks = new HashSet<>();
		for (String otherLinkName : model.getLinkNames()) {
			if (otherLinkName.equals(linkName)) {
				continue;
			}
			LinkModel otherLinkModel = model.getLinkModel(otherLinkName);
			if (!otherLinkModel.getFields().containsAll(linkModel.getFields())) {
				nonEnclosingLinks.add(otherLinkModel);
			}
		}

		// Find impacted fields. We need to preserve variables that are shared by other links, except by enclosing links
		Set<String> impactedFields = new HashSet<>();
		for (String field : linkModel.getFields()) {
			boolean shared = false;
			for (LinkModel otherLinkModel : nonEnclosingLinks) {
				if (otherLinkModel.getFields().contains(field)) {
					shared = true;
					break;
				}
			}
			if (!shared) {
				impactedFields.add(field);
			}
		}

		// All fields are used by enclosed links: we have to invalidate all fields
		if (impactedFields.size() == 0) {
			impactedFields.addAll(linkModel.getFields());
		}

		// Set to null impacted fields
		for (String field : impactedFields) {
			linkedEntity.invokeSetter(field, null);
		}
	}


	/**
	 * Populate linked entity fields with link's keys fields when a key is selected in an attach action
	 * 
	 * @param linkedEntity
	 *            Source entity
	 * @param linkName
	 *            Current link
	 * @param key
	 *            Selected key to attach
	 */
	public void validateAttachLink(Entity linkedEntity, String linkName, Key key) {
		EntityModel model = linkedEntity.getModel();
		LinkModel linkModel = model.getLinkModel(linkName);

		// Get all key fields that have been changed
		Set<String> updatedFields = new HashSet<String>();
		for (int i = 0; i < key.getModel().getFields().size(); i++) {
			String field = key.getModel().getFields().get(i);
			// Get the field in the FK matching the PK field
			String fkField = linkModel.getFields().get(i);
			Object newValue = key.getValue(field);
			Object oldValue = linkedEntity.invokeGetter(fkField);
			if ((newValue == null && oldValue != null)
					|| (newValue != null && !newValue.equals(oldValue))) {
				updatedFields.add(fkField);
				// Update field
				linkedEntity.invokeSetter(fkField, key.getValue(field));
			}
		}

		// List field that can be impacted by the change: we need to invalidate other links
		Set<String> impactedFields = new HashSet<String>();
		for (String link : model.getLinkNames()) {
			List<String> linkFields = model.getLinkModel(link).getFields();
			// Only process keys containing one of the updated fields
			if (updatedFields.containsAll(linkFields) || Collections.disjoint(updatedFields, linkFields))
				continue;
			Set<String> keyImpactedFields = new HashSet<String>();
			for (String field : linkFields) {
				// Only process keys that may have change
				if (!updatedFields.contains(field)) {
					if (!isSharedField(linkedEntity, field, model, link)) {
						keyImpactedFields.add(field);
					}
				}
			}
			// All key fields are shared between links: invalid all links
			if (keyImpactedFields.containsAll(linkFields)) {
				impactedFields.addAll(linkFields);
			// Only set to null fields that are not shared: invalid current link without breacking others
			} else {
				impactedFields.addAll(keyImpactedFields);
			}
		}

		// Do not modify fields from the current link
		impactedFields.removeAll(linkModel.getFields());

		// Set all impacted fields to null
		for (String field : impactedFields) {
			linkedEntity.invokeSetter(field, null);
		}
	}

	/**
	 * Define if the field is used by another link
	 * 
	 * @param field
	 *            Current field
	 * @param model
	 *            Field's entity model
	 * @param linkName
	 *            Current link name
	 * @return true if field is shared by other foreign keys
	 */
	private boolean isSharedField(Entity linkedEntity, String field, EntityModel model, String linkName) {
		for (String link : model.getLinkNames()) {
			if (!link.equals(linkName)) {
				if (linkedEntity.getForeignKey(link).isFull()) {
					for (String f : model.getLinkModel(link).getFields()) {
						if (field.equals(f)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Define if the field is part of a full fk in the source entity
	 * 
	 * @param field
	 * @param model
	 * @param linkedEntity
	 * @param linkName
	 * @return
	 */
	private boolean isPartOfFullFk(String field, EntityModel model, Entity linkedEntity, String linkName) {
		for (String link : model.getLinkNames()) {
			if (!link.equals(linkName)) {
				for (String f : model.getLinkModel(link).getFields()) {
					if (field.equals(f) && linkedEntity.getForeignKey(link).isFull()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Validates recursively links and backrefs in the entity. Validation (or not) is based on applyActionOnLink property on Links elements. 
	 * 
	 * @param <E> The Entity class
	 * @param request Current validation request
	 * @param entity current entity. Method is first called on root entity in the current action and recursively called on links / backrefs. 
	 * @param validateBackRef when this parameter is true, method processes the backrefs, otherwise, it processes links. 
	 */
	private <E extends Entity> void validateInner(Request<?> request, E entity, boolean validateBackRef, String linkPath,
			Boolean insideBackRefInner) {
		if (entity == null) {
			return;
		}
		if (validateBackRef) {
			for (String linkName : entity.getBackRefs().keySet()) {
				if (linkPath.substring(linkPath.lastIndexOf(".") + 1).equals(linkName) && Boolean.FALSE.equals(insideBackRefInner)) {
					continue;
				}
				Link backRef = entity.getBackRef(linkName);
				String subLinkPath = (linkPath.length() > 0 ? (linkPath + ".") : "") + linkName;
				validateInner(request, backRef.getEntity(), false, subLinkPath, Boolean.TRUE);
				if (backRef.isApplyActionOnLink()) {
					// Execution of an action on a backRef, so we may have to set / update the foreign key in child entity to make it reference
					// our entity's primary key
					if (request.getAction().getPersistence() == Persistence.INSERT || request.getAction().getPersistence() == Persistence.UPDATE) {
						backRef.getEntity().setForeignKey(backRef.getModel().getLinkName(), entity.getPrimaryKey());
					}
					applyActionOnLink(request, backRef.getEntity(), subLinkPath);
				}
				validateInner(request, backRef.getEntity(), true, subLinkPath, Boolean.TRUE);
			}
		} else {
			for (String linkName : entity.getLinks().keySet()) {
				if (linkPath.substring(linkPath.lastIndexOf(".") + 1).equals(linkName) && Boolean.TRUE.equals(insideBackRefInner)) {
					continue;
				}
				Link link = entity.getLink(linkName);
				String subLinkPath = (linkPath.length() > 0 ? (linkPath + ".") : "") + linkName;
				validateInner(request, link.getEntity(), false, subLinkPath, Boolean.FALSE);
				if (link.isApplyActionOnLink()) {
					applyActionOnLink(request, link.getEntity(), subLinkPath);
					// We executed an action on a link, so we can now update the foreign key in child entity
					if (request.getAction().getPersistence() == Persistence.INSERT || request.getAction().getPersistence() == Persistence.UPDATE) {
						entity.setForeignKey(linkName, link.getEntity().getPrimaryKey());
					} else if (request.getAction().getPersistence() == Persistence.DELETE) {
						entity.setForeignKey(linkName, null);
					}
				}
				validateInner(request, link.getEntity(), true, subLinkPath, Boolean.FALSE);
			}
		}
	}

	/**
	 * Persist or remove a link.
	 * 
	 * @param request The current validation request
	 * @param entity the main bean of the page.
	 */
	private void applyActionOnLink(Request<?> request, Entity entity, String linkPath) {
		request.getContext().putCustomData(ERROR_FIELD_LINK_PATH, linkPath);
		Action action = request.getAction();
		if (action.getPersistence() == Persistence.INSERT || action.getPersistence() == Persistence.UPDATE) {
			DB.persist(entity, action, request.getContext());
		} else if (action.getPersistence() == Persistence.DELETE) {
			DB.remove(entity, action, request.getContext());
		}
	}

	/**
	 * Call the custom method doCustomAction.
	 * 
	 * @param <E> The Entity class
	 * @param request The current validation request
	 */
	public <E extends Entity> void validateCustom(Request<E> request) {
		RequestContext context = request.getContext();
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(request.getEntityName(), context);
		Action action = request.getAction();
		if (action.getInput() == Input.NONE || action.getInput() == Input.ONE) {
			domainLogic.internalDoCustomAction(request, request.getEntity(), context);
		} else {
			domainLogic.internalDoCustomAction(request, request.getEntity(), request.getKeys(), context);
		}
	}

	/**
	 * Persist an assocation.
	 * 
	 * @param linkedEntity The linked entity
	 * @param action The action 
	 * @param linkName The name of the link used to make the assocation
	 * @param keys The keys of the beans to associate
	 * @param context The request context
	 */
	public void selectBackRef(Entity linkedEntity, Action action, String linkName, List<Key> keys, RequestContext context) {
		Link link = linkedEntity.getBackRef(linkName);
		if (EntityManager.getEntityModel(link.getModel().getEntityName()).isAssociative()) {
			DB.persistAssociations(linkedEntity, linkName, action, keys, context);
		} else {
			for (Key selectedKey : keys) {
				Entity selectedEntity = DB.get(link.getModel().getEntityName(), selectedKey, action, context);
				selectedEntity.setForeignKey(link.getModel().getLinkName(), linkedEntity.getPrimaryKey());
				selectedEntity.getLink(linkName).setEntity(linkedEntity);
				DB.persist(selectedEntity, action, context);
			}
		}
	}

	/**
	 * Remove an assocation.
	 * 
	 * @param linkedEntity The linked entity
	 * @param action The action 
	 * @param linkName The name of the link used to make the assocation
	 * @param keys The keys of the beans to associate
	 * @param context The request context
	 */
	public void detachBackRef(Entity linkedEntity, Action action, String linkName, List<Key> keys, RequestContext context) {
		Link link = linkedEntity.getBackRef(linkName);
		if (EntityManager.getEntityModel(link.getModel().getEntityName()).isAssociative()) {
			DB.removeAssociations(linkedEntity, linkName, action, keys, context);
		} else {
			for (Key selectedKey : keys) {
				Entity selectedEntity = DB.get(link.getModel().getEntityName(), selectedKey, action, context);
				selectedEntity.setForeignKey(link.getModel().getLinkName(), null);
				selectedEntity.getLink(linkName).setEntity(linkedEntity);
				DB.persist(selectedEntity, action, context);
			}
		}

	}

	/**
	 * Load an entity, depends of the response.<br>
	 * If the action is an "input one" action, load the entity thanks to the primary key.<br>
	 * If the action is a query, load a criteria, so a new empty entity.<br>
	 * If the action is a creation or insert action, load a new empty entity.
	 * Call the custom method dbPostLoad. 
	 *  
	 * @param response The current response.
	 * @param context The current context.
	 * @return The entity loaded.
	 */
	public Entity getEntity(Response response, RequestContext context) {
		Entity entity = null;
		Action action = response.getAction();
		if (action == null) {
			return null;
		}
		if (action.getInput() == Input.ONE) {
			// load bean
			Key pk = ((List<Key>) response.getKeys()).get(0);
			if (EntityManager.getEntityModel(response.getEntityName()).isExternal()) {
				DomainLogic domainLogic = DomainUtils.getLogic(response.getEntityName(), context);
				entity = domainLogic.internalExtActionLoad(response.getEntityName(), pk, action, context);
			} else {
				entity = DB.get(response.getEntityName(), pk, action, context);
			}
		} else if (action.getInput() == Input.QUERY) {
			// Criteria
			entity = DomainUtils.newDomain(response.getEntityName());
			entity.removeDefaultValues();
			// We are in a selection list
			if (action.isAttach() && !response.isBackRef()) {
				Entity linkedEntity = response.getLinkedEntity();
				EntityModel model = linkedEntity.getModel();
				LinkModel linkModel = model.getLinkModel(response.getLinkName());
				if (linkModel == null) {
					throw new TechnicalException("Link '"+response.getLinkName()+"' not found in entity "+linkedEntity.name());
				}
				// Adds fields that are part of a full foreign key in the source entity
				for (int i = 0; i < linkModel.getFields().size(); i++) {
					String field = linkModel.getFields().get(i);
					Object value = linkedEntity.invokeGetter(field);
					if (isPartOfFullFk(field, model, linkedEntity, response.getLinkName()) && value != null) {
						String pkField = entity.getPrimaryKey().getModel().getFields().get(i);
						entity.invokeSetter(pkField, value);
					}
				}
			}
			DomainLogic domainLogic = DomainUtils.getLogic(response.getEntityName(), context);
			domainLogic.internalDbPostLoad(entity, action, context);
		} else {
			// initializes an empty bean ("creation action" or "multi process at a time")
			entity = DomainUtils.newDomain(response.getEntityName());
			// keep default values on create
			if (action.getInput() != Input.NONE)
				entity.removeDefaultValues();
			if (response.getLinkName() != null && response.isBackRef() && response.getLinkedEntity() != null) {
				// Avoid N-N links
				if (!response.getLinkedEntity().getModel().isAssociativeLink(response.getLinkName())) {
					// Action launched through back ref template --> Initialize foreign key
					entity.setForeignKey(response.getLinkName(), response.getLinkedEntity().getPrimaryKey());
				}
			}
			DomainLogic domainLogic = DomainUtils.getLogic(response.getEntityName(), context);
			domainLogic.internalDbPostLoad(entity, action, context);
		}
		return entity;
	}

	/**
	 * Load an entity for an inner template. <br>
	 * If action of modification or display, the entity is load from database.<br>
	 * If action of creation or insert, the entity is a new empty entity.
	 * 
	 * @param <E> The Entity class
	 * @param entity The main bean of the page.
	 * @param entityName The name of the entity of the inner template.
	 * @param linkName The link between main entity and inner entity.
	 * @param action The current action.
	 * @param context The current context.
	 * @return The entity loaded.
	 */
	public <E extends Entity> Entity getLinkInnerEntity(Entity entity, String entityName, String linkName, Action action, RequestContext context) {
		Entity innerEntity = null;
		if (EntityManager.getEntityModel(entityName).isExternal()) {
			DomainLogic domainLogic = DomainUtils.getLogic(entityName, context);
			innerEntity = domainLogic.internalExtActionLoad(entityName, entity.getForeignKey(linkName), action, context);
		} else {
			innerEntity = DB.getRef(entity, linkName, action, context);
			if (innerEntity == null) {
				innerEntity = DomainUtils.newDomain(entityName);
				innerEntity.removeDefaultValues();
				DomainLogic domainLogic = DomainUtils.getLogic(entityName, context);
				domainLogic.internalDbPostLoad(innerEntity, action, context);
			}
		}
		return innerEntity;
	}

	/**
	 * Load an entity for a back ref. <br>
	 * If action of modification or display, the entity is load from database.<br>
	 * If action of creation or insert, the entity is a new empty entity.
	 * 
	 * @param entity The main bean of the page.
	 * @param entityName The name of the entity linked.
	 * @param backRefName Name of the back ref link.
	 * @param action The current action.
	 * @param context The current context.
	 * @return The entity loaded.
	 */
	public Entity getUniqueBackRefInnerEntity(Entity entity, String entityName, String backRefName, Action action, RequestContext context) {
		Entity innerEntity = null;
		if (EntityManager.getEntityModel(entityName).isExternal()) {
			DomainLogic domainLogic = DomainUtils.getLogic(entityName, context);
			innerEntity = domainLogic.internalExtActionLoad(entityName, entity.getForeignKey(EntityManager.getEntityModel(entityName).getBackRefModel(backRefName).getLinkName()), action, context);
		} else {
			innerEntity = DB.getUniqueBackRef(entity, backRefName, action, context);
			if (innerEntity == null) {
				innerEntity = DomainUtils.newDomain(entityName);
				DomainLogic domainLogic = DomainUtils.getLogic(entityName, context);
				domainLogic.internalDbPostLoad(innerEntity, action, context);
			}
		}
		innerEntity.setForeignKey(entity.getModel().getBackRefModel(backRefName).getLinkName(), entity.getPrimaryKey());
		return innerEntity;
	}

	/**
	 * Prepares data for a list.
	 * 
	 * @param <E> The Entity class
	 * @param entityName
	 *            Entity name
	 * @param queryName
	 *            Query Name
	 * @param criteria
	 *            List criteria's value
	 * @param context
	 *            Current request context
	 * @return The datas returned by the query.
	 */
	public <E extends Entity> ListData getListData(String entityName, String queryName, ListCriteria<E> criteria, RequestContext context) {
		ListData data = null;
		if (EntityManager.getEntityModel(entityName).isExternal()) {
			DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(entityName, context);
			data = domainLogic.internalExtQueryLoad(context, entityName, queryName);
		} else {
			// Apply user criteria to query
			DbQuery query = DB.getQuery(context, entityName, queryName);
			query.setMinRownum(criteria.minRow);
			query.setMaxRownum(criteria.maxRow);
			query.setFetchSize(Math.min(criteria.maxRow, Constants.MAX_ROW));
			addOrderIntoQuery(criteria, query);

			Map<String, ColumnData> columnsMetadata = getColumnsMetadata(query, context);
			try {
				DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(entityName, context);
				
				// Check if user has implemented query override
				data = domainLogic.internalDbLoad(query, criteria, columnsMetadata, context);
				if (data != null) {
					// Check returned data
					// TODO Verif des PKS
					
					// Put list metadata
					if (data.getColumns().isEmpty()) {
						data.setColumns(columnsMetadata);
					}
				} else {
					// No override, use standard process
					domainLogic.internalDbQueryPrepare(query, criteria, context);
					data = getListData(query, true, columnsMetadata, context);
				}
			} catch (FunctionalException e) {
				data = getEmptyListData(query, columnsMetadata, context); // Prevent from having a null ListData
			} catch (TechnicalException e) {
				data = getEmptyListData(query, columnsMetadata, context); // Prevent from having a null ListData
				context.getMessages().add(
						new Message(
								MessageUtils.getInstance(context).getMessage("uiControlerModel.queryExecError",
										new Object[] { (Object) e.getMessage() }),
								Severity.ERROR));
				LOGGER.error(e.getMessage(), e);
			}
		}
		data.setActionRights(getActionRights(entityName, null, null, null, null, context));
		return data;
	}

	/**
	 * Prepares data for a list.
	 * 
	 * @param query The query to execute.
	 * @param count True if we want to have a count on the query.
	 * @param columnsMetadata Columns data (title, visibility).
	 * @param context The current context.
	 * @return The datas returned by the query.
	 */
	private ListData getListData(DbQuery query, boolean count, Map<String, ColumnData> columnsMetadata, RequestContext context) {
		ListData data = null;
		DbManager dbManager = null;

		// Get data from base
		try {
			dbManager = new DbManager(context, query);
			data = dbManager.getListData();
			if (count) {
				data.setTotalRowCount(dbManager.count());
			}

			// Put list metadata
			if (columnsMetadata == null) {
				columnsMetadata = getColumnsMetadata(query, context);
			}
			data.setColumns(columnsMetadata);
		} catch (FunctionalException e) {
			data = getEmptyListData(query, columnsMetadata, context); // Prevent from having a null ListData
		} catch (TechnicalException e) {
			data = getEmptyListData(query, columnsMetadata, context); // Prevent from having a null ListData
			context.getMessages().add(new Message(
					MessageUtils.getInstance(context).getMessage("uiControlerModel.queryExecError", new Object[] { (Object) e.getMessage() }),
					Severity.ERROR));
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (dbManager != null) {
				dbManager.close();
			}
		}

		return data;
	}
	
	/**
	 * Prepare columns data : caption and visibility
	 * 
	 * @param query The query to execute.
	 * @param context The current context.
	 * @return The columns data
	 */
	private Map<String, ColumnData> getColumnsMetadata(DbQuery query, RequestContext context) {
		Map<String, ColumnData> data = new HashMap<>();
		DomainLogic domainLogic = DomainUtils.getLogic(query.getMainEntity().name(), context);
		for (DbQuery.Var var : query.getOutVars()) {
			String columnKey = var.tableId + "_" + var.name;
			ColumnData columnData = new ColumnData();
			columnData.setTitle(domainLogic.internalDbQueryColumnCaption(query, null, columnKey, context));
			columnData.setVisible(domainLogic.internalDbQueryColumnIsVisible(query, null, columnKey, context));
			data.put(columnKey, columnData);
		}
		return data;
	}

	/**
	 * Prepare an empty list of datas.
	 * 
	 * @param query The query to execute.
	 * @param columnsMetadata Columns data (title, visibility).
	 * @param context The current context.
	 * @return The datas with only column and column caption.
	 */
	private ListData getEmptyListData(DbQuery query, Map<String, ColumnData> columnsMetadata, RequestContext context) {
		ListData data;
		if (query.getCategoryBreak().isEmpty()) {
			data = new ListData(query.getMainEntity().name());
		} else {
			data = new ListCategoryData(query.getMainEntity().name());
		}

		// Put list metadata (either if success and error)
		if (columnsMetadata == null) {
			columnsMetadata = getColumnsMetadata(query, context);
		}
		data.setColumns(columnsMetadata);

		return data;
	}

	/**
	 * Prepare data for a list of back ref elements.
	 * 
	 * @param <E> The Entity class
	 * @param targetEntity The target entity.
	 * @param entityName The name of the main entity.
	 * @param linkName The link name.
	 * @param queryName The query name.
	 * @param action The current action.
	 * @param linkActions Attach and detach actions.
	 * @param context The current context.
	 * @return The datas returned by the query.
	 */
	public <E extends Entity> ListData getBackRefListData(E targetEntity, String entityName, String linkName, String queryName, Action action,
			Action[] linkActions, RequestContext context) {
		return getBackRefListData(targetEntity, entityName, linkName, queryName, null, action, linkActions, context);
	}

	/**
	 * Prepare data for a list of back ref elements.
	 * 
	 * @param <E> The Entity class
	 * @param targetEntity The target entity.
	 * @param entityName The name of the main entity.
	 * @param linkName The link name.
	 * @param queryName The query name.
	 * @param criteria The criteria.
	 * @param action The current action.
	 * @param linkActions Attach and detach actions.
	 * @param context The current context.
	 * @return The datas returned by the query.
	 */
	public <E extends Entity> ListData getBackRefListData(E targetEntity, String entityName, String linkName, String queryName,
			ListCriteria criteria, Action action, Action[] linkActions, RequestContext context) {
		DbQuery query = DB.getLinkQuery(targetEntity, linkName, queryName, false, context);
		query.setMinRownum(criteria.minRow);
		query.setMaxRownum(criteria.maxRow);
		query.setFetchSize(Math.min(criteria.maxRow, Constants.MAX_ROW));
		// If parent primary key is not full, back ref list is necessarily empty
		if (!targetEntity.getPrimaryKey().isFull()) {
			ListData data = getEmptyListData(query, null, context);
			data.setActionRights(getActionRights(query.getMainEntity().name(), targetEntity, linkName, queryName, linkActions, context));
			return data;
		}
		DomainLogic sourceDomainLogic = DomainUtils.getLogic(query.getMainEntity().name(), context);
		addOrderIntoQuery(criteria, query);
		Map<String, ColumnData> columnsMetadata = getColumnsMetadata(query, context);
		ListData data;
		try {
			data = sourceDomainLogic.internalDbLoad(query, criteria, columnsMetadata, context);
			if (data != null) {
				// Put list metadata
				if (data.getColumns().isEmpty()) {
					data.setColumns(columnsMetadata);
				}
			} else {
				sourceDomainLogic.internalDbQueryPrepare(query, criteria, context);
				data = getListData(query, true, columnsMetadata, context);
			}
		} catch (FunctionalException e) {
			data = getEmptyListData(query, columnsMetadata, context); // Prevent from having a null ListData
		} catch (TechnicalException e) {
			data = getEmptyListData(query, columnsMetadata, context); // Prevent from having a null ListData
			context.getMessages().add(
					new Message(
							MessageUtils.getInstance(context).getMessage("uiControlerModel.queryExecError",
									new Object[] { (Object) e.getMessage() }),
							Severity.ERROR));
			LOGGER.error(e.getMessage(), e);
		}
		data.setActionRights(getActionRights(query.getMainEntity().name(), targetEntity, linkName, queryName, linkActions, context));

		return data;
	}

	/**
	 * If criteria contains an order by, it's added to the query
	 * 
	 * @param criteria
	 *            Search criterias
	 * @param query
	 *            Target query
	 */
	private void addOrderIntoQuery(ListCriteria criteria, DbQuery query) {
		if (criteria.orderByField != null) {
			Var v = query.getOutVar(criteria.orderByField);
			query.addSortBy(v.name, v.tableId, criteria.orderByDirection, true);
		}
	}

	/**
	 * Prepare data for a list of back ref elements.
	 * 
	 * @param <E> The Entity class
	 * @param targetEntity The target entity.
	 * @param entityName The name of the main entity.
	 * @param linkName The link name.
	 * @param queryName The query name.
	 * @param pk The primary key of the linked entity.
	 * @param context The current context.
	 * @return The datas returned by the query.
	 */
	public <E extends Entity> ListData getBackRefListDataSingleElement(E targetEntity, String entityName, String linkName, String queryName,
			Key pk,
			RequestContext context) {
		DbQuery query = DB.getLinkQuery(targetEntity, linkName, queryName, false, context);
		// If parent primary key is not full, back ref list is necessarily empty
		if (!targetEntity.getPrimaryKey().isFull()) {
			return getEmptyListData(query, null, context);
		}
		query.addCondKey(pk, query.getMainEntityAlias());

		Map<String, ColumnData> columnsMetadata = getColumnsMetadata(query, context);
		ListData data = null;
		try {
			DomainLogic domainLogic = DomainUtils.getLogic(query.getMainEntity(), context);
			// Create "dummy" criteria
			ListCriteria<E> criteria = new ListCriteria<>(targetEntity, linkName, null);
			 // FIXME Add other data to criteria ?
			data = domainLogic.internalDbLoad(query, criteria, columnsMetadata, context);
			if (data != null) {
				// Put list metadata
				if (data.getColumns().isEmpty()) {
					data.setColumns(columnsMetadata);
				}
			} else {
				domainLogic.internalDbQueryPrepare(query, criteria, context);
			}
			data = getListData(query, false, columnsMetadata, context);
		} catch (FunctionalException e) {
			data = getEmptyListData(query, columnsMetadata, context); // Prevent from having a null ListData
		} catch (TechnicalException e) {
			data = getEmptyListData(query, columnsMetadata, context); // Prevent from having a null ListData
			context.getMessages().add(
					new Message(
							MessageUtils.getInstance(context).getMessage("uiControlerModel.queryExecError",
									new Object[] { (Object) e.getMessage() }),
							Severity.ERROR));
			LOGGER.error(e.getMessage(), e);
		}
		return data;
	}

	/**
	 * Prepare data for a combo box.
	 * 
	 * @param sourceEntity The entity source of the link.
	 * @param entityName The name of the entity.
	 * @param linkName The link beetween the entity source and the target entity.
	 * @param filterName The filter name.
	 * @param action The current action.
	 * @param context The current context.
	 * @return The datas.
	 */
	public ComboData getLinkComboData(Entity sourceEntity, String entityName, String linkName, String filterName, Action action,
			RequestContext context) {
		Link link = sourceEntity.getLink(linkName);
		DomainLogic domainLogic = DomainUtils.getLogic(sourceEntity.name(), context);
		DbQuery filterQuery = null;
		if (filterName != null) {
			filterQuery = DB.getQuery(context, entityName, filterName);
		}
		Map<Key, String> comboValues = domainLogic.internalDoLinkLoadCombo(sourceEntity, link.getModel(), filterQuery, action, context);
		ComboData comboData = new ComboData(link.getModel().getRefEntityName(), comboValues);
		return comboData;
	}

	/**
	 * Prepare data for a link.
	 * 
	 * @param sourceEntity The entity source of the link.
	 * @param entityName The name of the entity.
	 * @param linkName The link beetween the entity source and the target entity.
	 * @param action The current action.
	 * @param linkActions Attach and detach actions.
	 * @param context The current context.
	 * @return The datas.
	 */
	public LinkData getLinkData(Entity sourceEntity, String entityName, String linkName, Action action, Action[] linkActions, RequestContext context) {
		LinkData data;
		if (sourceEntity == null) {
			data = new LinkData(entityName, null, null);
		} else {
			Entity targetEntity = null;
			String description = null;
			if (EntityManager.getEntityModel(entityName).isExternal()) {
				DomainLogic<Entity> targetEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(entityName, context);
				targetEntity = targetEntityLogic.internalExtActionLoad(entityName, sourceEntity.getForeignKey(linkName), action, context);
			} else {
				targetEntity = DB.getRef(sourceEntity, linkName, action, context);
			}
			if (targetEntity != null) {
				description = getEntityDescription(targetEntity, context);
			}
			data = new LinkData(entityName, targetEntity, description);
		}
		data.setActionRights(getActionRights(entityName, sourceEntity, linkName, null, linkActions, context));
		return data;
	}

	/**
	 * Get an object's description through a doDescription() call
	 * 
	 * @param entity - object
	 * @param context - current request context
	 * @return object's description
	 */
	public String getEntityDescription(Entity entity, RequestContext context) {
		DomainLogic<Entity> targetEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(entity.name(), context);
		return targetEntityLogic.internalDoDescription(entity, context);
	}

	/**
	 * Prepare data for a list of back ref elements.
	 * 
	 * @param targetEntity The target entity.
	 * @param entityName The name of the main entity.
	 * @param backRefName The name of the back ref link.
	 * @param action The current action.
	 * @param linkActions Attach and detach actions.
	 * @param context The current context.
	 * @return The datas.
	 */
	public BackRefData getUniqueBackRefData(Entity targetEntity, String entityName, String backRefName, Action action, Action[] linkActions, RequestContext context) {
		BackRefData data;

		if (targetEntity == null || !targetEntity.getPrimaryKey().isFull()) {
			data = new BackRefData(entityName, null, null);
		} else {
			Entity sourceEntity = null;
			String description = null;
			if (EntityManager.getEntityModel(entityName).isExternal()) {
				DomainLogic<Entity> sourceEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(entityName, context);
				// We don't know source key, we give the target PK to the method
				sourceEntity = sourceEntityLogic.internalExtActionLoad(entityName, targetEntity.getPrimaryKey(), action, context);
			} else {
				sourceEntity = DB.getUniqueBackRef(targetEntity, backRefName, action, context);
			}
			if (sourceEntity != null) {
				DomainLogic<Entity> sourceEntityLogic = (DomainLogic<Entity>) DomainUtils.getLogic(entityName, context);
				description = sourceEntityLogic.internalDoDescription(sourceEntity, context);
			}
			data = new BackRefData(entityName, sourceEntity, description);
		}
		data.setActionRights(getActionRights(entityName, targetEntity, backRefName, null, linkActions, context));
		return data;
	}

	/**
	 * Prepare data for a quick search.
	 * 
	 * @param sourceEntity The source entity.
	 * @param entityName The entity name.
	 * @param linkName The link name.
	 * @param filterName The filter name.
	 * @param criteria The criteria.
	 * @param context The current context.
	 * @return A map key value of the datas (primary key - description)
	 */
	public Map<String, String> getLinkQuickSearchData(Entity sourceEntity, String entityName, String linkName, String filterName,
			String criteria,
			RequestContext context) {
		Map<String, String> result = new LinkedHashMap<>();
		Link link = sourceEntity.getLink(linkName);
		DbQuery filterQuery = null;

		filterQuery = DB.getQuery(context, entityName, filterName);
		filterQuery.setCaseInsensitiveSearch(true);

		List<? extends Var> columns = filterQuery.getOutVars();
		List<String> colAliases = new ArrayList<>();
		List<String> tableAliases = new ArrayList<>();
		Set<String> lookupFields = EntityManager.getEntityModel(entityName).getLookupFields();

		if (lookupFields.isEmpty()) {
			for (Var var : columns) {
				if (!var.model.isTransient() && var.model.isAlpha()) {
					colAliases.add(var.name);
					tableAliases.add(var.tableId);
				}
			}
		} else {
			String tableAlias = filterQuery.getMainEntityAlias();
			for (String field : lookupFields) {
				colAliases.add(field);
				tableAliases.add(tableAlias);
			}
		}

		StringTokenizer st = new StringTokenizer(criteria, " ");

		while (st.hasMoreTokens()) {
			String sText = st.nextToken();
			filterQuery.addCondLikeConcat(colAliases, tableAliases, sText, false);
		}
		DomainLogic domainLogic = (DomainLogic) DomainUtils.getLogic(sourceEntity.name(), context);
		try {
			Map<Key, String> values = domainLogic.internalDoLinkLoadValues(sourceEntity, link.getModel(), filterQuery, true, context);
			if (null == values || values.isEmpty()) {
				result.put("-1", MessageUtils.getInstance(context).getMessage("autocomplete.noResult", (Object[]) null));
			} else if (values.size() > Constants.AUTOCOMPLETE_MAX_ROW) {
				result.put("-1", MessageUtils.getInstance(context).getMessage("autocomplete.tooManyResults", new Object[] { values.size() }));
			} else {
				for (Entry<Key, String> e : values.entrySet()) {
					result.put(e.getKey().getEncodedValue(), e.getValue());
				}
			}
		} catch (TechnicalException exception) {
			result.put("-1", MessageUtils.getInstance(context).getMessage("autocomplete.error", (Object[]) null));
		}
		return result;
	}

	/**
	 * Generate the count queries for the menu in order to display or not the notifications. 
	 * 
	 * @param menuQueries The list of the queries.
	 * @param context The current context.
	 * @return The map of the results (integers).
	 */
	public Map<String, Integer> generateMenuCounters(Map<String, String[]> menuQueries, RequestContext context) {
		Map<String, Integer> menuCounters = new HashMap<>();
		context.getAttributes().put("QUERY_TYPE", "count");
		for (Entry<String, String[]> menuEntry : menuQueries.entrySet()) {
			DbManager dbManager = null;
			try {
				String entityName = menuEntry.getValue()[0];
				String queryName = menuEntry.getValue()[1];
				// Get query
				DbQuery query = DB.getQuery(context, entityName, queryName);
				// Load domainLogic
				DomainLogic domainLogic = DomainUtils.getLogic(entityName, context);
				// Prepare query
				Entity entity = DomainUtils.newDomain(entityName);
				entity.removeDefaultValues();
				// Create search criteria
				ListCriteria criteria = new ListCriteria<>();
				criteria.action = Action.getListAction(queryName, null);
				criteria.searchEntity = entity;
				domainLogic.internalDbQueryPrepare(query, criteria, context);
				// Execute and store result
				menuCounters.put(menuEntry.getKey(), DbManager.count(context, query));
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			} finally {
				if (dbManager != null) {
					dbManager.close();
				}
			}
		}
		context.getAttributes().remove("QUERY_TYPE");
		return menuCounters;
	}

	/**
	 * Get a file container.
	 * 
	 * @param entity The entity.
	 * @param varName The name of the variable.
	 * @param action The current action.
	 * @param context The current context.
	 * @return The file container of the variable for the entity.
	 */
	public FileContainer getFile(Entity entity, String varName, Action action, RequestContext context) {
		// get the current value (already computed if transient var)
		FileContainer container = (FileContainer) entity.invokeGetter(varName);

		if (!entity.getModel().getField(varName).isTransient()) {
			// db value is lazy loaded here
			byte[] content = DB.getLobContent(context, entity, varName);
			container.setContent(content);
		}
		return container;
	}

	/**
	 * Prepare datas for export Excel.
	 * 
	 * @param <E> The Entity class
	 * @param entity The entity.
	 * @param entityName The entity name.
	 * @param queryName The query name.
	 * @param criteria The criteria.
	 * @param action The current action.
	 * @param linkName The link name.
	 * @param linkedEntity The linked Entity.
	 * @param context The current context.
	 * @return The Excel file.
	 */
	public <E extends Entity> File getListExportXls(E entity, String entityName, String queryName, ListCriteria criteria, Action action,
			String linkName, Entity linkedEntity,
			RequestContext context) {
		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(entityName, context);
		DbQuery query = DB.getQuery(context, entityName, queryName);
		addOrderIntoQuery(criteria, query);
		
		context.getAttributes().put("EXPORT_TYPE", "xls");
		domainLogic.internalDbQueryPrepare(query, criteria, context);
		
		// Get output format
		if (ApplicationUtils.getApplicationLogic().useXslxFormat()) {
			// New Xslx format
			return prepareXlsxSheet(query, context);
		} else {
			// Old Xls format, check result count
			if (DB.count(query, context) > Constants.MAX_ROW_EXCEL_EXPORT) {
				throw new FunctionalException(new Message(MessageUtils.getInstance(context).getMessage("xls.export.error.max", (Object[]) null),
						Severity.ERROR));
			}
			return prepareXlsSheet(query, context);
		}
	}

	/**
	 * Prepare datas of a back ref list for export Excel.
	 * 
	 * @param <E> The Entity class
	 * @param entity The entity.
	 * @param entityName The entity name.
	 * @param linkName The link name.
	 * @param queryName The query name.
	 * @param context The current context.
	 *
	 * @return The Excel file.
	 */
	public <E extends Entity> File getBackRefListExportXls(E entity, String entityName, String linkName, String queryName, RequestContext context) {
		DbQuery query = DB.getLinkQuery(entity, linkName, queryName, false, context);
		DomainLogic domainLogic = DomainUtils.getLogic(query.getMainEntity().name(), context);

		context.getAttributes().put("EXPORT_TYPE", "xls");
		// Create search criteria
		ListCriteria criteria = new ListCriteria<>(entity, Action.getListAction(queryName, null), linkName, null);
		domainLogic.internalDbQueryPrepare(query, criteria, context);

		// Get output format
		if (ApplicationUtils.getApplicationLogic().useXslxFormat()) {
			// New Xslx format
			return prepareXlsxSheet(query, context);
		} else {
			// Old Xls format, check result count
			if (DB.count(query, context) > Constants.MAX_ROW_EXCEL_EXPORT) {
				throw new FunctionalException(new Message(MessageUtils.getInstance(context).getMessage("xls.export.error.max", (Object[]) null),
						Severity.ERROR));
			}
			return prepareXlsSheet(query, context);
		}
	}

	/**
	 * Excel export filenames follow the pattern PREFIX_xxxxxxxxxxxxxxxxxx.SUFFIX.<br/>
	 * - xxxxxxxxxxxxxxxxxx are random numbers created by File.createTempFile. <br/>
	 * - SUFFIX is .xls or .xls<br/>
	 * This method computes PREFIX. Default behavior returns query.getName(). If RequestContext contains a attachmentName longer than 3
	 * characters, this will override default behavior and use it instead.
	 * 
	 * @param query Query to export
	 * @param ctx Current RequestContext
	 * @return PREFIX name to use in DbQuery export
	 */
	private String getExportPrefix(DbQuery query, RequestContext ctx) {
		String prefix = query.getName();
		if (ctx.getAttachmentName() != null && ctx.getAttachmentName().length() > 3) {
			prefix = ctx.getAttachmentName();
		}
		return prefix;
	}

	/**
	 * Prepare an excel file (xls format).
	 * 
	 * @param query The input query.
	 * @param ctx The current context.
	 * @return The excel file.
	 */
	public File prepareXlsSheet(DbQuery query, RequestContext ctx) {
		File excelFile = null;
		try {
			excelFile = File.createTempFile(getExportPrefix(query, ctx) + "_", ".xls");
			new XlsWriter(ctx, query).export(excelFile);
		} catch (FunctionalException|TechnicalException ex) {
			// Let the caller handle Functional or Technical Exception display
			throw ex;
		} catch (Exception e) {
			// Wrap any other Exception in a Technical Exception and let the caller handle this
			throw new TechnicalException(e.getMessage(), e);
		}
		return excelFile;
	}

	/**
	 * Prepare an excel file (xlsx format).
	 * 
	 * @param query The input query.
	 * @param ctx The current context.
	 * @return The excel file.
	 */
	public File prepareXlsxSheet(DbQuery query, RequestContext ctx) {
		File excelFile = null;
		try {
			excelFile = File.createTempFile(getExportPrefix(query, ctx) + "_", ".xlsx");
			new XlsxWriter(ctx, query).export(excelFile);
		} catch (FunctionalException|TechnicalException ex) {
			// Let the caller handle Functional or Technical Exception display
			throw ex;
		} catch (Exception e) {
			// Wrap any other Exception in a Technical Exception and let the caller handle this
			throw new TechnicalException(e.getMessage(), e);
		}
		return excelFile;
	}

	/**
	 * Computes the action rights (visibility) for an entity.
	 * 
	 * @param entityName Entity name.
	 * @param linkedEntity Linked entity.
	 * @param linkName Link name.
	 * @param queryName Query name.
	 * @param linkActions Attach and detach actions.
	 * @param ctx Current Context.
	 * @return A map containing a boolean value for each action.
	 * @see AbstractSecurityManager#isActionRendered(String, String, RequestContext)
	 * @see DomainLogic#internalUiActionIsRendered(Response, Action, Entity, String, String, RequestContext)
	 */
	private Map<String, Boolean> getActionRights(String entityName, Entity linkedEntity, String linkName, String queryName,
			Action[] linkActions, RequestContext ctx) {

		EntityModel model = EntityManager.getEntityModel(entityName);
		AbstractSecurityManager sm = SecurityUtils.getSecurityManager();
		Map<String, Boolean> actionRights = new HashMap<>();
		Collection<Action> actions = new ArrayList<>(model.getActions());

		if (linkActions != null) {
			for (Action action : linkActions) {
				actions.add(action);
			}
		}

		for (Action action : actions) {
			String codeAction = action.getCode();
			boolean rendered = true;

			try {
				rendered = sm.isActionRendered(entityName, codeAction, ctx);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			actionRights.put(codeAction, rendered);
		}
		return actionRights;
	}

}
