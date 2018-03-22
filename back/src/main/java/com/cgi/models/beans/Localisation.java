package com.cgi.models.beans;

import java.io.Serializable;
import java.util.Date;

import com.cgi.commons.ref.entity.Action.*;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.KeyModel;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.db.DB;
import com.cgi.commons.ref.annotations.*;
import com.cgi.models.constants.LocalisationConstants;

import static com.cgi.commons.ref.entity.Action.Input.*;

/**
 * Entity Localisation definition.
 */
@EntityDef(dbName = "LOCALISATION", primaryKey = { "id" })
@Links({
	@Link(name = "localisationRBalise", targetEntity = "balise", fields = { "baliseId" })
})
@Actions({
	@Action(code = "ATTACH", input = MANY, persistence = Persistence.INSERT, ui = UserInterface.OUTPUT, process =  com.cgi.commons.ref.entity.Action.Process.LINK),
	@Action(code = "DETACH", input = MANY, persistence = Persistence.DELETE, ui = UserInterface.OUTPUT, process =  com.cgi.commons.ref.entity.Action.Process.LINK),
	@Action(code = "CREATE_ALERT", input = NONE, persistence = Persistence.INSERT, ui = UserInterface.NONE, process =  com.cgi.commons.ref.entity.Action.Process.CUSTOM),
	@Action(code = "CREATE", input = NONE, persistence = Persistence.INSERT, ui = UserInterface.NONE, process =  com.cgi.commons.ref.entity.Action.Process.CUSTOM),
	@Action(code = "UPDATE", persistence = Persistence.UPDATE),
	@Action(code = "DISPLAY", persistence = Persistence.NONE, ui = UserInterface.READONLY),
	@Action(code = "DELETE", persistence = Persistence.DELETE, ui = UserInterface.OUTPUT),
	@Action(code = "LIST", input = QUERY, persistence = Persistence.NONE, ui = UserInterface.OUTPUT)
})
public class Localisation extends Entity implements Serializable {
	/** Serial id. */
	public static final long serialVersionUID = 1L;

	/** Technical ID. */
	@EntityField(sqlName = "ID", sqlType = SqlTypes.DECIMAL, sqlSize = 19, isMandatory = true, isAutoIncrementField = true)
	private Long id;

	/** Coordonnée x. */
	@EntityField(sqlName = "COORD_X", sqlType = SqlTypes.INTEGER, sqlSize = 5)
	private Integer coordX;

	/** Coordonnée y. */
	@EntityField(sqlName = "COORD_Y", sqlType = SqlTypes.INTEGER, sqlSize = 5)
	private Integer coordY;

	/** Heure. */
	@EntityField(sqlName = "HEURE", sqlType = SqlTypes.TIMESTAMP, sqlSize = 0)
	private Date heure;

	/** Statut. */
	@EntityField(sqlName = "STATUT", sqlType = SqlTypes.BOOLEAN, sqlSize = 0, defaultValue = "true")
	@DefinedValues({
			@DefinedValue(code = "VRAI", label = "localisation.statut.VRAI", value = "true", isDefault = true), // Oui
			@DefinedValue(code = "FAUX", label = "localisation.statut.FAUX", value = "false") // Non
	})
	private Boolean statut = true;

	/** Technical ID. */
	@EntityField(sqlName = "BALISE_ID", sqlType = SqlTypes.DECIMAL, sqlSize = 19, isMandatory = true)
	private Long baliseId;

	/**
	 * Initialize a new Localisation.<br/>
	 * <b>The fields with initial value will be populated.</b>
	 */
	public Localisation() {
		// Default constructor
		super();
	}

