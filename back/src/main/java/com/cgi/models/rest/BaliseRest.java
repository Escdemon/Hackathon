package com.cgi.models.rest;

import java.io.Serializable;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.rest.domain.RestEntity;
import com.cgi.models.constants.BaliseConstants;

/**
 * Web Service Rest for Entity Balise.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BaliseRest extends RestEntity implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 1L;
	
	@Override
	public String name() {
		return "balise";
	}

	/** Technical ID. */
	private Long id;

	/** Nom. */
	private String nom;

	/** Description. */
	private String internalCaption;

	/**
	 * Retourne la valeur du champ Id.
	 *
	 * @return Valeur de Id.
	 */
	@XmlElement
	public Long getId() {
		return this.id;
	}
	
	/**
	 * Renseigne le champ Id.
	 * 
	 * @param id Nouvelle valeur.
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * Retourne la valeur du champ Nom.
	 *
	 * @return Valeur de Nom.
	 */
	@XmlElement
	public String getNom() {
		return this.nom;
	}
	
	/**
	 * Renseigne le champ Nom.
	 * 
	 * @param nom Nouvelle valeur.
	 */
	public void setNom(final String nom) {
		this.nom = nom;
	}

	/**
	 * Retourne la valeur du champ InternalCaption.
	 *
	 * @return Valeur de InternalCaption.
	 */
	@XmlElement
	public String getInternalCaption() {
		return this.internalCaption;
	}
	
	/**
	 * Renseigne le champ InternalCaption.
	 * 
	 * @param internalCaption Nouvelle valeur.
	 */
	public void setInternalCaption(final String internalCaption) {
		this.internalCaption = internalCaption;
	}

	@XmlRootElement
	public static class BaliseRestKey extends Key implements Serializable {
		private static final long serialVersionUID = 1L;

		public BaliseRestKey() {
			super();
		}

		public BaliseRestKey(String encodedString) {
			super(BaliseConstants.ENTITY_NAME, encodedString);
		}
	}
}
