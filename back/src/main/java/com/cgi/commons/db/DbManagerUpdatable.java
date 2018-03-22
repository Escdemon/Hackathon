package com.cgi.commons.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.utils.MessageUtils;

/**
 * Evolve the DbManager for Updates.
 */
public class DbManagerUpdatable extends DbManager {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(DbManagerUpdatable.class);

	/**
	 * Constructor.
	 * 
	 * @param ctx Current Context.
	 * @param sql SQL Request to execute.
	 */
	public DbManagerUpdatable(RequestContext ctx, String sql) {
		super(ctx, sql, null, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	/**
	 * Constructor.
	 * 
	 * @param ctx Current Context.
	 * @param sql SQL Request to execute.
	 * @param parms Parameters for the SQL Request.
	 */
	public DbManagerUpdatable(RequestContext ctx, String sql, Object[] parms) {
		super(ctx, sql, parms, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	/**
	 * Constructor.
	 * 
	 * @param ctx Current Context.
	 * @param query Query to execute.
	 */
	public DbManagerUpdatable(RequestContext ctx, DbQuery query) {
		super(ctx, query, query.getBindValues().toArray(), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	/**
	 * Insert a row.
	 * 
	 * @param bean The bean used to construct the row.
	 * @throws SQLException If error.
	 */
	public void insertRow(Entity bean) throws SQLException {
		try{
			rs.moveToInsertRow();
			putToResultSet(bean, rs, true);
			rs.insertRow();
		} catch (SQLException e) {
			LOGGER.error("Error inserting row [" + sqlString + "]", e);
			throw e;
		}
	}

	
	/**
	 * Update a row.
	 * 
	 * @param bean The bean used to construct the row.
	 * @throws SQLException If error.
	 */
	public void updateRow(Entity bean) throws SQLException {
		try {
			putToResultSet(bean, rs, true);
			rs.updateRow();
		} catch (SQLException e) {
			LOGGER.error("Error updating row [" + sqlString + "]", e);
			throw e;
		}
	}

	/**
	 * Méthode conservée pour compatibilité avec les précédentes version.
	 * 
	 * @deprecated Préférer la méthode {@link #deleteRow(Entity)}.
	 * @throws SQLException If error.
	 */
	@Deprecated
	public void deleteRow() throws SQLException {
		try {
			rs.deleteRow();
		} catch (SQLException e) {
			LOGGER.error("Error deleting row [" + sqlString + "]", e);
			throw e;
		}
	}

	/**
	 * Delete a row.
	 * 
	 * @param bean Not used.
	 * @throws SQLException If error.
	 */
	public void deleteRow(Entity bean) throws SQLException {
		try {
			rs.deleteRow();
		} catch (SQLException e) {
			LOGGER.error("Error deleting row [" + sqlString + "]", e);
			throw e;
		}
	}

	/**
	 * Gestion des id : si seulement un id est du type Compteur, alors set automatique avec la nextVal.
	 * 
	 * @param entity Current entity
	 * @param ctx Current request context
	 * @return the given entity with autoincrement fields updated
	 */
	public static Entity fillAutoIncrement(Entity entity, RequestContext ctx) {
		for (String fieldName : entity.getModel().getFields()) {
			if (entity.getModel().isAutoIncrementField(fieldName) && entity.invokeGetter(fieldName) == null) {
				// Auto Increment field is not filled
				EntityModel entityModel = entity.getModel();
				// Get table name
				String tableName = entity.getModel().dbName();
				// Look for schema name
				String schemaName = null;
				String schemaId = entityModel.getDbSchemaName();
				if (!schemaId.isEmpty()) {
					schemaName = MessageUtils.getServerProperty("schema." + schemaId);
				}
				if (schemaName == null || schemaName.isEmpty()) {
					// try default schema if any
					schemaName = MessageUtils.getServerProperty("schema.default");
				}
				if (schemaName != null && !schemaName.isEmpty()) {
					tableName = schemaName + "." + tableName;
				}

				// Auto Increment field is not filled
				String selectNextValSql = "SELECT MAX(" + entityModel.getField(fieldName).getSqlName() + ") + 1 FROM " + tableName;
				DbManager dbManager = new DbManager(ctx, selectNextValSql);
				long nextVal = 1;
				if (dbManager.next()) {
					if (dbManager.getBigDecimal(1) != null) {
						nextVal = dbManager.getBigDecimal(1).longValue();
					}
					dbManager.close();
				}
				entity.invokeSetter(fieldName, nextVal);
			}
		}
		return entity;
	}


	/**
	 * Get the underlying result set
	 * 
	 * @return the current ResultSet
	 */
	public ResultSet getResultSet() {
		return rs;
	}
}
