package com.cgi.models.endpoint;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.cgi.commons.db.DB;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.controller.BusinessController;
import com.cgi.commons.ref.controller.Request;
import com.cgi.commons.ref.data.ListCriteria;
import com.cgi.commons.ref.data.ListData;
import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.LinkModel;
import com.cgi.commons.rest.EndpointConstants;
import com.cgi.commons.rest.api.error.NotFoundException;
import com.cgi.commons.rest.auth.WsUserMgr;
import com.cgi.commons.rest.domain.RestResponse;
import com.cgi.commons.rest.domain.RestUtils;
import com.cgi.commons.rest.domain.ResultList;
import com.cgi.models.beans.Balise;
import com.cgi.models.constants.BaliseConstants;
import com.cgi.models.queries.BaliseQuery;
import com.cgi.models.rest.BaliseRest;
import com.cgi.models.rest.BaliseRest.BaliseRestKey;
import com.cgi.commons.rest.domain.ManyProcessParameters;


/**
 * Web Service for {@link com.cgi.models.beans.Balise Balise} Entity.
 */
@Path("balise")
@Produces({ "application/json", "application/xml" })
@Consumes("application/json")
public class BaliseEndpoint extends AbstractEndpoint implements BaliseConstants, EndpointConstants {

	/** Logger. */
	private static final Logger logger = Logger.getLogger(BaliseEndpoint.class);

