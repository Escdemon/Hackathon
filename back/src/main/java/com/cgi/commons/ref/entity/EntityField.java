package com.cgi.commons.ref.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.cgi.commons.utils.MessageUtils;

/**
 * Field of an Entity.
 */
public class EntityField {

	/** Supported SQL data types. */
	public enum SqlTypes {
		/** Blob. */
		BLOB,
		/** Boolean. */
		BOOLEAN,
		/** Char. */
		CHAR,
		/** Clob. */
		CLOB,
		/** Date. */
		DATE,
		/** Decimal. */
		DECIMAL,
		/** Integer. */
		INTEGER,
		/** Time. */
		TIME,
		/** Timestamp. */
		TIMESTAMP,
		/** Varchar. */
		VARCHAR,
		/** Varchar 2. */
		VARCHAR2
	}

	/** Behavior for in-memory (calculated) variables. */
	public enum Memory {
		/** Not an in-memory variable : persisted in database. */
		NO,
		/** Always recaculated each time we get a chance. */
		ALWAYS,
		/** Never calculated : used to pass parameters to custom actions. */
		NEVER,
		/** SQL scalar expression. */
		SQL
	}

	/** SQL field name. */
	private final String sqlName;
	/** SQL type. */
	private final SqlTypes sqlType;
	/** SQL field size. */
	private int sqlSize = -1;
	/** SQL decimal accuracy. */
	private int sqlAccuracy = -1;
	/** Memory variable type. */
	private Memory memory;
	/** SQL expression. */
	private String sqlExpr;
	/** Default value. */
	private Object defaultValue;

	/** Indicates if the field is mandatory. */
	private boolean isMandatory = false;
	/** Indicates if the field can be lookup. */
	private boolean isLookupField;

	/** List of defined values. */
	private final List<DefinedValue> definedValues;

	/**
	 * Constructor.
	 * 
	 * @param sqlName SQL field name.
	 * @param sqlType SQL type.
	 * @param sqlSize SQL field size.
	 * @param sqlAccuracy SQL decimal accuracy.
	 * @param memory Memory variable type.
	 * @param isMandatory Indicates if the field is mandatory.
	 * @param isLookupField Indicates if the field can be lookup.
	 */
	public EntityField(String sqlName, SqlTypes sqlType, int sqlSize, int sqlAccuracy, Memory memory, boolean isMandatory, boolean isLookupField) {
		super();
		this.sqlName = sqlName;
		this.sqlType = sqlType;
		this.sqlSize = sqlSize;
		this.sqlAccuracy = sqlAccuracy;
		this.memory = memory;
		this.isMandatory = isMandatory;
		this.isLookupField = isLookupField;
		this.definedValues = new ArrayList<DefinedValue>();
	}

	/**
	 * Getter for the SQL field name.
	 * @return The SQL field name.
	 */
	public String getSqlName() {
		return sqlName;
	}

	/**
	 * Getter for the SQL type.
	 * @return The SQL type.
	 */
	public SqlTypes getSqlType() {
		return sqlType;
	}

	/**
	 * Getter for the SQL field size.
	 * @return The SQL field size.
	 */
	public int getSqlSize() {
		return sqlSize;
	}

	/**
	 * Getter for the SQL decimal accuracy.
	 * @return The SQL decimal accuracy.
	 */
	public int getSqlAccuracy() {
		return sqlAccuracy;
	}

	/**
	 * Return Memory variable type.
	 * @return Memory variable type.
	 */
	public Memory getMemory() {
		return memory;
	}

	/** 
	 * Returns <code>true</code> for in-memory (calculated) variables. 
	 * @return <code>true</code> for in-memory (calculated) variables. 
	 */
	public boolean isTransient() {
		return memory != Memory.NO;
	}

	/** 
	 * Returns <code>true</code> for SQL variables (either true columns or SQL expression).
	 * @return <code>true</code> for SQL variables (either true columns or SQL expression).
	 */
	public boolean isFromDatabase() {
		return memory == Memory.NO || memory == Memory.SQL;
	}

	/**
	 * Indicates if the field is mandatory.
	 * @return <code>true</code> if the field is mandatory.
	 */
	public boolean isMandatory() {
		return isMandatory;
	}
	
	/**
	 * Indicates if the field is lookup.
	 * @return <code>true</code> if the field is lookup.
	 */
	public boolean isLookupField() {
		return isLookupField;
	}

	/**
	 * Returns the list of defined values.
	 * @return the list of defined values.
	 */
	public List<DefinedValue> getDefinedValues() {
		return definedValues;
	}

