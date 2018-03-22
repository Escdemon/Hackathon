package com.cgi.models.endpoint;

import com.cgi.business.application.SecurityManager;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.controller.BusinessController;
import com.cgi.commons.ref.controller.Request;
import com.cgi.commons.ref.data.LinkData;
import com.cgi.commons.ref.data.ListCriteria;
import com.cgi.commons.ref.data.ListData;
import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.LinkModel;
import com.cgi.commons.rest.api.error.BadRequestException;
import com.cgi.commons.rest.api.error.ForbiddenRequestException;
import com.cgi.commons.rest.api.error.NotFoundException;
import com.cgi.commons.rest.auth.WsUserMgr;
import com.cgi.commons.rest.domain.ManyProcessParameters;
import com.cgi.commons.rest.domain.RestEntity;
import com.cgi.commons.rest.domain.RestResponse;
import com.cgi.commons.rest.domain.RestUtils;
import com.cgi.commons.rest.domain.ResultList;
import com.cgi.commons.utils.FunctionalException;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.reflect.DomainUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;

/**
 * Abstract endpoint.
 */
public class AbstractEndpoint {
	/** Logger. */
	private static final Logger logger = Logger.getLogger(AbstractEndpoint.class);

	/**
	 * Verify if connected user can use an action.
	 *
	 * @param context    current context
	 * @param entityName entity name of action to verify
	 * @param action     action name to verify
	 * @throws ForbiddenRequestException when use cannot use the action
	 */
	protected void checkIsActionRendered(
			RequestContext context,
			Action action
	) throws ForbiddenRequestException {
		final SecurityManager securityManager = new SecurityManager();
		securityManager.initializeAccessRights(context);
		if (!securityManager.isActionRendered(action.getEntityName(), action.getCode(), context)) {
			throw new ForbiddenRequestException(10001L, MessageUtils.getInstance(context)
					.getMessage("rest.action.forbidden.error", action.getCode(), action.getEntityName()));
		}
	}

	/**
	 * Verify if connected user can use a query.
	 *
	 * @param context   current context
	 * @param queryName query name to verify
	 * @throws ForbiddenRequestException when use cannot use the query
	 */
	protected void checkIsListRendered(
		RequestContext context,
		String queryName
	) throws ForbiddenRequestException {
		final SecurityManager securityManager = new SecurityManager();
		securityManager.initializeAccessRights(context);
		if (!securityManager.isListRendered(queryName, context)) {
			throw new ForbiddenRequestException(10001L, MessageUtils.getInstance(context)
					.getMessage("rest.list.forbidden.error", queryName));
		}
	}