	/**
	 * Endpoint for action "Attacher" and link "Link to BALISE"
	 *
	 * @return view of entity thought link.
	 */
	@GET
	@Path("/id/{pk}/action/attach/localisation-r-balise")
	public RestResponse attachLocalisationRBalise(
			@Context UriInfo uriInfo,
			@PathParam("pk") BaliseRestKey pk,
			@Context HttpServletRequest httpRequest) {
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			BaliseRest restEntity = new BaliseRest();
		
			Entity linkData = getLinkData(pk, httpRequest, ENTITY_NAME, Actions.ACTION_ATTACH, Links.LINK_LOCALISATION_R_BALISE);
		
			RestUtils.setRestEntity(linkData, null, restEntity);
			RestResponse restResponse = new RestResponse(restEntity, context);
			return restResponse;
		}
	}

	/**
	 * Endpoint for action "D�tacher" and link "Link to BALISE"
	 *
	 * @return view of entity thought link.
	 */
	@GET
	@Path("/id/{pk}/action/detach/localisation-r-balise")
	public RestResponse detachLocalisationRBalise(
			@Context UriInfo uriInfo,
			@PathParam("pk") BaliseRestKey pk,
			@Context HttpServletRequest httpRequest) {
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			BaliseRest restEntity = new BaliseRest();
		
			Entity linkData = getLinkData(pk, httpRequest, ENTITY_NAME, Actions.ACTION_DETACH, Links.LINK_LOCALISATION_R_BALISE);
		
			RestUtils.setRestEntity(linkData, null, restEntity);
			RestResponse restResponse = new RestResponse(restEntity, context);
			return restResponse;
		}
	}

	/**
	 * Endpoint for action "Cr�er"
	 *
	 * @return Empty response
	 */
	@POST
	@Path("/action/create")
	public RestResponse create(
		@Context UriInfo uriInfo,
		BaliseRest rEntity,
		@Context HttpServletRequest httpRequest) {
	
		Action action = EntityManager.getEntityModel(ENTITY_NAME).getAction(Actions.ACTION_CREATE);
		BusinessController ctrl = new BusinessController();
	
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			// Put entity
			Balise entity = new Balise();
			if (rEntity != null) {
				RestUtils.setEntity(rEntity, entity);
			}
	
			Request<Balise> request = new Request<>(context, ENTITY_NAME, action, entity);
			ctrl.validate(request);
			BaliseRest restEntity = new BaliseRest();
			RestUtils.setRestEntity(entity, null, restEntity);
			RestResponse restResponse = new RestResponse(restEntity, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error("Cannot create entity", ex);
			throw ex;
		}
	}
	
	/**
	 * Endpoint for action "Cr�er"
	 *
	 * @return The {@link com.cgi.models.rest.BaliseRest Rest Entity} if found.
	 */
	@GET
	@Path("/action/create")
	public RestResponse create(
		@Context HttpServletRequest httpRequest) {
	
		Action action = EntityManager.getEntityModel(ENTITY_NAME).getAction(Actions.ACTION_CREATE);
		BusinessController ctrl = new BusinessController();
		// Get entity
		BaliseRest entity = new BaliseRest();
		final Balise e;
	
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			Request<Balise> request = new Request<>(context, ENTITY_NAME, action);
			com.cgi.commons.ref.controller.Response<?> response = ctrl.process(request);
			e = (Balise)response.getEntity();
			if (null == e) {
				throw new NotFoundException(10001L, "entity not found");
			}
			RestUtils.setRestEntity(e, null, entity);
			RestResponse restResponse = new RestResponse(entity, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error("Cannot create entity", ex);
			throw ex;
		}
	}

	/**
	 * Endpoint for action "Modifier"
	 *
	 * @return Empty response
	 */
	@PUT
	@Path("/id/{pk}/action/update")
	public RestResponse update(
		@Context UriInfo uriInfo,
		BaliseRest rEntity,
		@PathParam("pk") BaliseRestKey pk,
		@Context HttpServletRequest httpRequest) {
	
		Action action = EntityManager.getEntityModel(ENTITY_NAME).getAction(Actions.ACTION_UPDATE);
		BusinessController ctrl = new BusinessController();
	
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			// Put entity
			Balise entity = new Balise();
			// Control PK validity
			if (!pk.isFull()) {
				throw new NotFoundException(10001L, format("%s is not a valid key", pk));
			}
			if (rEntity != null) {
				RestUtils.setEntity(rEntity, entity);
				entity.setInitialKey(pk);
			} else {
				// Load entity from PK
				entity = DB.get(ENTITY_NAME, pk, context);
			}
	
			Request<Balise> request = new Request<>(context, ENTITY_NAME, action, pk, entity);
			ctrl.validate(request);
			BaliseRest restEntity = new BaliseRest();
			RestUtils.setRestEntity(entity, null, restEntity);
			RestResponse restResponse = new RestResponse(restEntity, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error(format("Cannot update entity with %s", pk), ex);
			throw ex;
		}
	}
	
	/**
	 * Endpoint for action "Modifier"
	 *
	 * @return The {@link com.cgi.models.rest.BaliseRest Rest Entity} if found.
	 */
	@GET
	@Path("/id/{pk}/action/update")
	public RestResponse update(
		@PathParam("pk") BaliseRestKey pk,
		@Context HttpServletRequest httpRequest) {
	
		Action action = EntityManager.getEntityModel(ENTITY_NAME).getAction(Actions.ACTION_UPDATE);
		BusinessController ctrl = new BusinessController();
		// Get entity
		BaliseRest entity = new BaliseRest();
		final Balise e;
	
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			// Control PK validity
			if (!pk.isFull()) {
				throw new NotFoundException(10001L, format("%s is not a valid key", pk));
			}
			Request<Balise> request = new Request<>(context, ENTITY_NAME, action, pk);
			com.cgi.commons.ref.controller.Response<?> response = ctrl.process(request);
			e = (Balise)response.getEntity();
			if (null == e) {
				throw new NotFoundException(10001L, format("entity with %s not found", pk));
			}
			RestUtils.setRestEntity(e, null, entity);
			RestResponse restResponse = new RestResponse(entity, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error(String.format("Cannot update entity with %s", pk), ex);
			throw ex;
		}
	}

	/**
	 * Endpoint for action "Afficher"
	 *
	 * @return The {@link com.cgi.models.rest.BaliseRest Rest Entity} if found.
	 */
	@GET
	@Path("/id/{pk}/action/display")
	public RestResponse display(
		@PathParam("pk") BaliseRestKey pk,
		@Context HttpServletRequest httpRequest) {
	
		Action action = EntityManager.getEntityModel(ENTITY_NAME).getAction(Actions.ACTION_DISPLAY);
		BusinessController ctrl = new BusinessController();
		// Get entity
		BaliseRest entity = new BaliseRest();
		final Balise e;
	
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			// Control PK validity
			if (!pk.isFull()) {
				throw new NotFoundException(10001L, format("%s is not a valid key", pk));
			}
			Request<Balise> request = new Request<>(context, ENTITY_NAME, action, pk);
			com.cgi.commons.ref.controller.Response<?> response = ctrl.process(request);
			e = (Balise)response.getEntity();
			if (null == e) {
				throw new NotFoundException(10001L, format("entity with %s not found", pk));
			}
			RestUtils.setRestEntity(e, null, entity);
			RestResponse restResponse = new RestResponse(entity, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error(String.format("Cannot display entity with %s", pk), ex);
			throw ex;
		}
	}

	/**
	 * Endpoint for action "Supprimer"
	 *
	 * @return Empty response
	 */
	@POST
	@Path("/id/{pk}/action/delete")
	public RestResponse delete(
		@Context UriInfo uriInfo,
		BaliseRest rEntity,
		@PathParam("pk") BaliseRestKey pk,
		@Context HttpServletRequest httpRequest) {
	
		Action action = EntityManager.getEntityModel(ENTITY_NAME).getAction(Actions.ACTION_DELETE);
		BusinessController ctrl = new BusinessController();
	
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			// Put entity
			Balise entity = new Balise();
			// Control PK validity
			if (!pk.isFull()) {
				throw new NotFoundException(10001L, format("%s is not a valid key", pk));
			}
			if (rEntity != null) {
				RestUtils.setEntity(rEntity, entity);
				entity.setInitialKey(pk);
			} else {
				// Load entity from PK
				entity = DB.get(ENTITY_NAME, pk, context);
			}
	
			Request<Balise> request = new Request<>(context, ENTITY_NAME, action, pk, entity);
			ctrl.validate(request);
			BaliseRest restEntity = new BaliseRest();
			RestUtils.setRestEntity(entity, null, restEntity);
			RestResponse restResponse = new RestResponse(restEntity, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error(format("Cannot delete entity with %s", pk), ex);
			throw ex;
		}
	}
	
	/**
	 * Endpoint for action "Supprimer"
	 *
	 * @return The {@link com.cgi.models.rest.BaliseRest Rest Entity} if found.
	 */
	@GET
	@Path("/id/{pk}/action/delete")
	public RestResponse delete(
		@PathParam("pk") BaliseRestKey pk,
		@Context HttpServletRequest httpRequest) {
	
		Action action = EntityManager.getEntityModel(ENTITY_NAME).getAction(Actions.ACTION_DELETE);
		BusinessController ctrl = new BusinessController();
		// Get entity
		BaliseRest entity = new BaliseRest();
		final Balise e;
	
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			// Control PK validity
			if (!pk.isFull()) {
				throw new NotFoundException(10001L, format("%s is not a valid key", pk));
			}
			Request<Balise> request = new Request<>(context, ENTITY_NAME, action, pk);
			com.cgi.commons.ref.controller.Response<?> response = ctrl.process(request);
			e = (Balise)response.getEntity();
			if (null == e) {
				throw new NotFoundException(10001L, format("entity with %s not found", pk));
			}
			RestUtils.setRestEntity(e, null, entity);
			RestResponse restResponse = new RestResponse(entity, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error(String.format("Cannot delete entity with %s", pk), ex);
			throw ex;
		}
	}

	/**
	 * Endpoint for action "Lister"
	 *
	 * @return List of result.
	 */
	@POST
	@Path("/action/list")
	public RestResponse list(
		BaliseRest rEntity,
		@QueryParam("order-by") String orderByName,
		@QueryParam("order-direction") String orderDirection,
		@QueryParam("start-index") @DefaultValue("0") int startIndex,
		@QueryParam("length") @DefaultValue("200") int length,
		@QueryParam("search") String searchCriteria,
		@Context HttpServletRequest httpRequest) {
	
		Action action = EntityManager.getEntityModel(ENTITY_NAME).getAction(Actions.ACTION_LIST);
		BusinessController ctrl = new BusinessController();
	
		ListCriteria<Balise> crit = new ListCriteria<>(searchCriteria, action);
		if (null != orderByName && null != orderDirection) {
			crit.orderByField = orderByName;
			crit.orderByDirection = orderDirection;
		}
		if (null == searchCriteria || "".equals(searchCriteria)) {
			// Put entity
			Balise entity = new Balise();
			RestUtils.setEntity(rEntity, entity);
			crit.searchEntity = entity;
		}
		crit.minRow = startIndex;
		crit.maxRow = length;
	
		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			checkIsActionRendered(context, action);
			checkIsListRendered(context, Query.BALISE);
			ListData data = ctrl.getListData(ENTITY_NAME, Query.BALISE, crit, context);
	
			ResultList ret = new ResultList();
			ret.setResults(data.getRows());
			ret.setResultSetCount(data.getTotalRowCount());
			RestResponse restResponse = new RestResponse(ret, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error("Cannot request entity", ex);
			throw ex;
		}
	}

	/**
	 * Endpoint for query "BALISE"<br/>
	 *
	 * @return List Balise of result.
	 */
	@GET
	@Path("/query/balise")
	public RestResponse baliseList(
		@QueryParam("order-by") String orderByName,
		@QueryParam("order-direction") String orderDirection,
		@QueryParam("start-index") @DefaultValue("0") int startIndex,
		@QueryParam("length") @DefaultValue("200") int length,
		@QueryParam("action") String action,
		@QueryParam("search") String searchCriteria,
		@QueryParam("link-key") String linkKey,
		@QueryParam("link-entity") String linkEntity,
		@QueryParam("link-name") String linkName,
		@Context HttpServletRequest httpRequest) {

		try (RequestContext context = WsUserMgr.getInstance().getRequestContext(httpRequest)) {
			ResultList resultList = getResultList(
				ENTITY_NAME,
				Query.BALISE,
				orderByName,
				orderDirection,
				startIndex,
				length,
				action,
				searchCriteria,
				linkKey,
				linkEntity,
				linkName,
				httpRequest,
				context
			);
			RestResponse restResponse = new RestResponse(resultList, context);
			return restResponse;
		} catch (Exception ex) {
			logger.error("Cannot request entity", ex);
			throw ex;
		}
	}

	/**
	 * Endpoint for retriving an Balise from its children
	 *
	 * @return The {@link com.cgi.models.rest.BaliseRest Rest Entity} if found.
	 */
	@GET
	@Path("/id/{pk}/back-ref/{link-name}")
	public BaliseRest getFromBackRef (
		@PathParam("pk") BaliseRestKey pk,
		@PathParam("link-name") String linkName,
		@QueryParam("action") String actionName,
		@QueryParam("entity-action") String actionEntityName,
		@Context HttpServletRequest httpRequest) {
		return super.getFromBackRef(
			httpRequest,
			Balise.getEntityModel(),
			linkName,
			actionName,
			actionEntityName,
			new BaliseRest(),
			pk
		);
	}

	@GET
	@Path("/query-counts")
	public RestResponse getQueryCounts(
		@QueryParam("q") List<String> queryNames,
		@Context HttpServletRequest httpRequest) {

		return super.getQueryCounts(ENTITY_NAME, BaliseQuery.QUERIES.keySet(), queryNames, httpRequest);
	}

}