	/**
	 * Returns the list of old values.
	 * @return the list of old values.
	 */
	public List<Object> getOldValues() {
		List<Object> values = new ArrayList<Object>();
		for (DefinedValue defVal : definedValues)
			values.add(defVal.getValue());

		return values;
	}

	/**
	 * Returns the value of the "index" defined value.
	 * @param index The index in the list of defined values.
	 * @return the value.
	 */
	public Object getDefValValue(int index) {
		return definedValues.get(index).getValue();
	}

	/**
	 * Returns the label of the "index" defined value.
	 * @param index The index in the list of defined values.
	 * @return the label.
	 */
	public String getDefValLabel(int index) {
		return definedValues.get(index).getLabel();
	}

	/**
	 * Returns the default value.
	 * @return the default value.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Setter for the default value.
	 * @param defaultValue the default value.
	 */
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Indicates if the Field has defined values.
	 * @return <code>true</code> if the Field has defined values.
	 */
	public boolean hasDefinedValues() {
		return (definedValues.size() > 0);
	}

	/**
	 * Indicates if the Field has null as defined value.
	 * 
	 * @return <code>true</code> if the Field has NO defined values or has null as defined value;<br>
	 *         <code>false</code> if the Field doesn't have null as defined value
	 */
	public boolean hasNullAsDefinedValues() {
		if (hasDefinedValues()) {
			for (DefinedValue defVal : definedValues) {
				if (defVal.getValue() == null) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * Returns the number of defined values.
	 * @return The number of defined values.
	 */
	public int nbDefinedValues() {
		return definedValues.size();
	}

	/**
	 * Get the label for a defined value.
	 * @param value The value of the defined value.
	 * @param l The Locale.
	 * @return The Label.
	 */
	public String getDefinedLabel(Object value, Locale l) {
		for (DefinedValue defVal : definedValues) {
			if (value == null && defVal.getValue() == null) {
				return MessageUtils.getInstance(l).getGenLabel(defVal.getLabel(), (Object[]) null);
			} else if (defVal.getValue() == null) {
				continue;
			}
			if (defVal.getValue().equals(value)) {
				return MessageUtils.getInstance(l).getGenLabel(defVal.getLabel(), (Object[]) null);
			}
		}
		return null;
	}

	/**
	 * Get the label for a defined value.
	 * @param code The code of the defined value.
	 * @param l The Locale.
	 * @return The Label.
	 */
	public String getDefLabel(String code, Locale l) {
		for (DefinedValue defVal : definedValues) {
			if (defVal.getCode().equals(code)) {
				return MessageUtils.getInstance(l).getGenLabel(defVal.getLabel(), (Object[]) null);
			}
		}
		return null;
	}

	/**
	 * Indicates if a value is a defined value.
	 * @param value The Value.
	 * @return <code>true</code> if the value is a defined value.
	 */
	public boolean isDefValue(Object value) {
		for (DefinedValue defVal : definedValues) {
			if (value == null && defVal.getValue() == null) {
				return true;
			} else if (defVal.getValue() == null) {
				continue;
			}
			if (defVal.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Indicates if a code is a defined value.
	 * @param code The Code.
	 * @return <code>true</code> if the code is a defined value.
	 */
	public boolean isDefCode(String code) {
		for (DefinedValue defVal : definedValues) {
			if (defVal.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the value of a defined value by his code.
	 * @param code The Code.
	 * @return The value.
	 */
	public String getDefValue(String code) {
		for (DefinedValue defVal : definedValues) {
			if (defVal.getCode().equals(code)) {
				return defVal.getValue().toString();
			}
		}
		return null;
	}

	/**
	 * Get a boolean defined value by his code.
	 * @param code The Code.
	 * @return The value.
	 */
	public Boolean getBooleanDefValue(String code) {
		if (code == null) {
			return (Boolean) getDefaultValue();
		}
		for (DefinedValue defVal : definedValues) {
			if (defVal.getCode().equals(code)) {
				return (Boolean) defVal.getValue();
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Get SQL Expression.
	 * @return SQL Expression.
	 */
	public String getSqlExpr() {
		return sqlExpr;
	}

	/**
	 * Set the SQL Expression.
	 * @param sqlExpr the SQL Expression.
	 */
	public void setSqlExpr(String sqlExpr) {
		this.sqlExpr = sqlExpr;
	}

	/**
	 * Indicates if the field is an alphanumeric field (VARCHAR2, CHAR or CLOB).
	 * @return <code>true</code> if the field is an alphanumeric field.
	 */
	public boolean isAlpha() {
		return SqlTypes.VARCHAR2.equals(sqlType) || SqlTypes.CHAR.equals(sqlType) || SqlTypes.CLOB.equals(sqlType);
	}

}
