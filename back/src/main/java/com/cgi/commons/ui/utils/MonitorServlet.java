package com.cgi.commons.ui.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.cgi.business.application.User;
import com.cgi.commons.db.DbManager;
import com.cgi.commons.db.DbQuery;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityModel;

/**
 * MonitorServlet which allow acces to application status
 */
public class MonitorServlet extends HttpServlet {
	/** sUid */
	private static final long serialVersionUID = -7175526239042293932L;
	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(MonitorServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MonitorServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getOutputStream().print(getServerStatus());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getOutputStream().print(getServerStatus());
	}

	private String getServerStatus() {
		RequestContext context = null;
		try {
			// Init core objects
			context = new RequestContext(new User("monitorServlet"));

			// Check DB status
			if (!context.getDbConnection().check()) {
				return "[ERROR] DB not available";
			}

			// Check entities
			if (EntityManager.domains.isEmpty()) {
				return "[ERROR] EntityManager not loaded";
			}

			// Select from first entity
			String entityName = EntityManager.domains.iterator().next();
			EntityModel entityModel = EntityManager.getEntityModel(entityName);
			DbManager dbm = null;
			try {
				// Prepare query
				DbQuery query = new DbQuery(entityModel.name(), "T1");
				query.setMaxRownum(1);

				dbm = new DbManager(context, query);
				// Request executed
				if (dbm.next()) {
					// Has result
				}

			} catch (Exception e) {
				LOGGER.error("Request failed", e);
				return "[ERROR] DB request failed";
			} finally {
				if (dbm != null) {
					dbm.close();
				}
			}
		} finally {
			if (context != null) {
				context.close();
			}
		}

		// All OK
		return "OK";
	}

}
