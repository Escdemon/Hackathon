package com.cgi.commons.db;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.cgi.commons.logic.DomainLogic;
import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.data.ListCategoryData;
import com.cgi.commons.ref.data.ListData;
import com.cgi.commons.ref.data.Row;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField;
import com.cgi.commons.ref.entity.EntityField.Memory;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.ref.entity.FileContainer;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.utils.ApplicationUtils;
import com.cgi.commons.utils.DbException;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.TmpFileManager;
import com.cgi.commons.utils.reflect.DomainUtils;

/**
 * Utility class used to query database.
 * 
 * During construction of the object, the query is executed and the result set is initialized.
 *
 * This class is not thread-safe and therefore must not be shared between threads or put into static attributes.
 */
public class DbManager implements Closeable {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(DbManager.class);

	/** Statement to execute. */
	protected PreparedStatement ps = null;
	/** Results of a query. */
	protected ResultSet rs = null;
	/** Query to execute. */
	protected DbQuery dbQuery = null;

	/** Current context. */
	protected RequestContext ctx = null;

	/** store sql statement for error logging */
	protected String sqlString;

	/**
	 * Initialize a DbManager by executing the query and getting the result set from the database.
	 * 
	 * @param ctx Current Context.
	 * @param sql SQL Request to execute.
	 * @param parms Parameters for the SQL Request.
     * @param resultsetType a result set type; one of
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultsetConcurrency a concurrency type; one of
     *         <code>ResultSet.CONCUR_READ_ONLY</code> or
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
	 */
	protected void init(RequestContext ctx, String sql, Object[] parms, int resultsetType, int resultsetConcurrency) {

		// keep sql statement for logging
		sqlString = sql;

		/* create a PreparedStatement from the SQL query */
		try {
			ps = ctx.getDbConnection().getCnx().prepareStatement(sql, resultsetType, resultsetConcurrency);
			if (dbQuery != null) {
				ps.setFetchSize(dbQuery.getFetchSize());
			}
		} catch (SQLException e) {
			LOGGER.error("PrepareStatement failed [" + sqlString + "]", e);
			throw new DbException("Init DbManager: PrepareStatement failed [" + sqlString + "]", e);
		}

		/* apply query parameters */
		if (parms != null) {
			for (int i = 0; i < parms.length; i++) {
				try {
					if (parms[i] instanceof Timestamp) {
						ps.setTimestamp(i + 1, (Timestamp) parms[i]);

					} else if (parms[i] instanceof Time) {
						ps.setTime(i + 1, (Time) parms[i]);

					} else if (parms[i] instanceof Date) {
						ps.setDate(i + 1, new java.sql.Date(((Date) parms[i]).getTime()));

					} else {
						ps.setObject(i + 1, parms[i]);
					}
				} catch (SQLException e) {
					LOGGER.error("SetObject failed for parameter " + parms[i] + " [" + sqlString + "]", e);
					throw new DbException("Init DbManager: SetObject failed.", e);
				}
			}
		}

		/* Execution of the prepared statement and getting the result set */
		try {
			ps.execute();
			// Increment query count
			ctx.incQueryNbRequests();
		} catch (SQLException e) {
			LOGGER.error("Execute failed [" + sqlString + "]", e);
			throw new DbException("Init DbManager: execute failed [" + sqlString + "]", e);
		}
		try {
			rs = ps.getResultSet();
		} catch (SQLException e) {
			LOGGER.error("Get result set failed [" + sqlString + "]", e);
			throw new DbException("Init DbManager: Get ResultSet failed.", e);
		}
	}

	/**
	 * Internal constructor with every parameter.
	 * 
	 * @param ctx Current Context.
	 * @param sql SQL Request to execute.
	 * @param parms Parameters for the SQL Request.
     * @param resultsetType a result set type; one of
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultsetConcurrency a concurrency type; one of
     *         <code>ResultSet.CONCUR_READ_ONLY</code> or
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
	 */
	DbManager(RequestContext ctx, String sql, Object[] parms, int resultsetType, int resultsetConcurrency) {
		init(ctx, sql, parms, resultsetType, resultsetConcurrency);
	}

