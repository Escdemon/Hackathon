package com.cgi.models.beans;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

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
import com.cgi.models.constants.BaliseConstants;

import static com.cgi.commons.ref.entity.Action.Input.*;

/**
 * Entity Balise definition.
 */
@EntityDef(dbName = "BALISE", primaryKey = { "id" })
@Links({
})
@Actions({
	@Action(code = "ATTACH", input = MANY, persistence = Persistence.INSERT, ui = UserInterface.OUTPUT, process =  com.cgi.commons.ref.entity.Action.Process.LINK),
	@Action(code = "DETACH", input = MANY, persistence = Persistence.DELETE, ui = UserInterface.OUTPUT, process =  com.cgi.commons.ref.entity.Action.Process.LINK),
	@Action(code = "CREATE", input = NONE, persistence = Persistence.INSERT),
	@Action(code = "UPDATE", persistence = Persistence.UPDATE),
	@Action(code = "DISPLAY", persistence = Persistence.NONE, ui = UserInterface.READONLY),
	@Action(code = "DELETE", persistence = Persistence.DELETE, ui = UserInterface.OUTPUT),
	@Action(code = "LIST", input = QUERY, persistence = Persistence.NONE, ui = UserInterface.OUTPUT)
})
public class Balise extends Entity implements Serializable {
	/** Serial id. */
	public static final long serialVersionUID = 1L;

	/** Technical ID. */
	@EntityField(sqlName = "ID", sqlType = SqlTypes.DECIMAL, sqlSize = 19, isMandatory = true, isAutoIncrementField = true)
	private Long id;

	/** Nom. */
	@EntityField(sqlName = "NOM", sqlType = SqlTypes.VARCHAR2, sqlSize = 20, defaultValue = "Opérateur")
	private String nom = "Opérateur";

	/** Description. */
	@EntityField(sqlName = "W$_DESC", sqlType = SqlTypes.VARCHAR2, sqlSize = 40, memory =  com.cgi.commons.ref.entity.EntityField.Memory.SQL, sqlExpr = ":tableAlias.NOM")
	private String internalCaption;

	/**
	 * Initialize a new Balise.<br/>
	 * <b>The fields with initial value will be populated.</b>
	 */
	public Balise() {
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
	public Balise(Long id) {
		super();
		Key primaryKey = buildPrimaryKey(id);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Initialize a new Balise from an existing Balise.<br/>
	 * <b>All fields value are copied.</b>
	 *
	 * @param pBalise The existing Balise.
	 */
	public Balise(Balise pBalise) {
		super(pBalise);
	}

	/**
	 * Generate a primary key for the entity.
	 *
	 * @param id ID.
	 * @return The key.
	 */
	public static Key buildPrimaryKey(Long id) {
		KeyModel pkModel = new KeyModel(BaliseConstants.ENTITY_NAME);
		Key key = new Key(pkModel);
		key.setValue(BaliseConstants.Vars.ID, id);

		return key;
	}

	@Override
	public String name() {
		return BaliseConstants.ENTITY_NAME;
	}

	@Override
	public String description() {
		return getInternalCaption(); 
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
	 * Get the value from field Nom.<br>
	 * DefaultValue : Opérateur<br/>
	 * InitialValue : Opérateur<br/>
	 *
	 * @return the value
	 */
	public String getNom() {
		return this.nom;
	}

	/**
	 * Set the value from field Nom.
	 *
	 * @param nom : the value to set
	 */
	public void setNom(final String nom) {
		this.nom = nom;
	}

	/**
	 * Gets the value from field InternalCaption. This getter respects real Java naming convention. 
	 *
	 * @return the value
	 */
	public String getw$Desc() {
		return getInternalCaption();
	}

	/**
	 * Sets the value from field InternalCaption. This setter respects real Java naming convention
	 *
	 * @param internalCaption : the value to set
	 */
	public void setw$Desc(final String internalCaption) {
		setInternalCaption(internalCaption); 
	}

	/**
	 * Get the value from field InternalCaption.<br>
	 * Memory var : SQL Expresion<br>
	 *
	 * @return the value
	 */
	public String getInternalCaption() {
		return this.internalCaption;
	}

	/**
	 * Set the value from field InternalCaption.
	 *
	 * @param internalCaption : the value to set
	 */
	public void setInternalCaption(final String internalCaption) {
		this.internalCaption = internalCaption;
	}

	/**
	 * This methods gets all instances of Localisation back referenced by the current Balise instance via link LocalisationRBalise. <br/>
	 * <b>Warning: this method does not cache its results and will connect to database on every call.</b> <br/>
	 * <i>Note: if the PK is incomplete, an empty set will be returned.</i>
	 * 
	 * @param ctx Current context with open database connection.
	 * @return Set containing instances for every Localisation related to the current Balise via link LocalisationRBalise.
	 */
	public Set<Localisation> getList_LocalisationRBalise(RequestContext ctx) {
		Set<Localisation> s = new HashSet<Localisation>();
		if (this.getPrimaryKey() == null || !this.getPrimaryKey().isFull()) {
			// Do not get linked entities if PK is incomplete
			return s;
		}
		for (Entity e : DB.getLinkedEntities(this, BaliseConstants.Links.LINK_LOCALISATION_R_BALISE, ctx)) {
			s.add((Localisation) e);
		}
		return s;
	}

	
	@Override
	public Balise clone() {
		Balise clone = (Balise) super.clone();
		clone.removeDefaultValues();
		for (String f : getModel().getFields()) {
			clone.invokeSetter(f, invokeGetter(f));
		}
		clone.resetLinksAndBackRefs();
		return clone;
	}

	@Override
	public void removeDefaultValues() {
		nom = null; 
	}

	/**
	 * Gets entity metadata (field names, field mandatory, database definition, business actions, etc. 
	 * @return	EntityModel class loaded from bean annotations
	 */
	public static EntityModel getEntityModel() {
		return EntityManager.getEntityModel(BaliseConstants.ENTITY_NAME);
	}
}
