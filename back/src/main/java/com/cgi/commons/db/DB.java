package com.cgi.commons.db;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cgi.commons.db.DbQuery.Join;
import com.cgi.commons.logic.DomainLogic;
import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.data.ListCriteria;
import com.cgi.commons.ref.data.Message;
import com.cgi.commons.ref.data.Message.Severity;
import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.Link;
import com.cgi.commons.ref.entity.LinkModel;
import com.cgi.commons.utils.DbException;
import com.cgi.commons.utils.EntityNotFoundException;
import com.cgi.commons.utils.FunctionalException;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.reflect.DomainUtils;
import com.cgi.models.beans.Localisation;

/**
 * Class for Database access.
 */
public class DB {

	/** Map of Queries by Names. */
	private final static Map<String, AbstractEntityQuery> QUERIES;
	/** Map of Main Entity names by Query Names. */
	private final static Map<String, String> QUERY_MAIN_ENTITY;

	/** Indicates if DB is initialized or not. */
	private static boolean initialized = false;

	static {
		QUERIES = new HashMap<String, AbstractEntityQuery>();
		QUERY_MAIN_ENTITY = new HashMap<String, String>();
	}

	/**
	 * Initialize DB.
	 */
	private static void initialize() {
		if (DbConnection.getDbType() == null) {
			// dbType is not set yet, we'll initialize later
			return;
		}
		try {
			for (String domain : EntityManager.getDomains()) {
				String className = Constants.QUERIES_PACKAGE + "." + domain.substring(0, 1).toUpperCase() + domain.substring(1)
						+ Constants.EXTENSION_QUERY;

				AbstractEntityQuery qryClass = (AbstractEntityQuery) Class.forName(className).newInstance();
				for (String queryName : qryClass.getQueryNames()) {
					QUERY_MAIN_ENTITY.put(queryName, domain);
				}
			}
			initialized = true;
		} catch (Exception e) {
			throw new TechnicalException("Erreur d'initialisation de la DbFactory : " + e.getMessage(), e);
		}
	}

	/**
	 * Return a query thanks to the query name and the main entity name.
	 * 
	 * @param ctx
	 *            Request Context.
	 * @param entityName
	 *            The entity name.
	 * @param queryName
	 *            The query name.
	 * @return The query if finded
	 * @throws TechnicalException
	 *             if error.
	 */
	public static DbQuery getQuery(RequestContext ctx, String entityName, String queryName) throws TechnicalException {
		if (entityName == null || "".equals(entityName)) {
			return null;
		}
		if (queryName == null || "".equals(queryName)) {
			queryName = DomainUtils.createDbName(entityName);
		}
		if (QUERIES.get(entityName) == null) {
			String className = Constants.QUERIES_PACKAGE + "." + entityName.substring(0, 1).toUpperCase() + entityName.substring(1)
					+ Constants.EXTENSION_QUERY;

			try {
				QUERIES.put(entityName, (AbstractEntityQuery) Class.forName(className).newInstance());

			} catch (InstantiationException e) {
				throw new TechnicalException("Impossible d'instancier la classe de requêtes " + entityName, e);
			} catch (IllegalAccessException e) {
				throw new TechnicalException("Impossible d'instancier la classe de requêtes " + entityName, e);
			} catch (ClassNotFoundException e) {
				throw new TechnicalException("Impossible d'instancier la classe de requêtes " + entityName, e);
			}
		}
		try {
			if (QUERIES.get(entityName).getQuery(queryName) == null) {
				if (!initialized) {
					initialize();
				}
				String qMainEntity = QUERY_MAIN_ENTITY.get(queryName);
				// TODO queryName as alias ??
				return new DbQuery(qMainEntity, queryName);
			}
			return QUERIES.get(entityName).getQuery(queryName).clone();
		} catch (NullPointerException e) {
			throw new TechnicalException("La requête demandée : " + entityName + " - " + queryName + " est introuvable. ");
		}
	}

	/**
	 * Get the DbQuery for a given link.
	 * 
	 * @param e
	 *            The Entity.
	 * @param linkName
	 *            The link name.
	 * @param queryName
	 *            The query name.
	 * @param selectAll
	 *            if true, all variables are selected
	 * @param ctx
	 *            Request Context.
	 * 
	 * @return the query matching the given link.
	 */
	public static DbQuery getLinkQuery(Entity e, String linkName, String queryName, boolean selectAll, RequestContext ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.name());