	/**
	 * Default : result set is forward only, no parameters.
	 * 
	 * @param ctx Current Context.
	 * @param sql SQL Request to execute.
	 */
	protected DbManager(RequestContext ctx, String sql) {
		this(ctx, sql, null, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	/** 
	 * Default : result set is forward only.
	 * 
	 * @param ctx Current Context.
	 * @param sql SQL Request to execute.
	 * @param parms Parameters for the SQL Request.
	 */
	protected DbManager(RequestContext ctx, String sql, Object[] parms) {
		this(ctx, sql, parms, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * Full public constructor : initialize a DbManager with a query and a few parameters. Use it if you know what you are doing.
	 * 
	 * @param ctx Current Context.
	 * @param query Query to execute.
	 * @param parms Parameters for the SQL Request.
     * @param resultsetType a result set type; one of
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>,
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultsetConcurrency a concurrency type; one of
     *         <code>ResultSet.CONCUR_READ_ONLY</code> or
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
	 */
	public DbManager(RequestContext ctx, DbQuery query, Object[] parms, int resultsetType, int resultsetConcurrency) {
		this.dbQuery = query;
		this.ctx = ctx;
		Object[] params = parms;
		if (query.getMainEntity() != null) {
			if (!query.secured) {
				DomainLogic<? extends Entity> logic = DomainUtils.getLogic(query.getMainEntity(), ctx);
				logic.internalDbSecure(query, ctx);
				query.secured = true;
			}
			String sqlString = ApplicationUtils.getApplicationLogic().getSqlBuilder(query).toSql();
			params = query.getBindValues().toArray();
			init(ctx, sqlString, params, resultsetType, resultsetConcurrency);
		}
	}

	/**
	 * Commodity public constructor, with default value for the result set parameters.
	 * 
	 * @param ctx Current Context.
	 * @param query Query to execute.
	 */
	public DbManager(RequestContext ctx, DbQuery query) {
		this(ctx, query, query.getBindValues().toArray(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * Indicates if the query still contains results.
	 * 
	 * @return {@code true} if the query still contains results, {@code false} otherwise.
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public boolean next() throws DbException {
		try {
			boolean results = rs.next();
			if (!results) {
				close();
			}
			return results;
		} catch (SQLException e) {
			LOGGER.error("Get result set failed [" + sqlString + "]", e);
			throw new DbException("Get ResultSet failed.", e);
		}
	}

	/**
	 * Gets a domain object from the current position of the result set.
	 * 
	 * @param <E> Entity class
	 * @param tableAlias
	 *            Alias which represents the given entity in query executed by this dbManager.
	 * @param bean
	 *            Domain object to retrieve.
	 * @param ctx
	 *            Current RequestContext.
	 * @return An entity with properties filled by the result of the query executed by this dbManager..
	 */
	@SuppressWarnings({ "unchecked" })
	public <E extends Entity> E getEntity(String tableAlias, Entity bean, RequestContext ctx) {
		E e = (E) getFromResultSet(bean, tableAlias);

		/* setting calculated values */
		DomainLogic<E> custom = (DomainLogic<E>) DomainUtils.getLogic(e.name(), ctx);
		List<String> transientFields = e.getTransientFields();
		e.dump();
		for (String transientFieldName : transientFields) {
			EntityField ef = e.getModel().getField(transientFieldName);
			if (ef.getMemory() != Memory.NEVER && !ef.isFromDatabase()) {
				Object customValue = custom.internalDoVarValue(e, transientFieldName, ctx);
				if (customValue != null) {
					e.invokeSetter(transientFieldName, customValue);
				}
			}
		}

		return e;
	}

	/**
	 * Gets the column alias created by the query to identify in a unique way the selected value. This alias can be passed to the getIndex()
	 * method in order to get the resultSet index.
	 * 
	 * @param entityName
	 *            Entity (=table) name
	 * @param columnName
	 *            Variable (=column) name
	 * @return the alias used by the query to identify the column selected. The alias can be hashed if it's longer than 30 characters.
	 */
	public String getColumnAlias(String entityName, String columnName) {
		if (dbQuery == null)
			throw new IllegalStateException("Illegal use of getColumnAlias method on a DbManager opened with a native SQL statement.");
		else
			return dbQuery.getColumnAlias(entityName, columnName);
	}

	/**
	 * Returns the index of the variable name.
	 * 
	 * @param entityName Entity (=table) name
	 * @param varName Variable (=column) name
	 * @return The index of alias name in the query. This should be used to get data from result set instead of accessing RS via names.
	 */
	public int getColumnIndex(String entityName, String varName) {
		if (dbQuery == null) {
			throw new IllegalStateException("Illegal use of getColumnIndex method on a DbManager opened with a native SQL statement.");
		} else {
			return dbQuery.getIndex(entityName, varName);
		}
	}

	/**
	 * Returns the Boolean value of the column.
	 * 
	 * @param columnIndex column to get the value from
	 * @return Boolean value of the column
	 */
	public Boolean getBoolean(int columnIndex) {
		try {
			return rs.getBoolean(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Boolean value of the column.
	 * 
	 * @deprecated Use getBoolean(int columnIndex) instead. Get index with getColumnIndex method.
	 * @param columnName Name of the column
	 * @return Boolean value of the column
	 */
	@Deprecated
	public Boolean getBoolean(String columnName) {
		try {
			return rs.getBoolean(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the String value of the column.
	 * 
	 * @param columnIndex column to get the value from
	 * @return String value of the column
	 */
	public String getString(int columnIndex) {
		try {
			return rs.getString(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the String value of the column.
	 * 
	 * @deprecated Use getString(int columnIndex) instead. Get index with getColumnIndex method.
	 * @param columnName Name of the column
	 * @return String value of the column
	 */
	@Deprecated
	public String getString(String columnName) {
		try {
			return rs.getString(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the getBigDecimal value of the column.
	 * 
	 * @param columnIndex column to get the value from
	 * @return getBigDecimal value of the column
	 */
	public BigDecimal getBigDecimal(int columnIndex) {
		try {
			return rs.getBigDecimal(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the BigDecimal value of the column.
	 * 
	 * @deprecated Use getBigDecimal(int columnIndex) instead. Get index with getColumnIndex method.
	 * @param columnName Name of the column
	 * @return BigDecimal value of the column
	 */
	@Deprecated
	public BigDecimal getBigDecimal(String columnName) {
		try {
			return rs.getBigDecimal(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the String value of the column. If value is null, returns "".
	 * 
	 * @param columnIndex column to get the value from
	 * @return String value of the column
	 */
	public String getStringEmpty(int columnIndex) {
		String ret = getString(columnIndex);
		return (ret == null) ? "" : ret;
	}

	/**
	 * Returns the String value of the column. If value is null, returns "".
	 * 
	 * @deprecated Use getStringEmpty(int columnIndex) instead. Get index with getColumnIndex method.
	 * @param columnName Name of the column
	 * @return String value of the column
	 */
	@Deprecated
	public String getStringEmpty(String columnName) {
		String ret = getString(columnName);
		return (ret == null) ? "" : ret;
	}

	/**
	 * Returns the Date value of the column.
	 * 
	 * @param columnIndex column to get the value from
	 * @return Date value of the column
	 */
	public Date getDate(int columnIndex) {
		try {
			return rs.getDate(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Date value of the column.
	 * 
	 * @deprecated Use getDate(int columnIndex) instead. Get index with getColumnIndex method.
	 * @param columnName Name of the column
	 * @return Date value of the column
	 */
	@Deprecated
	public Date getDate(String columnName) {
		try {
			return rs.getDate(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Time value of the column.
	 * 
	 * @param columnIndex column to get the value from
	 * @return Time value of the column
	 */
	public Time getTime(int columnIndex) {
		try {
			return rs.getTime(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Time value of the column.
	 * 
	 * @deprecated Use getTime(int columnIndex) instead. Get index with getColumnIndex method.
	 * @param columnName Name of the column
	 * @return Time value of the column
	 */
	@Deprecated
	public Time getTime(String columnName) {
		try {
			return rs.getTime(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Timestamp value of the column.
	 * 
	 * @param columnIndex column to get the value from
	 * @return Timestamp value of the column
	 */
	public Timestamp getTimestamp(int columnIndex) {
		try {
			return rs.getTimestamp(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Timestamp value of the column.
	 * 
	 * @deprecated Use getDate(int columnIndex) instead. Get index with getColumnIndex method.
	 * @param columnName Name of the column
	 * @return Timestamp value of the column
	 */
	@Deprecated
	public Timestamp getTimestamp(String columnName) {
		try {
			return rs.getTimestamp(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Integer value of the column.
	 * 
	 * @param columnIndex column to get the value from
	 * @return Integer value of the column
	 */
	public Integer getInteger(int columnIndex) {
		try {
			int result = rs.getInt(columnIndex);
			if (rs.wasNull()) {
				return null;
			} else {
				return result;
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the Integer value of the column.
	 * 
	 * @param columnName
	 *            Name of the column
	 * @return Integer value of the column
	 */
	public Integer getInteger(String columnName) {
		try {
			Integer result = rs.getInt(columnName);
			if (rs.wasNull()) {
				return null;
			} else {
				return result;
			}
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Returns the int value of the column. If value is null, returns 0.
	 * 
	 * @param columnIndex column to get the value from
	 * @return int value of the column
	 */
	public int getInt(int columnIndex) {
		try {
			return rs.getInt(columnIndex);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * Returns the int value of the column. If value is null, returns 0.
	 * 
	 * @deprecated Use getInt(int columnIndex) instead. Get index with getColumnIndex method.
	 * @param columnName Name of the column
	 * @return int value of the column
	 */
	@Deprecated
	public int getInt(String columnName) {
		try {
			return rs.getInt(columnName);
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return 0;
		}
	}

	/**
	 * Retrieve a byte array from the current resultSet.
	 * 
	 * @param columnIndex Index of the column to retrieve.
	 * @return A byte array or {@code null}.
	 */
	public byte[] getBlob(int columnIndex) {
		try {
			Blob blob = rs.getBlob(columnIndex);
			return (null != blob) ? blob.getBytes(1, (int) blob.length()) : null;
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Retrieve a byte array from the current resultSet.
	 * 
	 * @deprecated Use {@link #getBlob(int)} instead. Get index with {@link #getColumnIndex(String, String)} method.
	 * @param columnName Name of the column to retrieve.
	 * @return A byte array or {@code null}.
	 */
	@Deprecated
	public byte[] getBlob(String columnName) {
		try {
			Blob blob = rs.getBlob(columnName);
			return (null != blob) ? blob.getBytes(1, (int) blob.length()) : null;
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Retrieve a character sequence from the current resultSet.
	 * 
	 * @param columnIndex Index of the column to retrieve.
	 * @return A string or {@code null}.
	 */
	public String getClob(int columnIndex) {
		try {
			Clob clob = rs.getClob(columnIndex);
			return (null != clob) ? clob.getSubString(1, (int) clob.length()) : null;

		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Retrieve a character sequence from the current resultSet.
	 * 
	 * @deprecated Use {@link #getClob(int)} instead. Get index with {@link #getColumnIndex(String, String)} method.
	 * @param columnName Name of the column to retrieve.
	 * @return A string or {@code null}.
	 */
	@Deprecated
	public String getClob(String columnName) {
		try {
			Clob clob = rs.getClob(columnName);
			return (null != clob) ? clob.getSubString(1, (int) clob.length()) : null;
		} catch (SQLException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Closes resources (result, query) used by this dbManager.
	 * 
	 * @throws DbException
	 *             Exception thrown if an error occurs.
	 */
	public void close() throws DbException {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			throw new DbException("Error closing resultSet", e);
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					throw new DbException("Error closing preparedStatement", e);
				}
			}
		}
	}

	/**
	 * Generate the Data List from the results of the query.
	 * 
	 * @param <E> Entity class.
	 * @return Data List.
	 * @throws DbException If error.
	 */
	@SuppressWarnings("unchecked")
	public <E extends Entity> ListData getListData() throws DbException {
		if (dbQuery == null)
			throw new IllegalStateException("Illegal use of getListData method on a DbManager opened with a native SQL statement.");

		ListData data = new ListData(dbQuery.getMainEntity().name());
		if (dbQuery.getCategoryBreak().size() > 0) {
			data = new ListCategoryData(dbQuery.getMainEntity().name());
			((ListCategoryData) data).setCategoryBreak(dbQuery.getCategoryBreak());
		}

		DomainLogic<E> domainLogic = (DomainLogic<E>) DomainUtils.getLogic(dbQuery.getMainEntity(), ctx);

		int rownum = dbQuery.getMinRownum();
		try {
			while (next()) {
				Row r = getNextRow(domainLogic);
				r.put(Constants.RESULT_ROWNUM, rownum++);
				data.add(r);
			}
		} catch (SQLException ex) {
			throw new DbException(ex.getMessage(), ex);
		} finally {
			close();
		}
		return data;
	}

	/**
	 * Get the next Row from the database results.<br/>
	 * <b>WARNING</b> : This may be a bad idea to access the resultset with your own code.<br/>
	 * <b>WARNING</b> : The date(-like) fields will NOT be formated.<br/>
	 * Check other available methods before using this one !
	 * 
	 * @param <E> Entity class.
	 * @param domainLogic Domain Logic.
	 * @return The next Row.
	 * @throws SQLException If error.
	 */
	public <E extends Entity> Row getNextRow(DomainLogic<E> domainLogic) throws SQLException {
		return getNextRow(domainLogic, null, null, null);
	}

	/**
	 * Get the next Row from the database results.<br/>
	 * <b>WARNING</b> : This may be a bad idea to access the resultset with your own code.<br/>
	 * Check other available methods before using this one !
	 * 
	 * @param <E> Entity class.
	 * @param domainLogic Domain Logic.
	 * @param dateFormatter formatter for Date.
	 * @param timeFormatter formatter for Time.
	 * @param timestampFormatter formatter for Timestamp.
	 * @return The next Row.
	 * @throws SQLException If error.
	 */
	public <E extends Entity> Row getNextRow(DomainLogic<E> domainLogic, DateFormat dateFormatter,
			DateFormat timeFormatter, DateFormat timestampFormatter)
			throws SQLException {

		if (dbQuery == null)
			throw new IllegalStateException("Illegal use of getNextRow method on a DbManager opened with a native SQL statement.");

		Row row = new Row();

		// Pour chaque ligne du result set, on construit la PK
		Key pk = dbQuery.getMainEntity().getPrimaryKey();
		pk.nullify();

		// Pour chaque variable attendue, on récupère le nom et le type SQL.
		Locale locale = ctx.getUser().getLocale();
		for (DbQuery.Var var : dbQuery.getOutVars()) {
			if (!var.model.isFromDatabase()) {
				continue;
			}

			/* Get the rsResult (object in the resultset) and the rsResultDisplay (object converted to a displayable shape, most likely a String) */
			Object rsResult = null;
			Object rsResultDisplay = null;
			String sqlName = var.tableId + "_" + var.model.getSqlName();
			int index = dbQuery.getIndex(sqlName);
			try {
				if (var.model.getSqlType() == SqlTypes.INTEGER) {
					rsResult = rs.getInt(index);
					if (rs.wasNull()) {
						rsResult = null;
					}
				} else if (var.model.getSqlType() == SqlTypes.BOOLEAN) {
					rsResult = rs.getBoolean(index);
				} else if (var.model.getSqlType() == SqlTypes.VARCHAR2) {
					rsResult = rs.getString(index);
				} else if (var.model.getSqlType() == SqlTypes.CHAR) {
					rsResult = rs.getString(index);
				} else if (var.model.getSqlType() == SqlTypes.DATE) {
					rsResult = rs.getDate(index);
					if (rsResult != null && dateFormatter != null) {
						rsResultDisplay = dateFormatter.format(rsResult);
					}
				} else if (var.model.getSqlType() == SqlTypes.TIME) {
					rsResult = rs.getTime(index);
					if (rsResult != null && timeFormatter != null) {
						rsResultDisplay = timeFormatter.format(rsResult);
					}
				} else if (var.model.getSqlType() == SqlTypes.TIMESTAMP) {
					try {
						rsResult = rs.getTimestamp(index);
					} catch (SQLException ex) {
						if (DbConnection.getDbType() == DbConnection.Type.MySQL && "S1009".equals(ex.getSQLState())) {
							// MySQL handles null timestamp in a very strange way.
							// This can be avoided by setting zeroDateTimeBehavior=convertToNull in jdbc connection string.
							// If the parameter is not set, it may break.
							rsResult = null;
						} else {
							throw ex;
						}
					}
					if (rsResult != null && timestampFormatter != null) {
						rsResultDisplay = timestampFormatter.format(rsResult);
					}
				} else if (var.model.getSqlType() == SqlTypes.DECIMAL) {
					rsResult = rs.getBigDecimal(index);
					if (rsResult != null && var.model.getSqlAccuracy() == 0) {
						// convert BigDecimal to Long
						rsResult = ((BigDecimal) rsResult).longValueExact();
					}
				} else {
					LOGGER.warn("Type non géré !!! : " + var.model.getSqlType());
				}

				// Check for defined values field
				if (var.model.hasDefinedValues()) {
					// For dates, this will override formated value
					rsResultDisplay = var.model.getDefinedLabel(rsResult, locale);
				}
			} catch (SQLException ex) {
				throw new TechnicalException("Erreur à la récupération du champ " + sqlName, ex);
			}

			// Compute display value
			if (rsResultDisplay == null) {
				/* only case where rsResultDisplay is not a String */
				rsResultDisplay = rsResult;
			}

			// Allow custom value formating
			rsResultDisplay = domainLogic.dbQueryFormatValue(dbQuery, dbQuery.getEntity(var.tableId), var.name, rsResult, rsResultDisplay, ctx);

			if (dbQuery.getMainEntityAlias().equals(var.tableId)
					&& dbQuery.getMainEntity().getModel().getKeyModel().getFields().contains(var.name)) {
				pk.setValue(var.name, rsResult);
			}

			row.put(var.getColumnAlias(), rsResultDisplay);
			if (var.model.hasDefinedValues()) {
				// Return DB value in addition of the label
				row.put("Val_" + var.getColumnAlias(), rsResult);
			}
		}

		String queryName = dbQuery.getName();
		/* manage calculated values */
		for (DbQuery.Var var : dbQuery.getOutVars()) {
			if (!var.model.isFromDatabase() && var.model.getMemory() != Memory.NEVER) {
				Object uiVarValue = domainLogic.internalDbQueryVarValue(row, queryName, dbQuery.getEntity(var.tableId), var.name, ctx);
				row.put(var.tableId + "_" + var.name, uiVarValue);
			}
		}
		row.put(Constants.RESULT_PK, pk);
		return row;
	}

	/**
	 * Returns the number of records in database corresponding to the query executed by this dbManager.
	 * 
	 * @return the number of records in database corresponding to the query executed by this dbManager.
	 * @exception DbException If error.
	 */
	public int count() throws DbException {
		if (dbQuery == null)
			throw new IllegalStateException("Illegal use of count method on a DbManager opened with a native SQL statement.");

		return DbManager.count(ctx, dbQuery);
	}

	/**
	 * Returns the number of records in database corresponding to the query executed by this dbManager.<br>
	 * 
	 * @param ctx
	 * @param query
	 * @return the number of records in database corresponding to the query executed by this dbManager.
	 * @throws DbException
	 */
	public static int count(RequestContext ctx, DbQuery query) throws DbException {
		query.setCount(true);
		DbManager mgr = new DbManager(ctx, query);
		int count = 0;

		try {
			if (mgr.next()) {
				// La requête est en mode "comptage", il n'y a qu'une colonne qui
				// contient 1 seul entier, le nombre de résultats.
				count = mgr.getInt(1);
			}
		} finally {
			mgr.close();
		}

		query.setCount(false);
		return count;
	}

	/**
	 * Retrieve an entity in a Result Set.
	 * 
	 * @param entity Entity to fill.
	 * @param tableAlias Alias of this entity in the result set.
	 * @return The Entity filled.
	 */
	@SuppressWarnings("deprecation")
	protected Entity getFromResultSet(Entity entity, String tableAlias) {
		for (Field field : entity.getFields()) {
			try {
				String methodName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
				Method method = entity.getClass().getDeclaredMethod(methodName, field.getType());
				String className = field.getType().getName();

				EntityField eField = entity.getModel().getField(field.getName());
				String dbName = tableAlias + "_" + eField.getSqlName();
				Integer index = (dbQuery == null ? null : dbQuery.getIndex(dbName));

				if (eField.getSqlType() == SqlTypes.BLOB || eField.getSqlType() == SqlTypes.CLOB) {
					InputStream is;
					if (eField.getSqlType() == SqlTypes.BLOB) {
						is = (index == null ? rs.getBinaryStream(dbName) : rs.getBinaryStream(index));
					} else {
						is = (index == null ? rs.getAsciiStream(dbName) : rs.getAsciiStream(index));
					}
					FileContainer container = new FileContainer();
					if (!rs.wasNull()) {
						container.setName(new TmpFileManager(is).extractName());
						IOUtils.closeQuietly(is);
					} else {
						container.setNull(true);
					}
					method.invoke(entity, container);
					continue;
				}

				Object rsResult = (index == null ? rs.getObject(dbName) : rs.getObject(index));

				if (rsResult == null || rs.wasNull()) {
					// Null values are processed here
					method.invoke(entity, (Object) null);

				} else if (String.class.getName().equals(className)) {
					method.invoke(entity, (index == null ? rs.getString(dbName) : rs.getString(index)));

				} else if (Date.class.getName().equals(className)) {
					SqlTypes sqlType = eField.getSqlType();
					if (SqlTypes.DATE.equals(sqlType)){
						Date dt = (index == null ? rs.getDate(dbName) : rs.getDate(index));
						method.invoke(entity, (dt == null ? null : new java.util.Date(dt.getTime())));
					} else if (SqlTypes.TIMESTAMP.equals(sqlType)) {
						Timestamp dt = (index == null ? rs.getTimestamp(dbName) : rs.getTimestamp(index));
						method.invoke(entity, (dt == null ? null : new java.util.Date(dt.getTime())));
					} else if (SqlTypes.TIME.equals(sqlType)) {
						Time dt = (index == null ? rs.getTime(dbName) : rs.getTime(index));
						method.invoke(entity, (dt == null ? null : new java.util.Date(dt.getTime())));
					}

				} else if (Integer.class.getName().equals(className)) {
					method.invoke(entity, (index == null ? rs.getInt(dbName) : rs.getInt(index)));

				} else if (Long.class.getName().equals(className)) {
					method.invoke(entity, (index == null ? rs.getBigDecimal(dbName) : rs.getBigDecimal(index)).longValueExact());

				} else if (Boolean.class.getName().equals(className)) {
					int bVal = (index == null ? rs.getInt(dbName) : rs.getInt(index));
					if (bVal == 1) {
						method.invoke(entity, Boolean.TRUE);
					} else {
						method.invoke(entity, Boolean.FALSE);
					}
				} else {
					method.invoke(entity, rsResult);
				}
			} catch (Exception e) {
				LOGGER.error("Building entity from resultset: error on variable " + field.getName(), e);
			}
		}
		return entity;
	}

	/**
	 * Put an entity in a Result Set.
	 * @param entity The entity.
	 * @param rs The Result Set.
	 * @param updateNullValues Indicates if null values must be updated or not.
	 */
	@SuppressWarnings("deprecation")
	protected void putToResultSet(Entity entity, ResultSet rs, boolean updateNullValues) {
		for (Field field : entity.getFields()) {
			try {
				Object result = getValue(entity, field);

				EntityField eField = entity.getModel().getField(field.getName());
				String columnName = eField.getSqlName();
				SqlTypes type = eField.getSqlType();

				if (eField.isTransient()) {
					// Skip Memory Vars from all types
					continue;
				}

				boolean isUnloadedFile = isUnloadedFile(field, type, result);
				if (result == null && updateNullValues && !isUnloadedFile) {
					rs.updateNull(columnName);
					continue;
				}

				switch (type) {
				case VARCHAR:
				case VARCHAR2:
					rs.updateString(columnName, result.toString());
					break;
				case INTEGER:
					if (result instanceof Integer) {
						rs.updateInt(columnName, (Integer) result);
					} else if (result instanceof Long) {
						rs.updateLong(columnName, (Long) result);
					}
					break;
				case DECIMAL:
					if (eField.getSqlAccuracy() > 0) {
						rs.updateBigDecimal(columnName, (BigDecimal) result);
					} else {
						rs.updateLong(columnName, (Long) result);
					}
					break;
				case BOOLEAN:
					updateBoolean(rs, result, columnName);
					break;
				case DATE:
					rs.updateDate(columnName, new java.sql.Date(((Date) result).getTime()));
					break;
				case TIME:
					rs.updateTime(columnName, new java.sql.Time(((Date) result).getTime()));
					break;
				case TIMESTAMP:
					rs.updateTimestamp(columnName, new java.sql.Timestamp(((Date) result).getTime()));
					break;
				case BLOB:
				case CLOB:
					// It seems updateBlob does not work with MySQL in an updatable resultset, see http://bugs.mysql.com/bug.php?id=53002,
					// Fortunately, updateObject is working (but it may be not efficient).
					FileContainer fc = (FileContainer) result;
					if (!isUnloadedFile) {
						rs.updateObject(columnName, fc.getContent());
					} else if (fc != null && fc.getUuid() != null) {
						// A temporary file is available, the column is set with an emtpy bytearray
						// It will be updated with the file content later.
						rs.updateObject(columnName, new byte[0]);
					}
					break;
				default:
					rs.updateObject(columnName, result);
					break;
				}

			} catch (SQLException e) {
				String msg = "Update failed for variable " + field.getName() + " in entity " + entity.name();
				LOGGER.error(msg, e);
				throw new DbException(msg, e);
			} catch (IllegalArgumentException e) {
				String msg = "Update failed for variable " + field.getName() + " in entity " + entity.name();
				LOGGER.error(msg, e);
				throw new TechnicalException(msg, e);
			}
		}
	}

	/**
	 * Indicates whether the given field is a file (BLOB or CLOB) which was not loaded.
	 * 
	 * @param field
	 *            Field to check.
	 * @param type
	 *            Field's Sql Type
	 * @param result
	 *            DB value.
	 * @return {@code true} if the type is {@value SqlTypes#BLOB} or {@value SqlTypes#CLOB} and there is a file container linked to the field
	 *         which is not {@code null}; {@code false} otherwise.
	 * @see FileContainer#isNull()
	 */
	private boolean isUnloadedFile(Field field, SqlTypes type, Object result) {
		boolean isLob = (type == SqlTypes.BLOB || type == SqlTypes.CLOB);
		if (isLob && result != null) {
			FileContainer container = (FileContainer) result;
			// BLOB data exists but has not been loaded
			return !container.isNull();
		}
		return false;
	}

	/**
	 * Update a boolean value into the database.
	 * 
	 * @param rs
	 *            Current resultSet.
	 * @param value
	 *            Value to store, it may be a Boolean or a String.
	 * @param columnName
	 *            Name of the column to update.
	 * @throws SQLException
	 *             If an exception occurs.
	 */
	private void updateBoolean(ResultSet rs, Object value, String columnName) throws SQLException {
		Boolean result = null;

		if (value instanceof String) {
			result = Boolean.valueOf((String) value);
		} else if (value instanceof Boolean) {
			result = (Boolean) value;
		}

		if (result != null) {
			if (result.booleanValue()) {
				rs.updateInt(columnName, 1);
			} else {
				rs.updateInt(columnName, 0);
			}
		} else {
			rs.updateNull(columnName);
		}
	}

	/**
	 * Get the value of a field.
	 * 
	 * @param entity The entity.
	 * @param field The field.
	 * @return An object (can be Integer, String, etc depends of the field).
	 */
	protected Object getValue(Entity entity, Field field) {
		try {
			String methodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
			Method method = entity.getClass().getMethod(methodName);
			Object result = method.invoke(entity);
			return result;
		} catch (IllegalArgumentException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		} catch (IllegalAccessException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		} catch (InvocationTargetException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		} catch (SecurityException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		} catch (NoSuchMethodException e) {
			String msg = "Unable to get value for variable " + field.getName() + " in entity " + entity.name();
			LOGGER.error(msg, e);
			throw new TechnicalException(msg, e);
		}
	}

}
