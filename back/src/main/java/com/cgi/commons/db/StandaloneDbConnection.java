package com.cgi.commons.db;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * Standalone Database Connection.
 */
public class StandaloneDbConnection implements Closeable {
	
	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(StandaloneDbConnection.class);
	/** Path root for the DataSource. */
	public static final String CONTEXT_ROOT = "java:";
	/** Path to the DataSource. */
	public static final String DEFAULT_JNDI_ROOT = "jdbc/TOY";

	/** The Connection. */
	private Connection cnx;
	/** Statement. */
	private Statement stm;

	/**
	 * Constructor.
	 * 
	 * @param jdbcString Url of the Connection.
	 * @param user User.
	 * @param password Password. 
	 * @throws ClassNotFoundException If error.
	 */
	public StandaloneDbConnection(String jdbcString, String user, String password) throws ClassNotFoundException {
		try {
			cnx = DriverManager.getConnection(jdbcString, user, password);
			cnx.setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException("Connection failed.", e);
		}
	}
	
	/**
	 * Return the connection to database.
	 * 
	 * @return the connection to database.
	 */
	public Connection getCnx() {
		return cnx;
	}

	/**
	 * Close the connection.
	 */
	public void close() {
		if (cnx != null) {
			try {
				cnx.close();
				cnx = null;
			} catch (SQLException e) {
				LOGGER.error("Error closing connection.", e);
			}
		}
	}

	/**
	 * Execute a query.
	 * 
	 * @param sql The query to execute.
	 * @return True if ok.
	 */
	public boolean execute(String sql) {
		if (stm == null) {
			try {
				stm = cnx.createStatement();
			} catch (SQLException e) {
				LOGGER.fatal("Error preparing statement.", e);
				throw new RuntimeException("Create Statement failed.", e);
			}
		}
		try {
			return !stm.execute(sql);
		} catch (SQLException e) {
			LOGGER.error("Error executing query.", e);
			return false; // Failsafe
		}
	}

	/**
	 * Execute a query.
	 * @param sql The Query to execute.
	 * @param message The message to log if ok or if error.
	 */
	public void execute(String sql, String message) {
		if (execute(sql)) {
			LOGGER.info(message + ": ok");
		} else {
			LOGGER.warn("ERREUR " + message);
		}
	}

	/**
	 * commit de la transaction.
	 * 
	 * @throws SQLException If error.
	 */
	public void commit() throws SQLException {
		this.getCnx().commit();
	}

	/**
	 * Rollback de la transaction.
	 * 
	 * @throws SQLException If error.
	 */
	public void rollback() throws SQLException {
		this.getCnx().rollback();
	}
}
