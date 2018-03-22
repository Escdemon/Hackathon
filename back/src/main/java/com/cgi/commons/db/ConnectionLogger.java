package com.cgi.commons.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Log all Db Connections.
 */
public class ConnectionLogger {

	/** ArrayList who save all connections opened. */
	private Map<Integer, ConnectionObject> mapConnections = new HashMap<Integer, ConnectionObject>();

	/** Next Connection id. */
	private int nextId = 1;

	/** Private constructor. */
	private ConnectionLogger() {
	}

	/** Unique instance of the class. */
	private static ConnectionLogger INSTANCE = new ConnectionLogger();

	/**
	 * Entry point to the unique instance of the class.
	 * 
	 * @return The instance.
	 */
	public static ConnectionLogger getInstance() {
		return INSTANCE;
	}

	/**
	 * Register a new DB connection and save the stackTrace's five first line.
	 * 
	 * @param conn
	 *            The new DB Connection to register.
	 * @return An unique id for the connection.
	 */
	public int register(DbConnection conn) {
		int id = nextId++;
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		ConnectionObject c = new ConnectionObject(id, conn, Arrays.copyOfRange(stackTrace, 1, stackTrace.length - 1));
		mapConnections.put(id, c);
		return id;
	}

	/**
	 * Drop an already registered connection.
	 * 
	 * @param id
	 *            The unique id returned by the register method.
	 */
	public void drop(int id) {
		mapConnections.remove(id);
	}

	/**
	 * Map all opened connections.
	 * 
	 * @return The map.
	 */
	public Map<Integer, ConnectionObject> getOpenedConnections() {
		return mapConnections;
	}

	/**
	 * Close a connection.
	 * 
	 * @param id
	 *            The connection's id.
	 */
	public void closeConnection(int id) {
		if (mapConnections.containsKey(id)) {
			ConnectionObject conn = mapConnections.get(id);
			if (conn != null) {
				conn.close();
			}
		}
		mapConnections.remove(id);
	}
	
	/**
	 * Get the list of the connections opened.
	 * 
	 * @return The list of the connections opened.
	 */
	public List<ConnectionObject> getListConnections() {
		List<ConnectionObject> list = new ArrayList<ConnectionObject>();
		Collection<ConnectionObject> openedConn = getOpenedConnections().values();
		if (openedConn.size() > 0) {
			list.addAll(openedConn);
			Collections.sort(list, new Comparator<ConnectionObject>() {
				@Override
				public int compare(ConnectionObject o1, ConnectionObject o2) {
					return o1.getId() - o2.getId();
				}
			});
		}
		return list;
	}
}
