package com.cgi.commons.utils.reflect;

import com.cgi.commons.logic.DomainLogic;
import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.utils.TechnicalException;

/**
 * Utility class to instantiate domain logic classes
 */
public class DomainUtils {

	/** Domain logic package */
	public static final String DOMAIN_LOGIC_PACKAGE = "com.cgi.business.logic.";
	/** Domain logic classes suffix */
	public static final String LOGIC_SUFFIX = "Logic";

	/**
	 * Retourne la classe associée à la classe passée en paramêtre
	 * 
	 * @param entity
	 *            object dont on veut la logique
	 * @return Instance de CustomClass
	 */
	@SuppressWarnings("unchecked")
	public static DomainLogic<? extends Entity> getLogic(Entity entity, RequestContext context) {
		return (DomainLogic<? extends Entity>) internalGetLogic(entity.name(), context);
	}

	/**
	 * Retourne la classe associée à la classe passée en paramètre
	 * 
	 * @param entityName
	 *            Nom de l'entité dont on veut la customClass
	 * @return Instance de CustomClass
	 */
	@SuppressWarnings("unchecked")
	public static DomainLogic<? extends Entity> getLogic(String entityName, RequestContext context) {
		return (DomainLogic<? extends Entity>) internalGetLogic(entityName, context);
	}

	/**
	 * Instantiates a DomainLogic class for specified entity
	 * 
	 * @param entityName
	 *            Entity name
	 * @param context
	 *            Current user request context (contains a link to user)
	 * @return New Entity's DomainLogic instance
	 */
	private static Object internalGetLogic(String entityName, RequestContext context) {
		String className = entityName.substring(0, 1).toUpperCase() + entityName.substring(1) + LOGIC_SUFFIX;
		String fullClassName = DOMAIN_LOGIC_PACKAGE + className;
		try {
			return Class.forName(fullClassName).newInstance();
			/* si l'objet custom n'existe pas, création d'un custom générique */
		} catch (InstantiationException e) {
			return internalGetDefaultLogic();
		} catch (IllegalAccessException e) {
			return internalGetDefaultLogic();
		} catch (ClassNotFoundException e) {
			return internalGetDefaultLogic();
		}
	}

	/**
	 * Instantiates Default Business Logic class
	 * 
	 * @return Default logic class instance
	 */
	private static Object internalGetDefaultLogic() {
		String cName = Constants.APPLICATION_LOGIC_PACKAGE + ".DefaultLogic";
		try {
			return Class.forName(cName).newInstance();
		} catch (Exception ex) {
			throw new TechnicalException("Unable to load default logic class " + cName, ex);
		}
	}

	/**
	 * Create a new domain object of class domainName
	 * 
	 * @param domainName
	 *            domain object to instantiate
	 * @return a domainName instance
	 */
	public static Entity newDomain(String domainName) {

		if (domainName == null || "".equals(domainName)) {
			throw new TechnicalException("Domain object not found " + domainName);
		}
		String className = Constants.DOMAIN_OBJECT_PACKAGE + "." + domainName.substring(0, 1).toUpperCase()
				+ domainName.substring(1);

		try {
			return (Entity) Class.forName(className).newInstance();

		} catch (InstantiationException e) {
			throw new TechnicalException("Domain object not found " + domainName);
		} catch (IllegalAccessException e) {
			throw new TechnicalException("Domain object not found " + domainName);
		} catch (ClassNotFoundException e) {
			throw new TechnicalException("Domain object not found " + domainName);
		}
	}

	/**
	 * Création du nom de la table ou du champ à partir du nom passé en paramètre
	 * 
	 * @param name
	 * @return the formatted db name
	 */
	public static String createDbName(String name) {
		char[] strName = name.toCharArray();
		char[] strUpper = name.toUpperCase().toCharArray();
		char[] result = new char[name.length() * 2];
		int j = 0;

		for (int i = 0; i < strName.length; i++) {

			if (Character.isUpperCase(strName[i]) && i > 0) {
				result[j] = '_';
				j++;
			}

			result[j] = strUpper[i];

			j++;
		}
		return String.valueOf(result).trim();
	}

}
