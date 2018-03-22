package com.cgi.commons.ref.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.cgi.commons.ref.Constants;

/**
 * Action.
 */
public class Action implements Serializable, Cloneable {

	/** Serial id. */
	private static final long serialVersionUID = 1301307950031652178L;

	/** Entity where this action is defined. */
	private String entityName;
	/** Code. */
	private String code;
	/** Code of the sub Action if one. */
	private String subActionCode;
	/** Set of the sub Actions. */
	private Set<String> subActions;
	/** Query Name. */
	private String queryName;
	/** Page Name. */
	private String pageName;

	/** Input. */
	private Input input;
	/** Persistence. */
	private Persistence persistence;
	/** User Interface. */
	private UserInterface ui;
	/** Process. */
	private Process process;

	/**
	 * Constructor.
	 * 
	 * @param entityName Entity Name.
	 * @param code Code.
	 * @param queryName Query Name.
	 * @param pageName Page Name.
	 * @param input Input.
	 * @param persistence Persistence.
	 * @param ui User Interface.
	 * @param process Process.
	 */
	public Action(String entityName, String code, String queryName, String pageName, Input input,
			Persistence persistence, UserInterface ui, Process process) {
		this(code, queryName, pageName, input, persistence, ui, process);
		this.entityName = entityName;
	}
	
