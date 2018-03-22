package com.cgi.commons.ref;

import com.cgi.commons.utils.MessageUtils;

/**
 * Contants.
 */
public final class Constants {

	/**
	 * Private constructor, Utility class.
	 */
	private Constants() {
	}

	/** Not Yet implemented. */
	public static final String NOT_YET_IMPLEMENTED = "NOT YET IMPLEMENTED";

	/* variables des classes custom */
	/** Package for beans. */
	public static final String DOMAIN_OBJECT_PACKAGE = "com.cgi.models.beans";
	/** Package for logic. */
	public static final String DOMAIN_LOGIC_PACKAGE = "com.cgi.business.logic";
	/** Package for application logic. */
	public static final String APPLICATION_LOGIC_PACKAGE = "com.cgi.business.application";
	/** Package for queries. */
	public static final String QUERIES_PACKAGE = "com.cgi.models.queries";
	/** Package for database. */
	public static final String DB_PACKAGE = "com.cgi.commons.db";

	/* extensions */
	/** Extension for sequence names. */
	public static final String EXTENSION_SEQUENCE = "S_";
	/** Extension for model names. */
	public static final String EXTENSION_MODEL = "Model";
	/** Extension for query names. */
	public static final String EXTENSION_QUERY = "Query";

	/* formats */
	/** Format for Date. */
	public static final String FORMAT_DATE = "dd/MM/yyyy";
	/** Format for Time. */
	public static final String FORMAT_TIME = "HH:mm:ss";
	/** Format for Hout. */
	public static final String FORMAT_HOUR = "HH:mm";
	/** Format for Miliseconds. */
	public static final String FORMAT_MS = "S";
	/** Format for Timestamp. */
	public static final String FORMAT_TIMESTAMP = FORMAT_DATE + " " + FORMAT_TIME + "." + FORMAT_MS;

	/** Format for Date ISO. */
	public static final String FORMAT_DATE_ISO = "yyyy-MM-dd";
	/** Format for Timestamp ISO. */
	public static final String FORMAT_TIMESTAMP_ISO = FORMAT_DATE_ISO + " " + FORMAT_TIME + "." + FORMAT_MS;;

	/* type action */
	/** Action Dummy. */
	public static final String DUMMY = "_DUMMY";
	/** Action Display File. */
	public static final String DISPLAY_FILE = "_DISPLAY_FILE";
	/** Action Delete File. */
	public static final String DELETE_FILE = "_DELETE_FILE";
	/** Action Detach BR. */
	public static final String DETACH_BR = "_DETACH_BR";
	/** Action Create. */
	public static final String CREATE = "CREATE";
	/** Action Select BR. */
	public static final String SELECT_BR = "_ATTACH_BR";
	/** Action Modify. */
	public static final String MODIFY = "UPDATE";
	/** Action Copy. */
	public static final String COPY = "COPY";
	/** Action Delete. */
	public static final String DELETE = "DELETE";
	/** Action Display. */
	public static final String DISPLAY = "DIPLAY";
	/** Action Search. */
	public static final String SEARCH = "_SEARCH";
	/** Action Rename. */
	public static final String RENAME = "Rename";

	/* code action par défaut */
	/** Action Create. */
	public static final String ACTION_CREATE = "CREATE";
	/** Action Modify. */
	public static final String ACTION_MODIFY = "UPDATE";
	/** Action Copy. */
	public static final String ACTION_COPY = "COPY";
	/** Action Delete. */
	public static final String ACTION_DELETE = "DELETE";
	/** Action Display. */
	public static final String ACTION_DISPLAY = "DIPLAY";
	/** Action Rename. */
	public static final String ACTION_Rename = "Rename";

