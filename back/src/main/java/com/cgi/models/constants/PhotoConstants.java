package com.cgi.models.constants;

/**
 * Constants interface for the entity Photo.
 *
 */
public interface PhotoConstants {
	/** Name of the entity. */
	String ENTITY_NAME = "photo";

	/** Table name for this entity or REST class name for external entity. */
	String ENTITY_DB_NAME = "PHOTO";

	/** Holder for the var names. */
	interface Vars {
		/** Var ID. */
		String ID = "id";
		/** Var IMAGE. */
		String IMAGE = "image";
	}

	/** Holder for the DB fields name. */
	interface DbFields {
		/** Field ID. */
		String ID = "ID";
		/** Field IMAGE. */
		String IMAGE = "IMAGE";
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
	}

	/** Holder for the query names. */
	interface Query {
		/** Query PHOTO. */
		String PHOTO = "PHOTO";
	}

	/** Holder for the query aliases. */
	interface Alias {
		/** Aliases for query PHOTO. */
		interface PHOTO { 
			/** Entity PHOTO, alias T1 */
			String T1 = "T1";
			/** Table Alias T1, Var id */
			String T1_ID = "T1_id";
			/** Table Alias T1, Var image */
			String T1_IMAGE = "T1_image";
		}
	}

	/** Holder for the page names. */
	interface Pages {
	}

	/** Holder for the templates names. */
	interface Templates {
		/** Template PHOTO. */
		String PHOTO = "photo";
	}

}
