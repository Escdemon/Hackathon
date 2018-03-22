package com.cgi.models.constants;

/**
 * Constants interface for the entity Balise.
 *
 */
public interface BaliseConstants {
	/** Name of the entity. */
	String ENTITY_NAME = "balise";

	/** Table name for this entity or REST class name for external entity. */
	String ENTITY_DB_NAME = "BALISE";

	/** Holder for the var names. */
	interface Vars {
		/** Var ID. */
		String ID = "id";
		/** Var NOM. */
		String NOM = "nom";
		/** Var INTERNAL_CAPTION. */
		String INTERNAL_CAPTION = "internalCaption";
		/** Var W$_DESC - deprecated: use INTERNAL_CAPTION instead. */
		@Deprecated 
		String W$_DESC = "w$Desc";
	}

	/** Holder for the DB fields name. */
	interface DbFields {
		/** Field ID. */
		String ID = "ID";
		/** Field NOM. */
		String NOM = "NOM";
	}

	/** Holder for the action names. */
	interface Actions {
		/** Attacher. */
		String ACTION_ATTACH = "ATTACH";
		/** Détacher. */
		String ACTION_DETACH = "DETACH";
		/** Créer. */
		String ACTION_CREATE = "CREATE";
		/** Modifier. */
		String ACTION_UPDATE = "UPDATE";
		/** Afficher. */
		String ACTION_DISPLAY = "DISPLAY";
		/** Supprimer. */
		String ACTION_DELETE = "DELETE";
		/** Lister. */
		String ACTION_LIST = "LIST";
	}

	/** Holder for the link names. */
	interface Links {
		/** Link to BALISE. */ 
		String LINK_LOCALISATION_R_BALISE = "localisationRBalise";
	}

	/** Holder for the query names. */
	interface Query {
		/** Query BALISE. */
		String BALISE = "BALISE";
	}

	/** Holder for the query aliases. */
	interface Alias {
		/** Aliases for query BALISE. */
		interface BALISE { 
			/** Entity BALISE, alias T1 */
			String T1 = "T1";
			/** Table Alias T1, Var id */
			String T1_ID = "T1_id";
			/** Table Alias T1, Var nom */
			String T1_NOM = "T1_nom";
			/** Table Alias T1, Var internalCaption */
			String T1_INTERNAL_CAPTION = "T1_internalCaption";
			/** Table Alias T1, Var w$Desc */
			@Deprecated
			String T1_W$_DESC = "T1_w$Desc";
		}
	}

	/** Holder for the page names. */
	interface Pages {
	}

	/** Holder for the templates names. */
	interface Templates {
		/** Template BALISE. */
		String BALISE = "balise";
	}

}