	/**
	 * Constructor. 
	 * 
	 * @param code Code.
	 * @param queryName Query Name.
	 * @param pageName Page Name.
	 * @param input Input.
	 * @param persistence Persistence.
	 * @param ui User Interface.
	 * @param process Process.
	 */
	public Action(String code, String queryName, String pageName, Input input,
			Persistence persistence, UserInterface ui, Process process) {
		this.entityName = null;
		this.code = code;
		this.queryName = queryName;
		this.pageName = pageName;
		this.input = input;
		this.persistence = persistence;
		this.ui = ui;
		this.process = process;
		this.subActions = new HashSet<String>();
		this.subActionCode = null;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param entityName Entity Name.
	 * @param code Code.
	 * @param queryName Query Name.
	 * @param pageName Page Name.
	 * @param input Input.
	 * @param persistence Persistence.
	 * @param ui User Interface.
	 * @param process Process.
	 * @param subActions Code of the subactions.
	 */
	public Action(String entityName, String code, String queryName, String pageName, Input input,
			Persistence persistence, UserInterface ui, Process process, String... subActions) {
		this(entityName, code, queryName, pageName, input, persistence, ui, process);
		this.subActions.addAll(Arrays.asList(subActions));
	}
	
	/**
	 * Constructor.
	 * 
	 * @param code Code.
	 * @param queryName Query Name.
	 * @param pageName Page Name.
	 * @param input Input.
	 * @param persistence Persistence.
	 * @param ui User Interface.
	 * @param process Process.
	 * @param subActions Code of the subactions.
	 */
	public Action(String code, String queryName, String pageName, Input input,
			Persistence persistence, UserInterface ui, Process process, String... subActions) {
		this(code, queryName, pageName, input, persistence, ui, process);
		this.subActions.addAll(Arrays.asList(subActions));
	}

	/**
	 * Constructor.
	 * 
	 * @param a Action to duplicate.
	 */
	public Action(Action a) {
		this.entityName = a.getEntityName();
		this.code = a.getCode();
		this.subActions = new HashSet<String>(a.getSubActions());
		this.queryName = a.getQueryName();
		this.pageName = a.getPageName();
		this.input = a.getInput();
		this.persistence = a.getPersistence();
		this.ui = a.getUi();
		this.process = a.getProcess();
		this.subActionCode = null;
	}

	/**
	 * Retrieve a subAction thanks to it code.
	 * @param subActionCode The code of the subAction.
	 * @return The subAction.
	 */
	public Action getSubAction(String subActionCode) {
		if (!subActions.contains(subActionCode)) {
			return null;
		}
		Action subAction = null;
		try {
			subAction = (Action) this.clone();
			subAction.subActions = null;
			subAction.subActionCode = subActionCode;
		} catch (CloneNotSupportedException e) {

		}
		return subAction;
	}

	/**
	 * Get a Search Action for a List Page.
	 * 
	 * @param queryName Name of the query.
	 * @param pageName Name of the page.
	 * @return The Action.
	 */
	public static Action getListAction(String queryName, String pageName) {
		return new Action(Constants.SEARCH, queryName, pageName, Input.QUERY, Persistence.NONE, UserInterface.OUTPUT,
				Process.STANDARD);
	}

	/**
	 * Get a Dummy Action for a Back Ref.
	 * 
	 * @return The Action.
	 */
	public static Action getDummyAction() {
		return new Action(Constants.DUMMY, null, null, Input.NONE, Persistence.NONE, UserInterface.NONE,
				Process.NONE);
	}

	/**
	 * Indicates if the action has sub actions.
	 * @return true if the action has sub actions.
	 */
	public boolean hasSubActions() {
		return subActions.size() > 0;
	}

	/** What is used as input for the action. */
	public enum Input {
		/** No input. */
		NONE, 
		/** A single object. */
		ONE, 
		/** A list of objects (list of keys). */
		MANY, 
		/** A database query. */
		QUERY
	}

	/** How the database is modified by the action. */
	public enum Persistence {
		/** No database modifications. */
		NONE, 
		/** New data will be inserted. */
		INSERT, 
		/** Data will be updated. */
		UPDATE, 
		/** Data will be deleted. */
		DELETE
	}

	/** Does the action interacts with the user and how. */
	public enum UserInterface {
		/** No interaction. */
		NONE, 
		/** Use an input page (also called an action page). */
		INPUT, 
		/** Use a display page. */
		READONLY, 
		/** Use a page as a result of the action (ex: a list page). */
		OUTPUT
	}

	/** How is realized the action. */
	public enum Process {
		/** No action is to be done. */
		NONE,
		/** Link process. */
		LINK,
		/** Automatic process (DB manipulation). */
		STANDARD,
		/** Call for custom code. */
		CUSTOM, 
	}

	public String getEntityName() {
		return entityName;
	}

	public String getCode() {
		return code;
	}

	public String getSubActionCode() {
		return subActionCode;
	}

	public Set<String> getSubActions() {
		return subActions;
	}

	public String getQueryName() {
		return queryName;
	}

	public String getPageName() {
		return pageName;
	}

	public Input getInput() {
		return input;
	}

	public Persistence getPersistence() {
		return persistence;
	}

	public UserInterface getUi() {
		return ui;
	}

	public Process getProcess() {
		return process;
	}

	/**
	 * Returns true if parameter code is equal to value of attribute code.
	 * 
	 * @param code The action code to check
	 * @return boolean true if code == this.code
	 */
	public boolean is(String code) {
		return (this.code != null) && this.code.equals(code);
	}

	/**
	 * Returns true if parameters entityName,code match the action attributes.
	 * 
	 * @param entityName The entity name to check
	 * @param code The action code to check
	 * @return boolean true if code == this.code
	 */
	public boolean is(String entityName, String code) {
		return (this.entityName != null ? this.entityName.equals(entityName) : entityName == null) && is(code);
	}

	/**
	 * Returns true if parameter code is equal to int value of attribute code and the parameter subActionCode is equal the current subAction code
	 * (if there's no sub action, this will be false)
	 * 
	 * @param code The action code to check
	 * @param subActionCode The sub action code to check
	 * @return boolean true if code == this.code && subActionCode == this.subActionCode
	 */
	public boolean is(String entityName, String code, String subActionCode) {
		if ((this.entityName != null && !this.entityName.equals(entityName)) || !is(code)) {
			return false;
		}
		if (this.subActionCode.isEmpty() || !this.subActionCode.equals(subActionCode)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Action [code=" + code + ", queryName=" + queryName + ", pageName=" + pageName + ", input=" + input
				+ ", persistence=" + persistence + ", ui=" + ui + ", process=" + process + "]";
	}

	/**
	 * Get a dummy instance of an Action.<br>
	 * This means : Input.NONE, Persistence.NONE, UserInterface.NONE, Process.NONE
	 * 
	 * @return new Action instance
	 */
	public static Action getDummy() {
		return new Action(Constants.DUMMY, null, null, Input.NONE, Persistence.NONE, UserInterface.NONE, Process.NONE);
	}

	/**
	 * Get a instance of a Create ation.<br>
	 * This means : Input.NONE, Persistence.INSERT, UserInterface.INPUT, Process.STANDARD
	 * 
	 * @return new Action instance
	 */
	public static Action getCreate() {
		return new Action(Constants.CREATE, null, null, Input.NONE, Persistence.INSERT, UserInterface.INPUT, Process.STANDARD);
	}

	/**
	 * Get a instance of an Update ation.<br>
	 * This means : Input.ONE, Persistence.UPDATE, UserInterface.INPUT, Process.STANDARD
	 * 
	 * @return new Action instance
	 */
	public static Action getUpdate() {
		return new Action(Constants.MODIFY, null, null, Input.ONE, Persistence.UPDATE, UserInterface.INPUT, Process.STANDARD);
	}

	/**
	 * Get a instance of a Delete ation.<br>
	 * This means : Input.ONE, Persistence.DELETE, UserInterface.READONLY, Process.STANDARD
	 * 
	 * @return new Action instance
	 */
	public static Action getDelete() {
		return new Action(Constants.DELETE, null, null, Input.ONE, Persistence.DELETE, UserInterface.READONLY, Process.STANDARD);
	}

	/**
	 * Returns true if action is detach action (ie Persistence is DELETE and Process is LINK).
	 *
	 * @return boolean true if persistence == DELETE and process == LINK.
	 */
	public boolean isDetach() {
		return Persistence.DELETE == this.persistence && Process.LINK == this.process;
	}

	/**
	 * Returns true if action is attach action (ie Persistence is INSERT and Process is LINK).
	 *
	 * @return boolean true if persistence == INSERT and process == LINK.
	 */
	public boolean isAttach() {
		return Persistence.INSERT == this.persistence && Process.LINK == this.process;
	}
}
