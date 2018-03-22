
package com.cgi.commons.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.cgi.commons.db.DbManager;
import com.cgi.commons.db.DbQuery;
import com.cgi.commons.db.DbQuery.Var;
import com.cgi.commons.db.DbQuery.Visibility;
import com.cgi.commons.db.FileDbManager;
import com.cgi.commons.db.SqlBuilder;
import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.controller.Request;
import com.cgi.commons.ref.controller.Response;
import com.cgi.commons.ref.data.ColumnData;
import com.cgi.commons.ref.data.ListCriteria;
import com.cgi.commons.ref.data.ListData;
import com.cgi.commons.ref.data.Message;
import com.cgi.commons.ref.data.Message.Severity;
import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Action.Input;
import com.cgi.commons.ref.entity.Action.Persistence;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityManager.SpecialValue;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.LinkModel;
import com.cgi.commons.utils.FunctionalException;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.reflect.DomainUtils;

/**
 * Domain logic for Process.
 *  
 * @param <E> Entity class.
 */
public abstract class DomainLogic<E extends Entity> {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(DomainLogic.class);

	/**
	 * Query data secure. This method will be called on every query execution.
	 * 
	 * @param query Current executed query.
	 * @param ctx Current request context.
	 */
	public final void internalDbSecure(DbQuery query, RequestContext ctx) {
		try {
			dbSecure(query, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Query data secure. This method will be called on every query execution.
	 * 
	 * @param query Current executed query.
	 * @param ctx Current request context.
	 */
	public void dbSecure(DbQuery query, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	/**
	 * Called before entity persistance.
	 * 
	 * @param bean Entity instance to be persisted.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public final void internalDbOnSave(E bean, Action action, RequestContext ctx) {
		try {
			for (String fieldName : bean.getModel().getFields()) {
				EntityField field = bean.getModel().getField(fieldName);
				if (field.getDefaultValue() != null && bean.invokeGetter(fieldName) == null) {
					Object defaultValue = field.getDefaultValue();
					if (defaultValue instanceof String && SpecialValue.from(defaultValue) != null) {
						// replace special value
						defaultValue = EntityManager.getSpecialValue(field.getSqlType(), field.getSqlAccuracy(), SpecialValue.from(defaultValue));
					}
					bean.invokeSetter(fieldName, defaultValue);
				}
			}
			dbOnSave(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Called before entity persistance.
	 * 
	 * @param bean Entity instance to be persisted.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public void dbOnSave(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	/**
	 * Called before entity removal.
	 * 
	 * @param bean Entity instance to be removed.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public final void internalDbOnDelete(E bean, Action action, RequestContext ctx) {
		try {
			dbOnDelete(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Called before entity removal.
	 * 
	 * @param bean Entity instance to be removed.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public void dbOnDelete(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	/**
	 * Called after entity loading from database.
	 * 
	 * @param bean Entity instance loaded from database.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public final void internalDbPostLoad(E bean, Action action, RequestContext ctx) {
		try {
			if (action.getInput() == Input.ONE && action.getPersistence() == Persistence.INSERT) {
				for (String fieldName : bean.getPrimaryKey().getModel().getFields()) {
					if (bean.getModel().isAutoIncrementField(fieldName)) {
						// Copy action with a auto increment field, we set it to null
						bean.invokeSetter(fieldName, null);
					}
				}
			}
			dbPostLoad(bean, action, ctx);
			bean.backup();
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Called after entity loading from database.
	 * 
	 * @param bean Entity instance loaded from database.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public void dbPostLoad(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	/**
	 * Called after entity persistance into database.
	 * 
	 * @param bean Entity instance persisted into database.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public final void internalDbPostSave(E bean, Action action, RequestContext ctx) {
		try {
			FileDbManager.saveFiles(bean, ctx);
			dbPostSave(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Called after entity persistance into database.
	 * 
	 * @param bean Entity instance persisted into database.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public void dbPostSave(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	/**
	 * Called after entity removal from database.
	 * 
	 * @param bean Entity instance removed from database.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public final void internalDbPostDelete(E bean, Action action, RequestContext ctx) {
		try {
			dbPostDelete(bean, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Called after entity removal from database.
	 * 
	 * @param bean Entity instance removed from database.
	 * @param action Current action.
	 * @param ctx Current request context.
	 */
	public void dbPostDelete(E bean, Action action, RequestContext ctx) {
		// Nothing to do on default behavior.
	}

	/**
	 * Custom action on many elements. This method is called once for all selected item.
	 * 
	 * @param request Current request.
	 * @param entity Entity to process.
	 * @param keys Keys to process.
	 * @param ctx Current request context.
	 * @return Remaining keys to process. If null, batch processing will stop after this element.
	 */
	public final List<Key> internalDoCustomAction(Request<E> request, E entity, List<Key> keys, RequestContext ctx) {
		try {
			return doCustomAction(request, entity, keys, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Custom action on one element.
	 * 
	 * @param request Current request.
	 * @param entity Entity to process.
	 * @param ctx Current request context.
	 * @return Remaining keys to process. If null, batch processing will stop after this element.
	 */
	public final List<Key> internalDoCustomAction(Request<E> request, E entity, RequestContext ctx) {
		try {
			return doCustomAction(request, entity, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Custom action on one element.
	 * 
	 * @param request Current request.
	 * @param entity Entity to process.
	 * @param ctx Current request context.
	 * @return Remaining keys to process. If null, batch processing will stop after this element.
	 */
	public List<Key> doCustomAction(Request<E> request, E entity, RequestContext ctx) {
		return null;
	}

	/**
	 * Custom action on many elements. This method is called once for all selected item.
	 * 
	 * @param request Current request.
	 * @param entity Entity to process.
	 * @param keys Keys to process.
	 * @param ctx Current request context.
	 * @return Remaining keys to process. If null, batch processing will stop after this element.
	 */
	public List<Key> doCustomAction(Request<E> request, E entity, List<Key> keys, RequestContext ctx) {
		return null;
	}

	/**
	 * Check consistency of an entity. Default behavior will call varCheck on every variable.
	 * This method returns true if the bean is invalid. Be aware that no error message will be added to the context if this method returns
	 * <code>true</code>, but the navigation will be interrupted. You should add yourself a message to the context.
	 * 
	 * @param bean Entity instance to check.
	 * @param action Action to check.
	 * @param ctx Current context.
	 * @return true if check fails.
	 */
	public final boolean internalDoCheck(E bean, Action action, RequestContext ctx) {
		boolean errors = false;
		try {
			for (String varName : bean.getModel().getFields()) {
				errors |= internalDoVarCheck(bean, action, varName, ctx);
			}
			errors |= doCheck(bean, action, ctx);
		} catch (FunctionalException ex) {
			LOGGER.error(ex);
			ctx.getMessages().addAll(ex.getMessages());
			errors = true;
		}
		return errors;
	}

	/**
	 * Check consistency of an entity. Default behavior will call varCheck on every variable.
	 * This method returns true if the bean is invalid. Be aware that no error message will be added to the context if this method returns
	 * <code>true</code>, but the navigation will be interrupted. You should add yourself a message to the context.
	 * 
	 * @param bean Entity instance to check.
	 * @param action Action to check.
	 * @param ctx Current context.
	 * @return true if check fails.
	 * @throws FunctionalException Exception thrown when entity is invalid.
	 */
	public boolean doCheck(E bean, Action action, RequestContext ctx) throws FunctionalException {
		return false;
	}

	/**
	 * Description of the current entity instance.
	 * 
	 * @param bean Entity instance (can be null).
	 * @param ctx Current request context
	 * @return String describing the entity instance. Default behavior will look for a descriptionField declared in entity, if no description
	 *         field exists, it will detail primary key fields.
	 */
	public final String internalDoDescription(E bean, RequestContext ctx) {
		try {
			return doDescription(bean, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Description of the current entity instance.<br>
	 * <u><b>Warning:</b></u> a description displayed in a list is computed by a call to the dbQueryVarValue() method.
	 * 
	 * @param bean Entity instance (can be null).
	 * @param ctx Current request context
	 * @return String describing the entity instance. Default behavior will look for a descriptionField declared in entity, if no description
	 *         field exists, it will detail primary key fields.
	 */
	public String doDescription(E bean, RequestContext ctx) {
		if (bean == null) {
			return "";
		}
		if (bean.description() != null) {
			return bean.description();
		}
		return bean.getPrimaryKey().getEncodedValue();
	}

	/**
	 * Variable check. Default behavior will check mandatory variables and datatypes.
	 * This method returns true if the variable is invalid in this specific situation. Be aware that no error message will be added to the
	 * context if this method returns <code>true</code>, but the navigation will be interrupted. You should add yourself a message to the
	 * context.
	 * 
	 * @param bean Current bean instance.
	 * @param varName Checked variable.
	 * @param action Current Action
	 * @param ctx Current request context.
	 * @return true if check fails.
	 */
	public final boolean internalDoVarCheck(E bean, Action action, String varName, RequestContext ctx) {
		boolean errors = false;
		try {
			// Check not-null fields
			if (bean.invokeGetter(varName) == null && !bean.getModel().isAutoIncrementField(varName)
					&& (bean.getModel().getField(varName).isMandatory() || internalDoVarIsMandatory(bean, varName, action, ctx))) {
				ctx.getMessages().add(new Message(MessageUtils.getInstance(ctx).getMessage(
						"uiControlerModel.fieldIsMandatory",
						new Object[] { varName }), Severity.ERROR));
				errors = true;
				addErrorFieldMarker(bean.getModel(), varName, ctx);
			}
			// Validate allowed values
			EntityField field = bean.getModel().getField(varName);
			if (field.hasDefinedValues()) {
				Object value = bean.invokeGetter(varName);
				if (!field.isDefValue(value)) {
					ctx.getMessages().add(new Message(MessageUtils.getInstance(ctx).getMessage(
							"uiControlerModel.fieldhasNotAllowedValue",
							new Object[] { varName }), Severity.ERROR));
					errors = true;
					addErrorFieldMarker(bean.getModel(), varName, ctx);
				}
			}
			if (doVarCheck(bean, varName, action, ctx)) {
				errors = true;
				addErrorFieldMarker(bean.getModel(), varName, ctx);
			}
		} catch (FunctionalException ex) {
			LOGGER.error(ex);
			ctx.getMessages().add(new Message(ex.getMessage(), Severity.ERROR));
			addErrorFieldMarker(bean.getModel(), varName, ctx);
			errors = true;
		}
		return errors;
	}

	/**
	 * Adds markers in request context so user interface will be able to highlight errors on vars and links
	 * 
	 * @param model
	 *            Entity model
	 * @param varName
	 *            name of the variable with an error
	 * @param ctx
	 *            Current request context
	 */
	private void addErrorFieldMarker(EntityModel model, String varName, RequestContext ctx) {
		ctx.addErrorField(model.name(), varName);
		for (String linkName : model.getAllLinkNames()) {
			LinkModel linkModel = model.getLinkModel(linkName);
			if (linkModel == null)
				linkModel = model.getBackRefModel(linkName);
			for (String field : linkModel.getFields()) {
				if (field.equals(varName))
					ctx.addErrorField(null, "link", linkName);
			}
		}
	}

	/**
	 * Variable check. Default behavior will check mandatory variables and datatypes.
	 * This method returns true if the variable is invalid in this specific situation. Be aware that no error message will be added to the
	 * context if this method returns <code>true</code>, but the navigation will be interrupted. You should add yourself a message to the
	 * context.
	 * 
	 * @param bean Current bean instance.
	 * @param varName Checked variable.
	 * @param action Current Action
	 * @param ctx Current request context.
	 * @return true if check fails.
	 * @throws FunctionalException If var is invalid.
	 */
	public boolean doVarCheck(E bean, String varName, Action action, RequestContext ctx) throws FunctionalException {
		return false;
	}

	/**
	 * Defines if a variable is mandatory in a specific action. Default behavior will rely on entity model definition.
	 * You do not have to add a message to explain the problem in the context, one will be added automatically.
	 * 
	 * @param bean Current entity instance.
	 * @param varName Checked variable
	 * @param action Current action
	 * @param ctx Current request context
	 * @return <code>true</code> when varName is mandatory, <code>false</code> otherwise.
	 */
	public final boolean internalDoVarIsMandatory(E bean, String varName, Action action, RequestContext ctx) {
		try {
			return doVarIsMandatory(bean, varName, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Defines if a variable is mandatory in a specific action. Default behavior will rely on entity model definition.
	 * You do not have to add a message to explain the problem in the context, one will be added automatically.
	 * 
	 * @param bean Current entity instance.
	 * @param varName Checked variable
	 * @param action Current action
	 * @param ctx Current request context
	 * @return <code>true</code> when varName is mandatory, <code>false</code> otherwise.
	 */
	public boolean doVarIsMandatory(E bean, String varName, Action action, RequestContext ctx) {
		return bean.getModel().getField(varName).isMandatory();
	}

	/**
	 * Computes a memory variable value for a domain object instance.
	 * 
	 * @param bean The current domain object instance.
	 * @param varName Calculated Variable
	 * @param ctx Current request context
	 * @return Object containing variable value.
	 */
	public final Object internalDoVarValue(E bean, String varName, RequestContext ctx) {
		try {
			return doVarValue(bean, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Computes a memory variable value for a domain object instance.
	 * 
	 * @param bean The current domain object instance.
	 * @param varName Calculated Variable
	 * @param ctx Current request context
	 * @return Object containing variable value.
	 */
	public Object doVarValue(E bean, String varName, RequestContext ctx) {
		if ("internalCaption".equals(varName)) {
			if (bean != null && bean.description() != null) {
				return bean.invokeGetter(bean.description());
			}
		}
		return null;
	}

	/**
	 * Computes a memory variable in a list. This method is called once for each row for each memory variable of the row.
	 * 
	 * @param vars All data contained in the row of the result set.
	 * @param queryName the query name
	 * @param domainName Name of the domain.
	 * @param varName Calculated Variable
	 * @param ctx Current request context
	 * @return Object containing variable value.
	 */
	public final Object internalDbQueryVarValue(Map<String, Object> vars, String queryName, String domainName, String varName, RequestContext ctx) {
		try {
			return dbQueryVarValue(vars, queryName, domainName, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Computes a memory variable in a list.<br/>
	 * <b>This method is called once for each memory variable in each row.</b>
	 * 
	 * @param vars All data contained in the row of the result set.
	 * @param queryName the query name
	 * @param domainName Name of the domain.
	 * @param varName Calculated Variable
	 * @param ctx Current request context
	 * @return Object containing variable value.
	 */
	public Object dbQueryVarValue(Map<String, Object> vars, String queryName, String domainName, String varName, RequestContext ctx) {
		return null;
	}

    /**
	 * Column caption text displayed on list / link list.
	 * 
	 * @param query Executed query.
	 * @param link Link (link list only)
	 * @param varName Variable
	 * @param ctx Current request context.
	 * @return String containing the caption displayed on the column header element of a list. Return <code>null</code> to keep default value.
	 */
	public final String internalDbQueryColumnCaption(DbQuery query, LinkModel link, String varName, RequestContext ctx) {
		try {
			return dbQueryColumnCaption(query, link, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Column caption text displayed on list / link list.<br/>
	 * <b>WARNING : Only used for Excel files</b>
	 * 
	 * @param query Executed query.
	 * @param link Link (link list only)
	 * @param varName Variable
	 * @param ctx Current request context.
	 * @return String containing the caption displayed on the column header element of a list. Return <code>null</code> to keep default value.
	 */
	public String dbQueryColumnCaption(DbQuery query, LinkModel link, String varName, RequestContext ctx) {
		Var var = query.getOutVar(varName);
		return MessageUtils.getInstance(ctx).getQryVarTitle(query.getName(), var.tableId, var.name);
	}

	/**
	 * Defines if a variable is displayed into a list.
	 * 
	 * @param query Executed query.
	 * @param link Link (link list only).
	 * @param varName Variable.
	 * @param ctx Current request context.
	 * @return {@code true} is the variable should be displayed, {@code false} otherwise.
	 */
	public final boolean internalDbQueryColumnIsVisible(DbQuery query, LinkModel link, String varName, RequestContext ctx) {
		try {
			return dbQueryColumnIsVisible(query, link, varName, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Defines if a variable is displayed into a list.<br/>
	 * <b>WARNING : Only used for Excel files</b>
	 * 
	 * @param query Executed query.
	 * @param link Link (link list only).
	 * @param varName Variable.
	 * @param ctx Current request context.
	 * @return {@code true} is the variable should be displayed, {@code false} otherwise.
	 */
	public boolean dbQueryColumnIsVisible(DbQuery query, LinkModel link, String varName, RequestContext ctx) {
		return query.getOutVar(varName).visibility == Visibility.VISIBLE;
	}

	/**
	 * Method called to load a combobox-based link.
	 * 
	 * @param bean Current entity instance. (null if list page criteria).
	 * @param link Link model between entity and comboboxed-entity.
	 * @param filterQuery Optional filter query, allow users to filter displayed data (null if not supplied).
	 * @param action Current action.
	 * @param ctx Current request context.
	 * @return Map containing primary key of each element displayed in the combobox and element descriptions.
	 */
	public final Map<Key, String> internalDoLinkLoadCombo(Entity bean, LinkModel link, DbQuery filterQuery, Action action, RequestContext ctx) {
		try {
			return doLinkLoadCombo(bean, link, filterQuery, action, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}
	
	/**
	 * Method called to load a combobox-based link.
	 * 
	 * @param bean Current entity instance. (null if list page criteria).
	 * @param linkModel Link model between entity and comboboxed-entity.
	 * @param filterQuery Optional filter query, allow users to filter displayed data (null if not supplied).
	 * @param action Current action.
	 * @param ctx Current request context.
	 * @return Map containing primary key of each element displayed in the combobox and element descriptions.
	 */
	public Map<Key, String> doLinkLoadCombo(Entity bean, LinkModel linkModel, DbQuery filterQuery, Action action, RequestContext ctx) {
		return internalDoLinkLoadValues(bean, linkModel, filterQuery, false, ctx);
	}

	/**
	 * Allows action override. This method is called before any other customizable method in the process.
	 * 
	 * @param response Current response
	 * @param ctx Current request context
	 * @return ActionPage to start instead of current action. Return null if current action should be executed.
	 */
	public final Response<?> internalDoCtrlOverrideAction(Response<E> response, RequestContext ctx) {
		try {
			return doCtrlOverrideAction(response, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Allows action override. This method is called before any other customizable method in the process.
	 * 
	 * @param response Current response
	 * @param ctx Current request context
	 * @return ActionPage to start instead of current action. Return null if current action should be executed.
	 */
	public Response<?> doCtrlOverrideAction(Response<E> response, RequestContext ctx) {
		return null;
	}

	/**
	 * Allows modifications on a query before execution.
	 * 
	 * @param query The list page query.
	 * @param criteria The list search criteria.
	 * @param ctx Current request context.
	 */
	public final void internalDbQueryPrepare(DbQuery query, ListCriteria<E> criteria, RequestContext ctx) {
		try {
			if (!dbQueryPrepare(query, criteria, ctx)) {
				if (criteria.isGlobalSearch()) {
					defaultDbQueryPrepare(query, criteria.searchCriteria, ctx);
				} else {
					defaultDbQueryPrepare(query, criteria.searchEntity, ctx);
				}
			}
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Allows modifications on a query before execution.
	 * 
	 * @param query The list page query.
	 * @param criteria Search criterias.
	 * @param ctx Current request context.
	 * 
	 * @return <code>true</code> if default query preparation should be skipped, <code>false</code> if query should still be prepared with user
	 *         input in Criteria object.
	 */
	public boolean dbQueryPrepare(DbQuery query, ListCriteria<E> criteria, RequestContext ctx) {
		return false;
	}

	/**
	 * Allows modifications on a query before execution.
	 * 
	 * @param query The list page query.
	 * @param critEntity The search entity.
	 * @param ctx Current request context.
	 */
	@SuppressWarnings("unchecked")
	private void defaultDbQueryPrepare(DbQuery query, E critEntity, RequestContext ctx) {
		if (critEntity == null) {
			// No criteria
			return;
		}
		// process simple variables
		Set<String> exactMatches = (Set<String>) critEntity.getSearchCriteria().get(Constants.SEARCH_PARAM_COMBO_EXACT_MATCH);
		if (exactMatches == null) {
			exactMatches = new HashSet<String>();
		}
		for (String fieldName : critEntity.getModel().getFields()) {
			EntityField field = critEntity.getModel().getField(fieldName);
			if (!field.isFromDatabase()) {
				// Not a SQL variable, skip it
				continue;
			}

			String alias = query.getAlias(query.getMainEntity().name());
			Object value1 = critEntity.invokeGetter(fieldName);

			if (!fieldName.endsWith("CritEnd") && critEntity.getModel().getField(fieldName + "CritEnd") != null) {

				Object value2 = critEntity.invokeGetter(fieldName + "CritEnd");
				if (value1 == null && value2 != null) {
					query.addCondLE(fieldName, alias, value2);
				} else if (value1 != null && value2 == null) {
					query.addCondGE(fieldName, alias, value1);
				} else if (value1 != null && value2 != null) {
					query.addCondBetween(fieldName, alias, value1, value2);
				}

			} else if (value1 != null) {
				String sValue = String.valueOf(value1);
				boolean exactMatch = exactMatches.contains(fieldName);
				if (SqlBuilder.STRING_SQL_TYPES.contains(field.getSqlType())
						&& !field.hasDefinedValues() && field.getSqlSize() > sValue.length() && !exactMatch) {
					if (field.getSqlSize() > (sValue.length() + 1)) {
						query.addCondLike(fieldName, alias, "%" + value1 + "%");
					} else {
						query.addCondLike(fieldName, alias, value1 + "%");
					}
				} else {
					query.addCondEq(fieldName, alias, value1);
				}
			}
		}
		for (Entry<String, Object> e : critEntity.getSearchCriteria().entrySet()) {
			if (e.getValue() instanceof String[]) {
				EntityField field = critEntity.getModel().getField(e.getKey());
				if (!field.isFromDatabase()) {
					// Not a SQL variable, skip it
					continue;
				}
				
				// Many checkbox on a criteria
				String[] values = (String[]) e.getValue();
				List<String> inValues = new ArrayList<String>();
				boolean nullValue = false;
				for (int i = 0; i < values.length; i++) {
					if ("".equals(values[i])) {
						nullValue = true;
					} else {
						inValues.add(values[i]);
					}
				}
				if (nullValue && inValues.size() > 0) {
					if (query.getConditionList().size() > 0) {
						query.and();
					}
					query.startGroupCondition();
					query.addCondInList(e.getKey(), query.getMainEntityAlias(), inValues);
					query.or();
					query.addCondIsNull(e.getKey(), query.getMainEntityAlias(), false);
					query.endGroupCondition();
				} else if (nullValue) {
					query.addCondIsNull(e.getKey(), query.getMainEntityAlias(), false);
				} else {
					query.addCondInList(e.getKey(), query.getMainEntityAlias(), inValues);
				}
			}
		}
		// process links
		for (String linkName : critEntity.getLinks().keySet()) {
			if (!critEntity.getModel().isVirtualLink(linkName) && critEntity.getLink(linkName).getKey() != null) {
				Key refPrimaryKey = critEntity.getLink(linkName).getKey();
				Key foreignKey = EntityManager.buildForeignKey(critEntity.name(), refPrimaryKey, linkName);
				query.addCondKey(foreignKey, query.getMainEntityAlias());
			}
		}
	}

	/**
	 * Allows modifications on a list page query before execution.
	 * 
	 * @param query The list page query.
	 * @param criteria Criteria Search string.
	 * @param ctx Current request context.
	 */
	private void defaultDbQueryPrepare(DbQuery query, String criteria, RequestContext ctx) {
		if (criteria == null) {
			// No criteria, nothing to do
			return;
		}
		// Force query to search without case (ie using UPPER)
		query.setCaseInsensitiveSearch(true);

		List<? extends DbQuery.Var> columns = query.getOutVars();
		List<String> colAliases = new ArrayList<String>();
		List<String> tableAliases = new ArrayList<String>();

		// Look if there is lookup fields in the query columns
		for (DbQuery.Var var : columns) {
			if (!var.model.isTransient() && var.model.isLookupField()) {
				colAliases.add(var.name);
				tableAliases.add(var.tableId);
			}
		}

		if (colAliases.isEmpty()) {
			// If not found, get all alpha query columns
			for (DbQuery.Var var : columns) {
				if (!var.model.isTransient() && var.model.isAlpha()) {
					colAliases.add(var.name);
					tableAliases.add(var.tableId);
				}
			}
		}
		
		if (!colAliases.isEmpty()) {
			// Split searched term into words
			StringTokenizer st = new StringTokenizer(criteria, " ");
			// Add each word to the query
			while (st.hasMoreTokens()) {
				String sText = st.nextToken();
				query.addCondLikeConcat(colAliases, tableAliases, sText, false);
			}
		} else {
			// Add warning if no column found
			ctx.getMessages().add(new Message(MessageUtils.getInstance(ctx).getLabel("liste.error.noLookupField", new Object[] {query.getName()}), Severity.ERROR));
		}
	}

	/**
	 * Load values for a Link.
	 * 
	 * @param bean The referenced bean.
	 * @param linkModel The link Model.
	 * @param filterQuery The filter query.
	 * @param limitSize Indicates if size is limited.
	 * @param ctx Current Context.
	 * @return A Map Key-Values.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final Map<Key, String> internalDoLinkLoadValues(Entity bean, LinkModel linkModel, DbQuery filterQuery, boolean limitSize,
			RequestContext ctx) {
		try {
			String mainAlias = "T1";
			DbQuery query = new DbQuery(linkModel.getRefEntityName(), mainAlias);

			if (filterQuery != null) {
				query = filterQuery;
				mainAlias = query.getMainEntityAlias();
				// By default all columns are selected to allow doDescription() to work.
				query.addAllColumns(mainAlias);
			}

			// Prepare partial key conditions
			Key fk = bean.getForeignKey(linkModel.getLinkName());
			if (!fk.isFull() && !fk.isNull()) {
				// Multi fields key partially filled
				query.addCondKey(fk, query.getAlias(linkModel.getRefEntityName()));
			}
		
			DomainLogic targetDomainLogic = DomainUtils.getLogic(query.getMainEntity().name(), ctx);
			ListCriteria criteria = new ListCriteria<>(bean, Action.getDummyAction(), linkModel.getLinkName(), null);
			targetDomainLogic.internalDbQueryPrepare(query, criteria, ctx);

			if (limitSize && query.getMaxRownum() <= 0) {
				query.setMaxRownum(Constants.MAX_ROW);
			}
			Map<Key, String> map = new LinkedHashMap<Key, String>();
			DbManager dbManager = null;
			// Set a request context flag so there's a difference between combo loading and list loading 
			ctx.getAttributes().put(Constants.LOAD_COMBO, query.getName());
			try {
				dbManager = new DbManager(ctx, query);
				while (dbManager.next()) {
					Entity e = DomainUtils.newDomain(linkModel.getRefEntityName());
					// Attach main bean in new instance backref in case it's needed to load customVars / do postLoad treatment
					e.getBackRef(linkModel.getLinkName()).setEntity(bean);
					e = dbManager.getEntity(mainAlias, e, ctx);
					// Loading a combo is considered as a ListAction
					Action comboAction = Action.getListAction(query.getName(), null);
					targetDomainLogic.dbPostLoad(e, comboAction, ctx);
					map.put(e.getPrimaryKey(), targetDomainLogic.internalDoDescription(e, ctx));
				}
			} finally {
				if (dbManager != null) {
					dbManager.close();
				}
			}
			// Remove the flag after combo loading
			ctx.getAttributes().remove(Constants.LOAD_COMBO);

			if (!limitSize && map.size() > Constants.MAX_ROW) {
				LOGGER.warn("Performance issue, loading combo with "+map.size()+" elements !");
			}
		
			return map;
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Called instead of query loading from database.
	 * 
	 * @param context Current Context.
	 * @param entityName Main Entity Name.
	 * @param queryName Query Name.
	 * @return Datas.
	 */
	public final ListData internalExtQueryLoad(RequestContext context, String entityName, String queryName) {
		return extQueryLoad(context, entityName, queryName);
	}

	/**
	 * Called instead of query loading from database.
	 * 
	 * @param context Current Context.
	 * @param entityName Main Entity Name.
	 * @param queryName Query Name.
	 * @return Datas.
	 */
	public ListData extQueryLoad(RequestContext context, String entityName, String queryName) {
		LOGGER.warn("Method extQueryLoad was not overrided in custom code");
		context.getMessages().add(MessageUtils.addStringErrorMessage("Method extQueryLoad was not overrided in custom code"));
		return new ListData(entityName);
	}

	/**
	 * Called instead of action page loading from database.
	 * 
	 * @param domainName Entity Name.
	 * @param primaryKey Primary key if action on one instance.
	 * @param action Action to execute.
	 * @param context Current Context.
	 * @return The Entity loaded.
	 */
	public final E internalExtActionLoad(String domainName, Key primaryKey, Action action, RequestContext context) {
		return extActionLoad(domainName, primaryKey, action, context);
	}

	/**
	 * Called instead of action page loading from database.
	 * 
	 * @param domainName Entity Name.
	 * @param primaryKey Primary key if action on one instance.
	 * @param action Action to execute.
	 * @param context Current Context.
	 * @return The Entity loaded.
	 */
	public E extActionLoad(String domainName, Key primaryKey, Action action, RequestContext context) {
		LOGGER.warn("Method extActionLoad was not overrided in custom code");
		context.getMessages().add(MessageUtils.addStringErrorMessage("Method extActionLoad was not overrided in custom code"));
		@SuppressWarnings("unchecked")
		E bean = (E) DomainUtils.newDomain(domainName);
		return bean;
	}

	/**
	 * Called after validating an action page, instead of database persistence.
	 * 
	 * @param request The Request to execute.
	 * @param context Current Context.
	 */
	public final void internalExtActionExecute(Request<E> request, RequestContext context) {
		extActionExecute(request, context);
	}

	/**
	 * Called after validating an action page, instead of database persistence.
	 * 
	 * @param request The Request to execute.
	 * @param context Current Context.
	 */
	public void extActionExecute(Request<E> request, RequestContext context) {
		LOGGER.warn("Method extActionExecute was not overrided in custom code");
		context.getMessages().add(MessageUtils.addStringErrorMessage("Method extActionExecute was not overrided in custom code"));
	}
	
	/**
	 * Allow user to override query execution by some custom code.
	 * 
	 * @param query The list page query.
	 * @param criteria The search criteria (values, action, link).
	 * @param columnsMetadata The output columns metadata.
	 * @param ctx The current context.
	 * @return The data to display in the list page.
	 */
	public final ListData internalDbLoad(DbQuery query, ListCriteria<E> criteria, Map<String, ColumnData> columnsMetadata, RequestContext ctx) {
		try {
			return dbLoad(query, criteria, columnsMetadata, ctx);
		} catch (FunctionalException fEx) {
			ctx.getMessages().addAll(fEx.getMessages()); 
			throw fEx; 
		}
	}

	/**
	 * Allow user to override query execution by some custom code.
	 * 
	 * @param query The list page query.
	 * @param criteria The search criteria (values, action, link).
	 * @param columnsMetadata The output columns metadata.
	 * @param ctx The current context.
	 * @return The data to display in the list page.
	 */
	public ListData dbLoad(DbQuery query, ListCriteria<E> criteria, Map<String, ColumnData> columnsMetadata, RequestContext ctx) {
		return null;
	}

	/**
	 * Method to allow to override displayed value of a variable.<br/>
	 * <b>This method is called once for each variable (not memory, see {@link #uiListVarValue}) in each row.</b>
	 * 
	 * @param dbQuery Current query
	 * @param entityName Entity of variable
	 * @param varName Displayed variable
	 * @param varValue Db value
	 * @param varComputedDisplay Computed displayed value
	 * @param ctx The current context.
	 * @return The value to display
	 */
	public Object dbQueryFormatValue(DbQuery dbQuery, String entityName, String varName, Object varValue, Object varComputedDisplay, RequestContext ctx) {
		return varComputedDisplay;
	}

}