	/* Chaines utilisées pour identifier des trucs dans le code */
	/** Primary Key in a Result. */
    public static final String RESULT_PK = "primaryKey";
    /** Rownum in a Result. */
    public static final String RESULT_ROWNUM = "$rownum";
    /** Custom Data. */
	public static final String CUSTOM_DATA = "cData_key_";
	/** Permalink Login Key. */
	public static final String PERMALINK_LOGIN_KEY = "permalinkKey"; 

	/** Event. */
	public static final String EVENT = "event";
	/** Event title. */
	public static final String EVENT_TITLE = "title";
	/** Event all day. */
	public static final String EVENT_ALL_DAY = "allDay";
	/** Event Read Only. */
	public static final String EVENT_READ_ONLY = "readOnly";
	/** Event Date Start. */
	public static final String EVENT_DATE_START = "dateStart";
	/** Event Date End. */
	public static final String EVENT_DATE_END = "dateEnd";
	/** Event Css Classname. */
	public static final String EVENT_CSS_CLASSNAME = "className";
	/** Event Css Color. */
	public static final String EVENT_CSS_COLOR = "color";
	/** Event Css Background Color. */
	public static final String EVENT_CSS_BACKGROUND_COLOR = "backgroundColor";
	/** Event Css Border Color. */
	public static final String EVENT_CSS_BORDER_COLOR = "borderColor";
	/** Event Css Text Color. */
	public static final String EVENT_CSS_TEXT_COLOR = "textColor";
	/** Event Create Date Start. */
	public static final String EVENT_CREATE_DATE_START = "createStartTime";
	/** Event Create Date End. */
	public static final String EVENT_CREATE_DATE_END = "createEndTime";

	/** Préfixe de la variable contenant l'adresse (associée à une variable de type Geometry). */
	public final static String GEOMETRY_ADDRESS = "geoadr";

	/** Ressources binaires gérées manuellement. */
	public static final String CUSTOM_RESSOURCE = "customRessource";

	/** Maximum number of rows initially defined on the criteria page. */
	public static int MAX_ROW = 200;
	
	/** Absolute maximum for the number of row, impossible for the user to bypass. Not applicable to custom code. */
	public static final int MAX_ROW_ABSOLUTE = 9999;
	
	/** Limit in Excel due to XLS format. */
	public static final int MAX_ROW_EXCEL_EXPORT = 65535;
	
	/** Max Row for Autocomplete. */
	public static int AUTOCOMPLETE_MAX_ROW = 20;
	
	/** Default duration for Event create. */
	public static final int EVENT_CREATE_DEFAULT_DURATION = 2;

	/** Parameter to define current application. Useful for permalinks and defaultPage loading */
	public static final String CURRENT_APP_PARAMETER = "currentApp";
	
	/** Parameter set when an action has a side effect on linked entity and it should be marked as dirty */
	public static final String MARK_LINKED_ENTITY_DIRTY = "MARK_LINKED_ENTITY_DIRTY";

	/**
	 * Parameter in criteria map that contains a set of field names which should be treated as exact matches and not with like conditions because
	 * they came from comboboxes
	 */
	public static final String SEARCH_PARAM_COMBO_EXACT_MATCH = "SEARCH_PARAM_COMBO_EXACT_MATCH";
	
	/** Request context Parameter used when loading a combobox. Parameter value will be set with the query name */
	public static final String LOAD_COMBO = "LOAD_COMBO";
	
	static {
		// Try to load some value from file
		String maxRowStr = MessageUtils.getServerProperty("query.maxrow");
		if (maxRowStr != null) {
			try {
				MAX_ROW = Integer.parseInt(maxRowStr);
			} catch (NumberFormatException e) {
				// Not a valid number
			}
		}

		String autocompleteMaxRowStr = MessageUtils.getServerProperty("query.autocomplete.maxrow");
		if (autocompleteMaxRowStr != null) {
			try {
				AUTOCOMPLETE_MAX_ROW = Integer.parseInt(autocompleteMaxRowStr);
			} catch (NumberFormatException e) {
				// Not a valid number
			}
		}
	}
}
