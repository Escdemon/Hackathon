package com.cgi.models.beans;

import java.io.Serializable;
import java.util.Arrays;

import com.cgi.commons.ref.entity.Action.*;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.ref.entity.EntityManager;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.FileContainer;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.KeyModel;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.db.DB;
import com.cgi.commons.ref.annotations.*;
import com.cgi.models.constants.PhotoConstants;

import static com.cgi.commons.ref.entity.Action.Input.*;

/**
 * Entity Photo definition.
 */
@EntityDef(dbName = "PHOTO", primaryKey = { "id" })
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
public class Photo extends Entity implements Serializable {
	/** Serial id. */
	public static final long serialVersionUID = 1L;

	/** Technical ID. */
	@EntityField(sqlName = "ID", sqlType = SqlTypes.DECIMAL, sqlSize = 19, isMandatory = true, isAutoIncrementField = true)
	private Long id;

	/** Image. */
	@EntityField(sqlName = "IMAGE", sqlType = SqlTypes.BLOB, sqlSize = 0)
	private FileContainer image;

	/**
	 * Initialize a new Photo.<br/>
	 * <b>The fields with initial value will be populated.</b>
	 */
	public Photo() {
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
	public Photo(Long id) {
		super();
		Key primaryKey = buildPrimaryKey(id);
		setPrimaryKey(primaryKey);
	}
	
	/**
	 * Initialize a new Photo from an existing Photo.<br/>
	 * <b>All fields value are copied.</b>
	 *
	 * @param pPhoto The existing Photo.
	 */
	public Photo(Photo pPhoto) {
		super(pPhoto);
	}

	/**
	 * Generate a primary key for the entity.
	 *
	 * @param id ID.
	 * @return The key.
	 */
	public static Key buildPrimaryKey(Long id) {
		KeyModel pkModel = new KeyModel(PhotoConstants.ENTITY_NAME);
		Key key = new Key(pkModel);
		key.setValue(PhotoConstants.Vars.ID, id);

		return key;
	}

	@Override
	public String name() {
		return PhotoConstants.ENTITY_NAME;
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
	 * Get the value from field Image.<br>
	 *
	 * @return the value
	 */
	public FileContainer getImage() {
		return this.image;
	}

	/**
	 * Set the value from field Image.
	 *
	 * @param image : the value to set
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
	/**
	 * Loads binary content of image from database into the FileContainer.
	 * 
	 * @param context Current request context
	 */
	public void loadImage(RequestContext context) {
		this.loadLobVariable(PhotoConstants.Vars.IMAGE, context);
	}

	
	@Override
	public Photo clone() {
		Photo clone = (Photo) super.clone();
		clone.removeDefaultValues();
		for (String f : getModel().getFields()) {
			clone.invokeSetter(f, invokeGetter(f));
		}
		clone.resetLinksAndBackRefs();
		return clone;
	}

	@Override
	public void removeDefaultValues() {
	}

	/**
	 * Gets entity metadata (field names, field mandatory, database definition, business actions, etc. 
	 * @return	EntityModel class loaded from bean annotations
	 */
	public static EntityModel getEntityModel() {
		return EntityManager.getEntityModel(PhotoConstants.ENTITY_NAME);
	}
}