	/**
	 * Initialize a new domain with its primary key. <br/>
	 * <b>This method does not load a bean from database.</b><br/>
	 * <b>This method does not call the logic method dbPostLoad().</b>
	 * 
	 * @param id Technical ID

	 */
	public Localisation(Long id) {
		super();
		Key primaryKey = buildPrimaryKey(id);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Initialize a new Localisation from an existing Localisation.<br/>
	 * <b>All fields value are copied.</b>
	 *
	 * @param pLocalisation The existing Localisation.
	 */
	public Localisation(Localisation pLocalisation) {
		super(pLocalisation);
	}

	/**
	 * Generate a primary key for the entity.
	 *
	 * @param id ID.
	 * @return The key.
	 */
	public static Key buildPrimaryKey(Long id) {
		KeyModel pkModel = new KeyModel(LocalisationConstants.ENTITY_NAME);
		Key key = new Key(pkModel);
		key.setValue(LocalisationConstants.Vars.ID, id);

		return key;
	}

	@Override
	public String name() {
		return LocalisationConstants.ENTITY_NAME;
	}

	@Override
	public String description() {
		return null;
	}

	/**
	 * Get the value from field Id.<br>
	 * <b>Is part of PrimaryKey</b><br>
	 *
	 * @return the value
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * Set the value from field Id.
	 *
	 * @param id : the value to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * Get the value from field CoordX.<br>
	 *
	 * @return the value
	 */
	public Integer getCoordX() {
		return this.coordX;
	}

	/**
	 * Set the value from field CoordX.
	 *
	 * @param coordX : the value to set
	 */
	public void setCoordX(final Integer coordX) {
		this.coordX = coordX;
	}

	/**
	 * Get the value from field CoordY.<br>
	 *
	 * @return the value
	 */
	public Integer getCoordY() {
		return this.coordY;
	}

	/**
	 * Set the value from field CoordY.
	 *
	 * @param coordY : the value to set
	 */
	public void setCoordY(final Integer coordY) {
		this.coordY = coordY;
	}

	/**
	 * Get the value from field Heure.<br>
	 *
	 * @return the value
	 */
	public Date getHeure() {
		return this.heure;
	}

	/**
	 * Set the value from field Heure.
	 *
	 * @param heure : the value to set
	 */
	public void setHeure(final Date heure) {
		this.heure = heure;
	}

	/**
	 * Get the value from field Statut.<br>
	 * DefaultValue : true<br/>
	 * InitialValue : true<br/>
	 *
	 * @return the value
	 */
	public Boolean getStatut() {
		return this.statut;
	}

	/**
	 * Set the value from field Statut.
	 *
	 * @param statut : the value to set
	 */
	public void setStatut(final Boolean statut) {
		this.statut = statut;
	}

	/**
	 * Get the defined label for the given Statut.
	 * 
	 * @param statut the field value
	 * @param context the current Context
	 * @return the label
	 */
	public static String getStatutLabel(Boolean statut, RequestContext context) {
		return EntityManager.getEntityModel(LocalisationConstants.ENTITY_NAME).getField(LocalisationConstants.Vars.STATUT).getDefinedLabel(statut, context.getUser().getLocale());
	}

	/**
	 * Get the value from field BaliseId.<br>
	 *
	 * @return the value
	 */
	public Long getBaliseId() {
		return this.baliseId;
	}

	/**
	 * Set the value from field BaliseId.
	 *
	 * @param baliseId : the value to set
	 */
	public void setBaliseId(final Long baliseId) {
		this.baliseId = baliseId;
	}

	/**
	 * Instance of Balise matching the link LocalisationRBalise based on foreign key values. <br/>
	 * <b>Warning : This method does not cache its results and each call imply a database access !!!</b>
	 * 
	 * @param ctx Current context
	 * @return Instance of Balise matching the link pays if any, null otherwise.
	 */
	public Balise getRef_BaliseFk(RequestContext ctx) {
		return (Balise) DB.getRef(this, LocalisationConstants.Links.LINK_LOCALISATION_R_BALISE, ctx);
	}
	
	/**
	 * This method sets all variables of foreign key in the current Localisation object to match the primary key of the Balise instance. <br/>
	 * If the Balise instance is null, all foreign key variables will be set to null. <br/>
	 * <b>This method does not access database and won't automatically update database link.</b>
	 * 
	 * @param pBalise The newly targeted bean of link LocalisationRBalise.
	 */
	public void setRef_BaliseFk(Balise pBalise) {
		Key primaryKey = null;
		if (pBalise != null) {
			primaryKey = pBalise.getPrimaryKey();
		}
		setForeignKey(LocalisationConstants.Links.LINK_LOCALISATION_R_BALISE, primaryKey);
	}

	
	@Override
	public Localisation clone() {
		Localisation clone = (Localisation) super.clone();
		clone.removeDefaultValues();
		for (String f : getModel().getFields()) {
			clone.invokeSetter(f, invokeGetter(f));
		}
		clone.resetLinksAndBackRefs();
		return clone;
	}

	@Override
	public void removeDefaultValues() {
		statut = null; 
	}

	/**
	 * Gets entity metadata (field names, field mandatory, database definition, business actions, etc. 
	 * @return	EntityModel class loaded from bean annotations
	 */
	public static EntityModel getEntityModel() {
		return EntityManager.getEntityModel(LocalisationConstants.ENTITY_NAME);
	}
}
