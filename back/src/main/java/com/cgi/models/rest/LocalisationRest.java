package com.cgi.models.rest;

import java.io.Serializable;
import java.util.Date;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.rest.domain.RestEntity;
import com.cgi.models.constants.LocalisationConstants;

/**
 * Web Service Rest for Entity Localisation.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LocalisationRest extends RestEntity implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 1L;
	
	@Override
	public String name() {
		return "localisation";
	}

	/** Technical ID. */
	private Long id;

	/** Coordonnée x. */
	private Integer coordX;

	/** Coordonnée y. */
	private Integer coordY;

	/** Heure. */
	private Date heure;

	/** Statut. */
	private Boolean statut;

	/** Technical ID. */
	private Long baliseId;

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
	 * Retourne la valeur du champ CoordX.
	 *
	 * @return Valeur de CoordX.
	 */
	@XmlElement
	public Integer getCoordX() {
		return this.coordX;
	}
	
	/**
	 * Renseigne le champ CoordX.
	 * 
	 * @param coordX Nouvelle valeur.
	 */
	public void setCoordX(final Integer coordX) {
		this.coordX = coordX;
	}

	/**
	 * Retourne la valeur du champ CoordY.
	 *
	 * @return Valeur de CoordY.
	 */
	@XmlElement
	public Integer getCoordY() {
		return this.coordY;
	}
	
	/**
	 * Renseigne le champ CoordY.
	 * 
	 * @param coordY Nouvelle valeur.
	 */
	public void setCoordY(final Integer coordY) {
		this.coordY = coordY;
	}

	/**
	 * Retourne la valeur du champ Heure.
	 *
	 * @return Valeur de Heure.
	 */
	@XmlElement
	public Date getHeure() {
		return this.heure;
	}
	
	/**
	 * Renseigne le champ Heure.
	 * 
	 * @param heure Nouvelle valeur.
	 */
	public void setHeure(final Date heure) {
		this.heure = heure;
	}

	/**
	 * Retourne la valeur du champ Statut.
	 *
	 * @return Valeur de Statut.
	 */
	@XmlElement
	public Boolean getStatut() {
		return this.statut;
	}
	
	/**
	 * Renseigne le champ Statut.
	 * 
	 * @param statut Nouvelle valeur.
	 */
	public void setStatut(final Boolean statut) {
		this.statut = statut;
	}

	/**
	 * Retourne la valeur du champ BaliseId.
	 *
	 * @return Valeur de BaliseId.
	 */
	@XmlElement
	public Long getBaliseId() {
		return this.baliseId;
	}
	
	/**
	 * Renseigne le champ BaliseId.
	 * 
	 * @param baliseId Nouvelle valeur.
	 */
	public void setBaliseId(final Long baliseId) {
		this.baliseId = baliseId;
	}

	@XmlRootElement
	public static class LocalisationRestKey extends Key implements Serializable {
		private static final long serialVersionUID = 1L;

		public LocalisationRestKey() {
			super();
		}

		public LocalisationRestKey(String encodedString) {
			super(LocalisationConstants.ENTITY_NAME, encodedString);
		}
	}
}