		if (!eModel.getBackRefNames().contains(linkName)) {
			throw new TechnicalException("Link " + linkName + " is not a backRef of " + e.name());
		}

		LinkModel backRefModel = eModel.getBackRefModel(linkName);
		Key foreignKey = EntityManager.buildForeignKey(backRefModel.getEntityName(), e.getPrimaryKey(), linkName);
		boolean isAssociative = EntityManager.getEntityModel(backRefModel.getEntityName()).isAssociative();

		DbQuery dbQuery;
		if (isAssociative) {
			String associatedLinkName = EntityManager.getEntityModel(backRefModel.getEntityName()).getAssociatedLink(linkName);
			String associatedEntityName = EntityManager.getEntityModel(backRefModel.getEntityName()).getLinkModel(associatedLinkName)
					.getRefEntityName();
			if (queryName == null) {
				dbQuery = new DbQuery(associatedEntityName, "ASSO");
			} else {
				dbQuery = DB.getQuery(ctx, associatedEntityName, queryName);
				if (selectAll)
					dbQuery.addAllColumns(dbQuery.getAlias(associatedEntityName));
			}
		} else {
			if (queryName == null) {
				dbQuery = new DbQuery(backRefModel.getEntityName(), "ASSO");
			} else {
				dbQuery = DB.getQuery(ctx, backRefModel.getEntityName(), queryName);
				if (selectAll)
					dbQuery.addAllColumns(dbQuery.getAlias(backRefModel.getEntityName()));
			}
		}
		if (foreignKey != null && !foreignKey.isNull()) {
			if (isAssociative) {
				String associatedLinkName = EntityManager.getEntityModel(backRefModel.getEntityName()).getAssociatedLink(linkName);
				// Add join entity
				dbQuery.addEntity(backRefModel.getEntityName(), "ASSO_NN", associatedLinkName, null, Join.STRICT, false);
				// Add cond on all DB fields
				EntityModel assoModel = EntityManager.getEntityModel(backRefModel.getEntityName());
				for (String fieldName : foreignKey.getModel().getFields()) {
					if (assoModel.getField(fieldName).isFromDatabase() && foreignKey.getValue(fieldName) != null) {
						dbQuery.addCondEq(fieldName, "ASSO_NN", foreignKey.getValue(fieldName));
					}
				}
			} else {
				// Add cond on all DB fields
				EntityModel backRefEntityModel = EntityManager.getEntityModel(backRefModel.getEntityName());
				String tableAlias = dbQuery.getAlias(backRefModel.getEntityName());
				for (String fieldName : foreignKey.getModel().getFields()) {
					if (backRefEntityModel.getField(fieldName).isFromDatabase() && foreignKey.getValue(fieldName) != null) {
						dbQuery.addCondEq(fieldName, tableAlias, foreignKey.getValue(fieldName));
					}
				}
			}
		}
		return dbQuery;
	}

	/**
	 * Get the DbQuery for a given link.
	 * 
	 * @param ctx
	 *            Request Context.
	 * @param entity
	 *            The Entity.
	 * @param linkName
	 *            The link name.
	 * @param queryName
	 *            The query name.
	 * @return the query matching the given link.
	 * @throws TechnicalException
	 *             if error.
	 */
	@SuppressWarnings("unchecked")
	public static DbQuery getLinkQuery(RequestContext ctx, Entity entity, String linkName, String queryName) throws TechnicalException {
		if (entity == null) {
			return null;
		}
		DbQuery dbQuery = getLinkQuery(entity, linkName, queryName, false, ctx);
		String sourceEntityName = entity.getModel().getBackRefModel(linkName).getEntityName();

		ListCriteria<Entity> criteria = new ListCriteria<>(entity, Action.getListAction(queryName, null), linkName, null);
		((DomainLogic<Entity>) DomainUtils.getLogic(sourceEntityName, ctx)).internalDbQueryPrepare(dbQuery, criteria, ctx);
		return dbQuery;
	}

	/**
	 * Retrieves a domain object linked to the given entity.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param e
	 *            Entity linked to the domain object to retrieve.
	 * @param linkModel
	 *            Link between the given entity and the domain object to retrieve.
	 * @param ctx
	 *            The Request Context.
	 * @return The domain object linked to the given entity or {@code null} if the domain object is not found.
	 */
	public static <E extends Entity> Entity getRef(Entity e, LinkModel linkModel, RequestContext ctx) {
		return getRef(e, linkModel, Action.getDummy(), ctx);
	}

	/**
	 * Retrieves a domain object linked to the given entity.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param e
	 *            Entity linked to the domain object to retrieve.
	 * @param linkModel
	 *            Link between the given entity and the domain object to retrieve.
	 * @param action
	 *            The action.
	 * @param ctx
	 *            The Request Context.
	 * @return The domain object linked to the given entity or {@code null} if the domain object is not found.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> Entity getRef(Entity e, LinkModel linkModel, Action action, RequestContext ctx) {
		Key foreignKey = e.getForeignKey(linkModel.getLinkName());
		if (!foreignKey.isFull()) {
			return null;
		}
		Key primaryKey = new Key(linkModel.getRefEntityName());
		primaryKey.setValue(foreignKey);
		return (E) get(linkModel.getRefEntityName(), primaryKey, action, ctx);
	}

	/**
	 * Retrieves a domain object linked to the given entity.
	 * 
	 * @param e
	 *            Entity linked to the domain object to retrieve.
	 * @param linkName
	 *            Name of the link between the given entity and the domain object to retrieve.
	 * @param ctx
	 *            Current context.
	 * @return The domain object linked to the given entity or {@code null} if the domain object is not found.
	 */
	public static Entity getRef(Entity e, String linkName, RequestContext ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.name());
		if (!eModel.getLinkNames().contains(linkName)) {
			throw new DbException("Link " + linkName + " is not a link of " + e.name());
		}
		return getRef(e, eModel.getLinkModel(linkName), ctx);
	}

	/**
	 * Retrieves a domain object linked to the given entity.
	 * 
	 * @param e
	 *            Entity linked to the domain object to retrieve.
	 * @param linkName
	 *            Name of the link between the given entity and the domain object to retrieve.
	 * @param action
	 *            The action.
	 * @param ctx
	 *            Current context.
	 * @return The domain object linked to the given entity or {@code null} if the domain object is not found.
	 */
	public static Entity getRef(Entity e, String linkName, Action action, RequestContext ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.name());
		if (!eModel.getLinkNames().contains(linkName)) {
			throw new DbException("Link " + linkName + " is not a link of " + e.name());
		}
		return getRef(e, eModel.getLinkModel(linkName), action, ctx);
	}

	/**
	 * Retrieves a domain object which is a backref of the given entity.
	 * 
	 * @param e
	 *            Entity ref to the domain object to retrieve.
	 * @param backRefName
	 *            Name of the backref between the given entity and the domain object to retrieve.
	 * @param action
	 *            Current action used in dbPostLoad call.
	 * @param ctx
	 *            Current context.
	 * @return The domain object backref of the given entity or {@code null} if the domain object is not found.
	 */
	@SuppressWarnings("unchecked")
	public static Entity getUniqueBackRef(Entity e, String backRefName, Action action, RequestContext ctx) {
		EntityModel eModel = EntityManager.getEntityModel(e.name());
		if (!eModel.getBackRefNames().contains(backRefName)) {
			throw new DbException("BackRef " + backRefName + " is not a backRef of " + e.name());
		}
		if (!e.getPrimaryKey().isFull()) {
			return null;
		}
		// Call DB.getLinkedEntites without specific query
		List<Entity> entities = DB.getLinkedEntities(e, backRefName, null, action, ctx);
		if (entities.size() > 0) {
			Entity backRefEntity = entities.get(0);
			if (!ctx.isForcePostLoad()) {
				// if ctx.isForceLoad returns false, then dbPostLoad was not called inside DB.getLinkedEntities, so we need to call it from here.
				DomainLogic<Entity> backRefLogic = (DomainLogic<Entity>) DomainUtils.getLogic(backRefEntity, ctx);
				backRefLogic.internalDbPostLoad(backRefEntity, action, ctx);
			}
			return backRefEntity;
		}
		return null;
	}

	/**
	 * Retrieves a domain object by its primary key.
	 * 
	 * @param domainName
	 *            Name of the domain object to retrieve.
	 * @param primaryKey
	 *            Primary key of the domain object to retrieve.
	 * @param ctx
	 *            Current context.
	 * @return The domain object identified by the primary key or {@code null} if the domain object is not found.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	private static Entity getEntity(String domainName, Key primaryKey, RequestContext ctx) throws DbException {
		if (primaryKey == null || domainName == null) {
			throw new TechnicalException("Unable to find domain object of class " + domainName + " for primary key : " + primaryKey);
		}
		DbQuery dbQuery = new DbQuery(domainName, "T1");
		dbQuery.addCondKey(primaryKey, "T1");
		dbQuery.setFetchSize(1);
		Entity e = null;
		DbManager mgr = new DbManager(ctx, dbQuery);
		try {
			if (mgr.next()) {
				e = mgr.getEntity("T1", DomainUtils.newDomain(domainName), ctx);
			}
		} finally {
			mgr.close();
		}
		if (e != null) {
			Key initialKey = new Key(e.getPrimaryKey().getModel());
			initialKey.setValue(e.getPrimaryKey());
			e.setInitialKey(initialKey);
			e.backup();
		}
		return e;
	}

	/**
	 * Retrieves one entity from the database, given its primary key.
	 * 
	 * @param <E>
	 *            Entity class
	 * @param domainName
	 *            Name of the entity.
	 * @param primaryKey
	 *            The primary key of the entity to find.
	 * @param ctx
	 *            Current context.
	 * @return The domain object identified by the primary key or {@code null} if the domain object is not found.
	 */
	public static <E extends Entity> E get(String domainName, Key primaryKey, RequestContext ctx) {
		return get(domainName, primaryKey, Action.getDummy(), ctx);
	}

	/**
	 * Retrieves one entity from the database, given its primary key.
	 * 
	 * @param <E>
	 *            Entity class
	 * @param domainName
	 *            Name of the entity.
	 * @param primaryKey
	 *            The primary key of the entity to find.
	 * @param action
	 *            The Action.
	 * @param ctx
	 *            Current context.
	 * @return The domain object identified by the primary key or {@code null} if the domain object is not found.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> E get(String domainName, Key primaryKey, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domainName, ctx));
		E e = (E) getEntity(domainName, primaryKey, ctx);
		if (e != null) {
			logic.internalDbPostLoad(e, action, ctx);
		}
		return e;
	}

	/**
	 * Persist a domain object into database.<br>
	 * <b>WARNING : In custom methods, action persistence will always be UPDATE.</b>
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param domain
	 *            The Domain object to persist
	 * @param ctx
	 *            Current Context.
	 * @return <code>true</code> if domain object has been inserted, <code>false</code> if it has been updated.
	 */
	public static <E extends Entity> boolean persist(E domain, RequestContext ctx) {
		return persist(domain, Action.getUpdate(), ctx);
	}

	/**
	 * Persist a domain object into database.
	 * 
	 * @param domain
	 *            The Domain object to persist
	 * @param ctx
	 *            Current context
	 * @return <code>true</code> if domain object has been inserted, <code>false</code> if it has been updated.
	 * @throws DbException
	 *             if error.
	 */
	private static boolean persistEntity(Entity domain, RequestContext ctx) throws DbException {
		DbManagerUpdatable dbMgr = null;

		try {
			DbManagerUpdatable.fillAutoIncrement(domain, ctx);

			DbQuery query = new DbQuery(domain.name(), "T01");
			if (domain.getInitialKey() != null) {
				query.addCondKey(domain.getInitialKey(), "T01");
			} else {
				query.addCondKey(domain.getPrimaryKey(), "T01");
			}
			query.setForUpdate(true);

			dbMgr = new DbManagerUpdatable(ctx, query);
			if (dbMgr.rs.next()) {
				dbMgr.updateRow(domain);
				return false;
			} else {
				dbMgr.insertRow(domain);
				return true;
			}
		} catch (SQLException sqlEx) {
			throw new DbException(sqlEx.getMessage(), sqlEx);
		} finally {
			if (null != dbMgr) {
				dbMgr.close();
			}
		}
	}

	/**
	 * Persist a domain object into database.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param domain
	 *            The Domain object to persist
	 * @param action
	 *            The Action.
	 * @param ctx
	 *            Current context
	 * @return <code>true</code> if domain object has been inserted, <code>false</code> if it has been updated.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> boolean persist(E domain, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain, ctx));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(new ArrayList<Message>(ctx.getMessages()));
		}
		boolean insert = persistEntity(domain, ctx);
		logic.internalDbPostSave(domain, action, ctx);
		return insert;
	}

	/**
	 * Insert a domain object into database.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param domain
	 *            The Domain object to persist
	 * @param ctx
	 *            Current Context.
	 */
	public static <E extends Entity> void insert(E domain, RequestContext ctx) {
		insert(domain, Action.getCreate(), ctx);
	}

	/**
	 * Insert a domain object into database.
	 * 
	 * @param entity
	 *            Domain object to persist.
	 * @param ctx
	 *            Current context.
	 * @throws DbException
	 *             Exception thrown if an error occurs or if entity already exists in database
	 */
	private static void insertEntity(Entity entity, RequestContext ctx) throws DbException {
		DbManagerUpdatable dbMgr = null;

		try {
			DbManagerUpdatable.fillAutoIncrement(entity, ctx);

			DbQuery query = new DbQuery(entity.name(), "T01");
			query.addCondKey(entity.getPrimaryKey(), "T01");
			query.setForUpdate(true);

			dbMgr = new DbManagerUpdatable(ctx, query);
			dbMgr.insertRow(entity);

		} catch (SQLException sqlEx) {
			throw new DbException(sqlEx.getMessage(), sqlEx);
		} finally {
			if (null != dbMgr) {
				dbMgr.close();
			}
		}
	}

	/**
	 * Insert a domain object into database.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param domain
	 *            Domain object to persist.
	 * @param action
	 *            The Action.
	 * @param ctx
	 *            Current context.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> void insert(E domain, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain, ctx));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(new ArrayList<Message>(ctx.getMessages()));
		}
		String className = domain.getClass().getName();
		if (className.equals("com.cgi.models.beans.Localisation")) {
			Object x = ((Localisation)domain).getCoordX();
			if (x != null) {
				insertEntity(domain, ctx);
			}
		} else {
			insertEntity(domain, ctx);
		}
		logic.internalDbPostSave(domain, action, ctx);
	}

	/**
	 * Update a domain object into database. Will fail if entity does not exists in database.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param domain
	 *            Domain object to persist.
	 * @param ctx
	 *            Current context.
	 */
	public static <E extends Entity> void update(E domain, RequestContext ctx) {
		update(domain, Action.getUpdate(), ctx);
	}

	/**
	 * Update a domain object into database. Will fail if entity does not exists in database.
	 * 
	 * @param entity
	 *            Domain object to persist.
	 * @param ctx
	 *            Current context.
	 * @throws DbException
	 *             Exception thrown if an error occurs or if entity does not exists in database
	 */
	private static void updateEntity(Entity entity, RequestContext ctx) throws DbException {
		DbManagerUpdatable dbMgr = null;

		try {
			DbManagerUpdatable.fillAutoIncrement(entity, ctx);

			DbQuery query = new DbQuery(entity.name(), "T01");
			if (entity.getInitialKey() != null) {
				query.addCondKey(entity.getInitialKey(), "T01");
			} else {
				query.addCondKey(entity.getPrimaryKey(), "T01");
			}
			query.setForUpdate(true);

			dbMgr = new DbManagerUpdatable(ctx, query);
			if (dbMgr.rs.next()) {
				dbMgr.updateRow(entity);
			} else {
				// if the expected object is not found, it throws an exception
				EntityNotFoundException entityNotFoundException = new EntityNotFoundException(ctx);
				ctx.getMessages().addAll(entityNotFoundException.getMessages());
				throw entityNotFoundException;
			}
		} catch (SQLException sqlEx) {
			throw new DbException(sqlEx.getMessage(), sqlEx);
		} finally {
			if (null != dbMgr) {
				dbMgr.close();
			}
		}
	}

	/**
	 * Update a domain object into database. Will fail if entity does not exists in database.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param domain
	 *            Domain object to persist.
	 * @param action
	 *            The Action.
	 * @param ctx
	 *            Current context.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> void update(E domain, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain, ctx));
		logic.internalDbOnSave(domain, action, ctx);
		if (logic.internalDoCheck(domain, action, ctx)) {
			throw new FunctionalException(new ArrayList<Message>(ctx.getMessages()));
		}
		updateEntity(domain, ctx);
		logic.internalDbPostSave(domain, action, ctx);
	}

	/**
	 * Deletes a domain object into database.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param domain
	 *            Domain object to delete.
	 * @param ctx
	 *            Current context.
	 */
	public static <E extends Entity> void remove(E domain, RequestContext ctx) {
		remove(domain, Action.getDelete(), ctx);
	}

	/**
	 * Deletes a domain object into database.
	 * 
	 * @param entity
	 *            Domain object to delete.
	 * @param ctx
	 *            Current context.
	 * @return {@code true} if the given entity has been deleted, {@code false} otherwise.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	private static boolean removeEntity(Entity entity, RequestContext ctx) throws DbException {
		DbManagerUpdatable dbMgr = null;

		try {
			boolean removed = false;
			DbQuery query = new DbQuery(entity.name(), "T01");
			query.addCondKey(entity.getPrimaryKey(), "T01");
			query.setForUpdate(true);
			dbMgr = new DbManagerUpdatable(ctx, query);
			if (dbMgr.next()) {
				dbMgr.deleteRow(entity);
				removed = true;
			}
			return removed;
		} catch (SQLIntegrityConstraintViolationException sqlEx) {
			throw new TechnicalException(MessageUtils.getInstance(ctx)
					.getMessage("db.remove.error.constraint") + sqlEx.getMessage());
		} catch (SQLException sqlEx) {
			throw new DbException(sqlEx.getMessage(), sqlEx);
		} finally {
			if (null != dbMgr) {
				dbMgr.close();
			}
		}
	}

	/**
	 * Deletes a domain object into database.
	 * 
	 * @param <E>
	 *            Entity class.
	 * @param domain
	 *            Domain object to delete.
	 * @param action
	 *            The Action.
	 * @param ctx
	 *            Current context.
	 * @return {@code true} if the given entity has been deleted, {@code false} otherwise.
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity> boolean remove(E domain, Action action, RequestContext ctx) {
		DomainLogic<E> logic = ((DomainLogic<E>) DomainUtils.getLogic(domain, ctx));
		logic.internalDbOnDelete(domain, action, ctx);
		boolean delete = removeEntity(domain, ctx);
		logic.internalDbPostDelete(domain, action, ctx);
		return delete;
	}

	/**
	 * Return the list of linked entities.
	 * 
	 * @param entity
	 *            Source entity.
	 * @param linkName
	 *            Name of the link.
	 * @param queryName
	 *            Name of the query.
	 * @param action
	 *            The Action.
	 * @param ctx
	 *            Current context.
	 * @param action
	 *            Current action being processed. <br/>
	 *            - when ctx.forcePostLoad() returns false, this attribute is not used<br/>
	 *            - when ctx.forcePostLoad() returns true, this attribute will be passed to dbPostLoad() as current action. <br/>
	 *            - when ctx.forcePostLoad() returns true and action is null, method will use a DUMMY_ACTION constant.
	 * 
	 * @return The list of the destination entities.
	 * @throws DbException
	 *             if error.
	 */
	@SuppressWarnings({ "unchecked" })
	public static List<Entity> getLinkedEntities(Entity entity, String linkName, String queryName, Action action, RequestContext ctx)
			throws DbException {
		DbQuery dbQuery = getLinkQuery(entity, linkName, queryName, true, ctx);
		String sourceEntityName = entity.getModel().getBackRefModel(linkName).getEntityName();
		if (EntityManager.getEntityModel(sourceEntityName).isAssociative()) {
			EntityModel assoModel = EntityManager.getEntityModel(sourceEntityName);
			String associatedLinkName = assoModel.getAssociatedLink(linkName);
			sourceEntityName = assoModel.getLinkModel(associatedLinkName)
					.getRefEntityName();
		}
		DomainLogic<Entity> backRefLogic = ((DomainLogic<Entity>) DomainUtils.getLogic(sourceEntityName, ctx));
		Entity sourceEntity = DomainUtils.newDomain(sourceEntityName);
		sourceEntity.removeDefaultValues();
		ListCriteria<Entity> criteria = new ListCriteria<>(sourceEntity, action, linkName, entity);
		backRefLogic.internalDbQueryPrepare(dbQuery, criteria, ctx);

		List<Entity> list = new ArrayList<Entity>();
		DbManager dbManager = null;
		Action currentAction = action;

		if (action == null && ctx.isForcePostLoad()) {
			currentAction = Action.getDummy();
		}

		try {
			dbManager = new DbManager(ctx, dbQuery);
			while (dbManager.next()) {
				Entity backRefEntity = DomainUtils.newDomain(dbQuery.getMainEntity().name());
				backRefEntity = dbManager.getEntity(dbQuery.getMainEntityAlias(), backRefEntity, ctx);
				if (ctx.isForcePostLoad()) {
					backRefLogic.internalDbPostLoad(backRefEntity, currentAction, ctx);
				}
				list.add(backRefEntity);
			}
			return list;

		} catch (Exception e) {
			throw new DbException(e.getMessage(), e);
		} finally {
			if (null != dbManager) {
				dbManager.close();
			}
		}
	}

	/**
	 * Return the list of linked entities. This will call DB.getLinkedEntites with null action.
	 * 
	 * @param entity
	 *            Source entity.
	 * @param linkName
	 *            Name of the link.
	 * @param queryName
	 *            Name of the query.
	 * @param ctx
	 *            Current context.
	 * @return The list of the destination entities.
	 * @throws DbException
	 *             if error.
	 */
	public static List<Entity> getLinkedEntities(Entity entity, String linkName, String queryName, RequestContext ctx)
			throws DbException {
		return getLinkedEntities(entity, linkName, queryName, null, ctx);
	}

	/**
	 * Retrieves a list of domain objects linked to the given entity. This will call DB.getLinkedEntities with null queryName and null Action
	 * 
	 * @param entity
	 *            Entity linked to the domain objects to retrieve.
	 * @param linkName
	 *            Name of the link between the given entity and the domain objects to retrieve.
	 * @param ctx
	 *            Current context.
	 * @return A list of domain objects linked to the given entity or an empty list.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public static List<Entity> getLinkedEntities(Entity entity, String linkName, RequestContext ctx) throws DbException {
		return getLinkedEntities(entity, linkName, null, null, ctx);
	}

	/**
	 * Persists entities' associations into database.
	 * 
	 * @param baseBean
	 *            Domain object linked to associations to persist.
	 * @param linkName
	 *            Name of the link between {@code baseBean} and associations to persist.
	 * @param selectedKeys
	 *            Keys of the associations to persist.
	 * @param action
	 *            The Action.
	 * @param ctx
	 *            Current context.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public static void persistAssociations(Entity baseBean, String linkName, Action action, List<Key> selectedKeys, RequestContext ctx)
			throws DbException {
		Link link = baseBean.getBackRef(linkName);
		EntityModel entityModel = EntityManager.getEntityModel(link.getModel().getEntityName());
		String associatedLinkName = entityModel.getAssociatedLink(linkName);

		for (Key selectedKey : selectedKeys) {
			// For each selected element we'll create an association.
			Entity association = DomainUtils.newDomain(link.getModel().getEntityName());
			association.setForeignKey(associatedLinkName, selectedKey);
			association.setForeignKey(link.getModel().getLinkName(), baseBean.getPrimaryKey());
			association.getLink(linkName).setEntity(baseBean);
			DB.persist(association, action, ctx);
		}
	}

	/**
	 * Removes entities' associations into database.
	 * 
	 * @param baseBean
	 *            Domain object linked to associations to remove.
	 * @param linkName
	 *            Name of the link between {@code baseBean} and associations to remove.
	 * @param action
	 * 			  The action.
	 * @param selectedKeys
	 *            Keys of the associations to remove.
	 * @param ctx
	 *            Current context.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public static void removeAssociations(Entity baseBean, String linkName, Action action, List<Key> selectedKeys, RequestContext ctx) throws DbException {
		Link link = baseBean.getBackRef(linkName);
		EntityModel entityModel = EntityManager.getEntityModel(link.getModel().getEntityName());
		String associatedLinkName = entityModel.getAssociatedLink(linkName);

		for (Key selectedKey : selectedKeys) {
			// For each selected element we'll create an association.
			Entity association = DomainUtils.newDomain(link.getModel().getEntityName());
			association.setForeignKey(associatedLinkName, selectedKey);
			association.setForeignKey(link.getModel().getLinkName(), baseBean.getPrimaryKey());
			association.getLink(linkName).setEntity(baseBean);
			DB.remove(association, action, ctx);
		}
	}

	/**
	 * Returns the number of records in database corresponding to the query.
	 * 
	 * @param query
	 *            Query to use for counting
	 * @param ctx
	 *            Current requestContext containing database connection
	 * @return Number of rows returned when executing query
	 */
	public static int count(DbQuery query, RequestContext ctx) {
		DbManager manager = null;
		try {
			manager = new DbManager(ctx, query.clone());
			return manager.count();
		} finally {
			if (manager != null)
				manager.close();
		}
	}

	/**
	 * Gets a LOB content from database.
	 * 
	 * @param ctx
	 *            Current Context.
	 * @param entity
	 *            The Entity.
	 * @param propertyName
	 *            The name of the property which contains the LOB.
	 * @return The LOB in byte array.
	 */
	public static byte[] getLobContent(RequestContext ctx, Entity entity, String propertyName) {
		byte[] content = null;
		String alias = "T01";
		DbQuery query = new DbQuery(entity.name(), alias);
		query.addColumn(propertyName, alias);
		query.addCondKey(entity.getPrimaryKey(), alias);
		DbManager manager = null;
		EntityField lobField = entity.getModel().getField(propertyName);

		try {
			manager = new DbManager(ctx, query);
			if (manager.next()) {
				if (lobField.getSqlType() == SqlTypes.CLOB) {
					String clob = manager.getClob(manager.getColumnIndex(entity.name(), propertyName));
					if (null != clob) {
						content = clob.getBytes();
					}
				} else {
					content = manager.getBlob(manager.getColumnIndex(entity.name(), propertyName));
				}
			}

		} finally {
			if (manager != null)
				manager.close();
		}
		return content;
	}

	/**
	 * Gets a list of objects from a prepared query based on an entity alias. On default behavior, dbPostLoad is not called. To force call to
	 * dbPostLoad, use RequestContext.forcePostLoad(true).
	 * 
	 * @param entityName
	 *            Entities that will be returned
	 * @param entityAlias
	 *            Table alias to use to get entities
	 * @param preparedQuery
	 *            DbQuery already prepared. This method won't process query with uiListPrepare methods, but it calls "addAllColumns" method on
	 *            the requested alias
	 * @param action
	 *            Current action being processed. <br/>
	 *            - when ctx.forcePostLoad() returns false, this attribute is not used<br/>
	 *            - when ctx.forcePostLoad() returns true, this attribute will be passed to dbPostLoad() as current action. <br/>
	 *            - when ctx.forcePostLoad() returns true and action is null, method will use a DUMMY_ACTION constant.
	 * @param ctx
	 *            Current RequestContext with dabatase connection.
	 * @return List of "entityName" objects extracted from database by processing preparedQuery.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E extends Entity> List<E> getList(String entityName, String entityAlias, DbQuery preparedQuery, Action action,
			RequestContext ctx) {
		// Add all columns on entity we want to prevent problems on getEntity
		preparedQuery.addAllColumns(entityAlias);

		DomainLogic domainLogic = DomainUtils.getLogic(entityName, ctx);
		Action currentAction = action;
		if (action == null && ctx.isForcePostLoad()) {
			currentAction = Action.getDummy();
		}

		// This is a list and not a Set because there may be a sort condition on query.
		List<E> list = new ArrayList<E>();
		DbManager dbManager = null;
		try {
			dbManager = new DbManager(ctx, preparedQuery);
			while (dbManager.next()) {
				E entity = dbManager.getEntity(entityAlias, DomainUtils.newDomain(entityName), ctx);
				if (ctx.isForcePostLoad()) {
					domainLogic.internalDbPostLoad(entity, currentAction, ctx);
				}
				list.add(entity);
			}
			return list;
		} catch (Exception e) {
			throw new TechnicalException(e.getMessage(), e);
		} finally {
			if (null != dbManager) {
				dbManager.close();
			}
		}
	}
}