	protected <E extends Entity, R extends RestEntity, K extends Key> RestResponse launchActionLink(
			HttpServletRequest httpRequest,
			String entityName,
			ManyProcessParameters<R, K> params,
			LinkModel backRefModel,
			String linkName,
			Action action,
			E entity) {
		if (backRefModel == null) {
			throw new NotFoundException(10001L, format("Link %s not found for entity %s", linkName, entityName));
		}
		if (null == params) {
			throw new BadRequestException(10001L, format("Need body to execute action %s", action.getCode()));
		}
		if (null == params.bean) {
			throw new BadRequestException(10001L, format("Need to have source entity to execute action %s", action.getCode()));
		}
		if (null == params.keys) {
			throw new BadRequestException(10001L, format("Need to have destination entity keys to execute action %s", action.getCode()));
		}
		EntityModel entityBackRef = EntityManager.getEntityModel(backRefModel.getEntityName());
		if (!entityBackRef.isAssociative() && backRefModel.isMandatory() && action.getPersistence() == Action.Persistence.DELETE) {
			throw new BadRequestException(10001L, format("Cannot delete link %s for entity %s", linkName, entityName));
		}
		BusinessController ctrl = new BusinessController();
		// Set entity
		RestUtils.setEntity(params.bean, entity);
		List<Key> keys = (List<Key>) Arrays.asList(params.keys);

		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			Request<E> request = new Request<>(entityName, action, keys, null, backRefModel.getLinkName(), true);
			request.setLinkedEntity(entity);
			request.setEntity(entity);
			request.setContext(context);
			ctrl.validate(request);
			RestResponse restResponse = new RestResponse(context);
			return restResponse;
		} catch (FunctionalException | TechnicalException ex) {
			throw ex;
		}
	}

	/**
	 * Get list form query.
	 * @param entityName principal entity name of query
	 * @param queryName query name to launch
	 * @param orderByName name of column for order
	 * @param orderDirection ASC or DESC
	 * @param startIndex number of row to start list
	 * @param length length of list
	 * @param action
	 * @param searchCriteria string to search into all of column
	 * @param linkKey primary key of linked entity
	 * @param linkEntity name of linked entity
	 * @param linkName name of link
	 * @param httpRequest httpRequest
	 * @param context The current context
	 * @return a list of principal entity.
	 */
	protected ResultList getResultList(
			String entityName,
			String queryName,
			String orderByName,
			String orderDirection,
			int startIndex,
			int length,
			String action,
			String searchCriteria,
			String linkKey,
			String linkEntity,
			String linkName,
			HttpServletRequest httpRequest,
			RequestContext context
	) {
		checkIsListRendered(context, queryName);
		ListCriteria<Entity> criteria = new ListCriteria<>(searchCriteria);
		if (null != orderByName && null != orderDirection) {
			criteria.orderByField = orderByName;
			criteria.orderByDirection = orderDirection;
		}
		criteria.minRow = startIndex;
		criteria.maxRow = length;
		final BusinessController businessController = new BusinessController();
		final EntityModel entityQueryModel = EntityManager.getEntityModel(entityName);
		final ListData data;
		if (null == linkName) {
			// Execute query
			criteria.action = entityQueryModel.getAction(action);
			data = businessController.getListData(entityQueryModel.name(), queryName, criteria, context);
		} else {
			// Execute query with link
			final LinkModel linkModel;
			final EntityModel entityLinkModel;
			LinkModel simpleLink = entityQueryModel.getLinkModel(linkName);
			if (null == simpleLink && entityName.equals(linkEntity)) {
				// Associative backRef
				entityLinkModel = entityQueryModel;
				linkModel = entityQueryModel.getBackRefModel(linkName);
			} else if (null != simpleLink) {
				// Simple link to query
				entityLinkModel = EntityManager.getEntityModel(simpleLink.getRefEntityName());
				linkModel = entityLinkModel.getBackRefModel(linkName);
			} else {
				// BackRef link through an associative entity to query
				EntityModel linkEntityModel = EntityManager.getEntityModel(linkEntity);
				if (null == linkEntityModel) {
					throw new NotFoundException(10001L, format("linkEntity %s not found", linkEntity));
				}
				entityLinkModel = linkEntityModel;
				linkModel = linkEntityModel.getBackRefModel(linkName);
			}
			if (null == linkModel) {
				throw new NotFoundException(10001L, format("Link %s not found for entity %s", linkName, entityLinkModel.name()));
			}
			// Prepare linked entity
			Entity entity = DomainUtils.newDomain(entityLinkModel.name());
			entity.setPrimaryKey(new Key(entityLinkModel.name(), linkKey));
			// Prepare seach criteria
			criteria.action = entityLinkModel.getAction(action);
			criteria.linkName = linkName;
			criteria.linkedEntity = entity; // FIXME l'entité n'est pas complète !
			// Get list data
			data = businessController.getBackRefListData(entity, entityLinkModel.name(), linkModel.getLinkName(), queryName, criteria,
					criteria.action, null, context);
		}

		ResultList ret = new ResultList();
		ret.setResults(data.getRows());
		ret.setResultSetCount(data.getTotalRowCount());
		return ret;
	}

	/**
	 * Get entity to display into link.
	 *
	 * @param pk primary key of entity to search
	 * @param httpRequest httpRequest
	 * @param entityName entity name of entity to search
	 * @param actionStr action name
	 * @param linkName link name
	 * @return
	 */
	protected Entity getLinkData(Key pk, HttpServletRequest httpRequest, String entityName, String actionStr, String linkName) {
		EntityModel entityModel = EntityManager.getEntityModel(entityName);
		Action action = entityModel.getAction(actionStr);
		LinkModel backRef = entityModel.getBackRefModel(linkName);
		BusinessController ctrl = new BusinessController();

		// Put entity
		Entity backRefEntity = DomainUtils.newDomain(backRef.getEntityName());
		backRefEntity.setForeignKey(backRef.getLinkName(), pk);

		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			LinkData data = ctrl.getLinkData(backRefEntity, backRef.getEntityName(), backRef.getLinkName(), action, null, context);
			return data.getTargetEntity();
		}
	}

	/**
	 * To get entity by id with link.
	 *
	 * @param httpRequest      httpRequest
	 * @param entityModel      entity name of entity to search
	 * @param linkName         link name to find action
	 * @param actionName       action name of associate entity to use
	 * @param entityActionName name of entity of action (optional)
	 * @param restEntity       Rest entity
	 * @param pk               primary key
	 * @return rest entity loaded
	 */
	protected <R extends RestEntity, K extends Key> R getFromBackRef(
			HttpServletRequest httpRequest,
			EntityModel entityModel,
			String linkName,
			String actionName,
			String entityActionName,
			R restEntity,
			K pk) {
		final EntityModel entityAction;
		if (null != entityActionName) {
			entityAction = EntityManager.getEntityModel(entityActionName);
			if (entityAction == null) {
				throw new NotFoundException(10001L, format("Entity `%s` not found", entityActionName));
			}
		} else {
			entityAction = null;
		}
		final String entityName = entityModel.name();
		String otherEntityName;
		LinkModel backRefModel = entityModel.getBackRefModel(linkName);
		if (backRefModel != null) {
			otherEntityName = backRefModel.getEntityName();
		} else {
			throw new NotFoundException(10001L, format("Link `%s` not found for entity `%s`", linkName, entityName));
		}
		final BusinessController ctrl = new BusinessController();
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			EntityModel linkedEntityModel = EntityManager.getEntityModel(otherEntityName);
			final Action action;
			if (null == entityAction) {
				action = linkedEntityModel.getAction(actionName);
			} else {
				action = entityAction.getAction(actionName);
			}
			checkIsActionRendered(context, action);
			Entity entity = DomainUtils.newDomain(otherEntityName);
			entity.setForeignKey(linkName, pk);
			// Get entity
			final LinkData linkData = ctrl.getLinkData(entity, entityName, linkName, action, null, context);
			if (null == linkData || linkData.getTargetEntity() == null) {
				throw new NotFoundException(10001L, format("entity with `%s` not found", pk.toString()));
			}
			RestUtils.setRestEntity(linkData.getTargetEntity(), null, restEntity);
		} catch (FunctionalException | TechnicalException ex) {
			logger.error(format("Cannot get entity `%s` threw link `%s` with `%s`", otherEntityName, linkName, pk), ex);
			throw ex;
		}
		return restEntity;
	}

	/**
	 * Retrieves row counts for the given queries.
	 * <p>
	 * If the parameter {@code queryNames} is {@code null} or empty, all queries for the given {@code entityName} are
	 * executed.
	 * </p>
	 * 
	 * @param entityName
	 *			Entity name.
	 * @param entityQueries
	 *			Entity queries' definition.
	 * @param queryNames
	 *			Name of the queries to execute.
	 * @param httpRequest
	 *			HTTP request.
	 * @return A response with the following body :
	 * 
	 *		 <pre>
	 * {
	 *   "content": {
	 *	 "query-name-1": 123,
	 *	 "query-name-2": 456
	 *   },
	 *   "messages": [],
	 *   "downloadFilename": null
	 * }
	 *		 </pre>
	 */
	protected RestResponse getQueryCounts(String entityName, Collection<String> entityQueryNames, List<String> queryNames,
			HttpServletRequest httpRequest) {

		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			Map<String, String[]> queries = new HashMap<>();

			if (queryNames != null && !queryNames.isEmpty()) {
				queries = getQueryMap(entityName, queryNames, entityQueryNames);
			} else {
				queries = getQueryMap(entityName, entityQueryNames, entityQueryNames);
			}
			Map<String, Integer> result = new BusinessController().generateMenuCounters(queries, context);
			RestResponse restResponse = new RestResponse(result, context);
			return restResponse;
		}
	}

	/**
	 * Converts a query list into a map to call the business controller. It translates query names if necessary.
	 * 
	 * @param entityName
	 *			Entity name.
	 * @param queries
	 *			Query names.
	 * @param entityQueryNames
	 *			All query names for the given entity to check if query exists.
	 * @return A map with the following entries : (QUERY_NAME_1, [ENTITY_NAME, QUERY_NAME_1]), (QUERY_NAME_2,
	 *		 [ENTITY_NAME, QUERY_NAME_2]).
	 */
	private Map<String, String[]> getQueryMap(String entityName, Iterable<String> queries, Collection<String> entityQueryNames) {
		Map<String, String[]> result = new HashMap<>();
		String[] entityQueries;
		boolean check = entityQueryNames != queries; // All queries are processed.

		for (String name : queries) {
			entityQueries = new String[2];
			String queryName = check ? name.toUpperCase().replace("-", "_") : name;
			if (check && !entityQueryNames.contains(queryName)) {
				continue;
			}
			entityQueries[0] = entityName;
			entityQueries[1] = queryName;
			result.put(queryName, entityQueries);
		}
		return result;
	}

}
