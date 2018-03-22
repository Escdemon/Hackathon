package com.cgi.models.constants;

/**
 * Constants interface for the entity Localisation.
 *
 */
public interface LocalisationConstants {
	/** Name of the entity. */
	String ENTITY_NAME = "localisation";

	/** Table name for this entity or REST class name for external entity. */
	String ENTITY_DB_NAME = "LOCALISATION";

	/** Holder for the var names. */
	interface Vars {
		/** Var ID. */
		String ID = "id";
		/** Var COORD_X. */
		String COORD_X = "coordX";
		/** Var COORD_Y. */
		String COORD_Y = "coordY";
		/** Var HEURE. */
		String HEURE = "heure";
		/** Var STATUT. */
		String STATUT = "statut";
		/** Var BALISE_ID. */
		String BALISE_ID = "baliseId";
	}

	/** Holder for the DB fields name. */
	interface DbFields {
		/** Field ID. */
		String ID = "ID";
		/** Field COORD_X. */
		String COORD_X = "COORD_X";
		/** Field COORD_Y. */
		String COORD_Y = "COORD_Y";
		/** Field HEURE. */
		String HEURE = "HEURE";
		/** Field STATUT. */
		String STATUT = "STATUT";
		/** Field BALISE_ID. */
		String BALISE_ID = "BALISE_ID";
	}

	/** Holder for the defined values. */
	interface ValueList {
		/**
		 * Holder for the defined values of var STATUT.
		 */
		interface STATUT {
			/** Oui. */
			Boolean VRAI = true;
			/** Non. */
			Boolean FAUX = false;
		}
	}

	/** Holder for the action names. */
	interface Actions {
		/** Attacher. */
		String ACTION_ATTACH = "ATTACH";
		/** Détacher. */
		String ACTION_DETACH = "DETACH";
		/** Créer Alerte. */
		String ACTION_CREATE_ALERT = "CREATE_ALERT";
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
		/** Query LOCALISATION. */
		String LOCALISATION = "LOCALISATION";
	}

	/** Holder for the query aliases. */
	interface Alias {
		/** Aliases for query LOCALISATION. */
		interface LOCALISATION { 
			/** Entity LOCALISATION, alias T1 */
			String T1 = "T1";
			/** Table Alias T1, Var id */
			String T1_ID = "T1_id";
			/** Table Alias T1, Var coordX */
			String T1_COORD_X = "T1_coordX";
			/** Table Alias T1, Var coordY */
			String T1_COORD_Y = "T1_coordY";
			/** Table Alias T1, Var heure */
			String T1_HEURE = "T1_heure";
			/** Table Alias T1, Var statut */
			String T1_STATUT = "T1_statut";
			/** Table Alias T1, Var baliseId */
			String T1_BALISE_ID = "T1_baliseId";
		}
	}

	/** Holder for the page names. */
	interface Pages {
	}

	/** Holder for the templates names. */
	interface Templates {
		/** Template LOCALISATION. */
		String LOCALISATION = "localisation";
	}

}
