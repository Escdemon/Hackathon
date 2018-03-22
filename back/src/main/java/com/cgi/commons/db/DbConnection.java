package com.cgi.commons.db;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.cgi.commons.utils.DbException;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TechnicalException;

/**
 * Connection Object.
 */
public class DbConnection implements Closeable {
	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(DbConnection.class);

	/**
	 * Type of the connection.
	 */
	public enum Type {
		/** Oracle. */
		ORACLE, 
		/** MySQL. */
		MySQL, 
		/** PostgreSQL. */
		PostgreSQL, 
		/** DB2. */
		DB2, 
		/** SqlServer. */
		SQLSERVER
	}

	/** Path to the DataSource. */
	public static final String DATASOURCE = "datasource";
	/** The DataSource. */
	protected DataSource dataSource = null;

	/** The Connection. */
	protected Connection cnx;

	/** The type of the database. */
	private static Type dbType;

	/** Unique id of the connection (for the ConnectionLogger). */
	private int id;

	/**
	 * Constructor.
	 */
	public DbConnection() {
		try {
			String dbJndiName = MessageUtils.getServerProperty(DATASOURCE);
			dataSource = (DataSource) new InitialContext().lookup(dbJndiName);
			cnx = dataSource.getConnection();
			cnx.setAutoCommit(false);

			// Register the connection's opening.
			id = ConnectionLogger.getInstance().register(this);
		} catch (SQLException e) {
			LOGGER.fatal("Connection failed.", e);
			throw new DbException("Connection failed.", e);
		} catch (NamingException e) {
			LOGGER.fatal("Datasource not found", e);
			throw new Error("ERROR: Datasource not found: " + e, e);
		}
	}

	/**
	 * Initialize the connection to database.
	 * 
	 * @param cnx The Connection
	 */
	private static synchronized void initializeDbType(Connection cnx) {
		DbConnection localDbConnection = null;
		Connection localConnection = null;
		if (cnx != null) {
			localConnection = cnx;
		} else {
			localDbConnection = new DbConnection();
			localConnection = localDbConnection.getCnx();
		}
		try {
			String driverName = null;
			driverName = localConnection.getMetaData().getDriverName().toUpperCase();
			if (driverName.contains("ORACLE")) {
				DbConnection.dbType = Type.ORACLE;
			} else if (driverName.contains("MYSQL")) {
				DbConnection.dbType = Type.MySQL;
			} else if (driverName.contains("POSTGRESQL")) {
				DbConnection.dbType = Type.PostgreSQL;
			} else if (driverName.contains("DB2") || driverName.contains("IBM") || driverName.contains("AS/400")) {
				DbConnection.dbType = Type.DB2;
			} else if (driverName.contains("MICROSOFT") || driverName.contains("JTDS")) {
				DbConnection.dbType = Type.SQLSERVER;
			} else {
				throw new TechnicalException("Driver type not supported : " + driverName);
			}
		} catch (SQLException e) {
			LOGGER.fatal("Connection failed.", e);
			throw new DbException("Connection failed.", e);
		} finally {
			if (localDbConnection != null) {
				localDbConnection.close();
			}
		}

	}

	/**
	 * Getter of the type of the database.
	 * 
	 * @return the type of the database.
	 */
	public static synchronized DbConnection.Type getDbType() {
		if (dbType == null) {
			initializeDbType(null);
		}
		return dbType;
	}

	/**
	 * Constructor.
	 * 
	 * @param standalone A standalone connection to duplicate.
	 */
	public DbConnection(StandaloneDbConnection standalone) {
		cnx = standalone.getCnx();
		if (dbType == null) {
			initializeDbType(cnx);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param connection A Connection.
	 */
	public DbConnection(Connection connection) {
		cnx = connection;
	}

	/**
	 * Return the SQL connection
	 * 
	 * @return the connection to database.
	 */
	public Connection getCnx() {
		return cnx;
	}

	/**
	 * Close the connection.
	 * 
	 * @throws DbException If error.
	 */
	public void close() throws DbException {
		if (cnx != null) {
			try {
				rollback();
			} finally {
				try {
					cnx.close();
					cnx = null;
					ConnectionLogger.getInstance().drop(id);
				} catch (SQLException e) {
					LOGGER.error("Error closing connection.", e);
					throw new DbException(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Check connection
	 * 
	 * @return true if connection ok
	 */
	public boolean check() {
		PreparedStatement ps = null;
		try {
			String query = "SELECT 1";
			if (dbType == Type.ORACLE) {
				query += " FROM DUAL";
			}
			ps = cnx.prepareStatement(query);
			return ps.execute();
		} catch (Exception e) {
			LOGGER.error("Error checking DB connection", e);
			return false;
		} finally {
			if (ps != null)
				try {
					ps.close();
				} catch (SQLException e) {
					// Quietly close connection
				}
		}
	}

	/**
	 * commit de la transaction.
	 * 
	 * @throws DbException If error.
	 */
	public void commit() throws DbException {
		try {
			this.getCnx().commit();
		} catch (SQLException e) {
			LOGGER.error("Error committing connection.", e);
			throw new DbException(e.getMessage(), e);
		}
	}

	/**
	 * Rollback de la transaction.
	 * 
	 * @throws DbException If error.
	 */
	public void rollback() throws DbException {
		try {
			this.getCnx().rollback();
		} catch (SQLException e) {
			LOGGER.error("Error rollbacking connection.", e);
			throw new DbException(e.getMessage(), e);
		}
	}
}
