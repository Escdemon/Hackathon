package com.cgi.models.rest;

import java.io.Serializable;
import java.util.Arrays;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.FileContainer;
import com.cgi.commons.rest.domain.RestEntity;
import com.cgi.models.constants.PhotoConstants;

/**
 * Web Service Rest for Entity Photo.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class PhotoRest extends RestEntity implements Serializable {

	/** Serial id. */
	private static final long serialVersionUID = 1L;
	
	@Override
	public String name() {
		return "photo";
	}

	/** Technical ID. */
	private Long id;

	/** Image. */
	private FileContainer image;

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
	 * Retourne la valeur du champ Image.
	 *
	 * @return Valeur de Image.
	 */
	@XmlElement
	public FileContainer getImage() {
		return this.image;
	}
	
	/**
	 * Renseigne le champ Image.
	 * 
	 * @param image Nouvelle valeur.
	 */
	public void setImage(final FileContainer image) {
		if (image == null) {
			if (this.image != null) {
				this.image.deleteFile();
			}
		} else {
			this.image = image;
		}
	}

	@XmlRootElement
	public static class PhotoRestKey extends Key implements Serializable {
		private static final long serialVersionUID = 1L;

		public PhotoRestKey() {
			super();
		}

		public PhotoRestKey(String encodedString) {
			super(PhotoConstants.ENTITY_NAME, encodedString);
		}
	}
}
