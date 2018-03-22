package com.cgi.commons.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.FileContainer;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.utils.DbException;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TmpFileManager;

/**
 * Specific DbManager to handle files properly.
 */
public class FileDbManager {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(FileDbManager.class);

	/** Current context. */
	private RequestContext ctx;
	/** Entity with the {@code BLOB} or {@code CLOB} variable. */
	private Entity entity;
	/** Entity definition. */
	private EntityModel model;
	/** {@code BLOB} or {@code CLOB} variable's name. */
	private String varName;
	/** Entity's primary key. */
	private Key pk;

	/**
	 * Creates a new manager.
	 * 
	 * @param ctx
	 *            Current context.
	 * @param entity
	 *            Entity with the {@code BLOB} or {@code CLOB} variable.
	 * @param varName
	 *            {@code BLOB} or {@code CLOB} variable's name.
	 */
	public FileDbManager(RequestContext ctx, Entity entity, String varName) {
		this.ctx = ctx;
		this.entity = entity;
		this.model = entity.getModel();
		this.varName = varName;
		this.pk = entity.getPrimaryKey();
	}

	/**
	 * @return {@code getFile(false)}
	 * @see #getFile(boolean)
	 */
	public FileContainer getFile() {
		return getFile(false);
	}

	/**
	 * Retrieves a file from the database.
	 * 
	 * @param appendName
	 *            Indicates whether the file name should be added to the content.
	 * @return A file container with {@code uuid} and {@code name} set or {@code null} if the file could not be found.
	 */
	public FileContainer getFile(boolean appendName) {
		String entityName = entity.name();
		String alias = "T01";
		DbQuery query = new DbQuery(entityName, alias);
		query.addColumn(varName, alias);
		query.addCondKey(pk, alias);

		try (DbManager manager = new DbManager(ctx, query)) {

			if (manager.next()) {
				ResultSet rs = manager.rs;
				int columnIndex = manager.getColumnIndex(entityName, varName);
				InputStream is;

				if (model.getField(varName).getSqlType() == SqlTypes.CLOB) {
					is = rs.getAsciiStream(columnIndex);

				} else {
					is = rs.getBinaryStream(columnIndex);
				}

				if (rs.wasNull()) {
					is = null;
				}
				return new TmpFileManager(is).createFile(true, appendName);
			}

		} catch (SQLException e) {
			String msg = "Error while getting file";
			LOGGER.error(msg, e);
			throw new DbException(msg, e);
		}
		return null;
	}

	/**
	 * Saves a file into the database.
	 */
	public void saveFile() {
		FileContainer container = (FileContainer) entity.invokeGetter(varName);

		if (container == null || container.getUuid() == null || container.getUuid().trim().isEmpty()) {
			return;
		}

		File file = TmpFileManager.getTemporaryFile(container.getUuid());

		if (!file.exists()) {
			LOGGER.warn("File not found : " + container.getUuid());
			return;
		}

		StringBuilder sqlBuilder = new StringBuilder("UPDATE ").append(table()).append(" SET ").append(column(varName))
				.append(" = ? ");

		Key pk = entity.getPrimaryKey();
		List<String> pkFields = pk.getModel().getFields();
		Iterator<String> iter = pkFields.iterator();
		sqlBuilder.append(" WHERE ").append(column(iter.next())).append(" = ? ");

		while (iter.hasNext()) {
			sqlBuilder.append(" AND ").append(column(iter.next())).append(" = ? ");
		}

		Connection connection = ctx.getDbConnection().getCnx();

		try (FileInputStream fis = new FileInputStream(file);
				PreparedStatement ps = connection.prepareStatement(sqlBuilder.toString())) {

			if (model.getField(varName).getSqlType() == SqlTypes.CLOB) {
				updateAsciiStream(file, fis, ps, 1);
			} else {
				updateBinaryStream(file, fis, ps, 1);
			}

			int i = 2;
			for (String fieldName : pkFields) {
				ps.setObject(i, pk.getValue(fieldName));
				i++;
			}
			ps.executeUpdate();

		} catch (IOException | SQLException e) {
			String msg = "Error while saving file";
			LOGGER.error(msg, e);
			throw new DbException(msg, e);
		} finally {
			// The temporary file is deleted
			// it may be optional using a new custom method to define the strategy.
			file.delete();
			// Delete the uuid into the container to avoid blob reset if an update is made onto the entity later.
			container.setUuid(null);
		}
	}

	/**
	 * Updates an ASCII stream using several tries.
	 * <ol>
	 * <li>Calls {@link PreparedStatement#setAsciiStream(int, InputStream, long)}</li>
	 * <li>If an {@code SQLFeatureNotSupportedException} is thrown, it tries with
	 * {@link PreparedStatement#setAsciiStream(int, InputStream, int)}</li>
	 * <li>If an {@code SQLFeatureNotSupportedException} is thrown, it tries with {@link PreparedStatement#setAsciiStream(int, InputStream)}</li>
	 * </ol>
	 * <p>
	 * This method exists because JDBC drivers may not implement all these methods.
	 * </p>
	 * 
	 * @param file
	 *            File to save into the database.
	 * @param inputStream
	 *            Open Input stream to read the file content.
	 * @param statement
	 *            Current open prepared statement.
	 * @param index
	 *            Index of the column to update.
	 * @throws SQLException
	 *             If an error occurs while accessing/writing to the database.
	 */
	private void updateAsciiStream(File file, FileInputStream inputStream, PreparedStatement statement, int index) throws SQLException {
		try {
			statement.setAsciiStream(index, inputStream, file.length());

		} catch (SQLFeatureNotSupportedException e) {

			try {
				statement.setAsciiStream(index, inputStream, (int) file.length());

			} catch (SQLFeatureNotSupportedException e2) {
				statement.setAsciiStream(index, inputStream);
			}
		}
	}

	/**
	 * Updates a binary stream using several tries.
	 * <ol>
	 * <li>Calls {@link PreparedStatement#setBinaryStream(int, InputStream, long)}</li>
	 * <li>If an {@code SQLFeatureNotSupportedException} is thrown, it tries with
	 * {@link PreparedStatement#setBinaryStream(int, InputStream, int)}</li>
	 * <li>If an {@code SQLFeatureNotSupportedException} is thrown, it tries with
	 * {@link PreparedStatement#setBinaryStream(int, InputStream)}</li>
	 * </ol>
	 * <p>
	 * This method exists because JDBC drivers may not implement all these methods.
	 * </p>
	 * 
	 * @param file
	 *            File to save into the database.
	 * @param inputStream
	 *            Open Input stream to read the file content.
	 * @param statement
	 *            Current open prepared statement.
	 * @param index
	 *            Index of the column to update.
	 * @throws SQLException
	 *             If an error occurs while accessing/writing to the database.
	 */
	private void updateBinaryStream(File file, FileInputStream inputStream, PreparedStatement statement, int index) throws SQLException {
		try {
			statement.setBinaryStream(index, inputStream, file.length());

		} catch (SQLFeatureNotSupportedException e) {

			try {
				statement.setBinaryStream(index, inputStream, (int) file.length());

			} catch (SQLFeatureNotSupportedException e2) {
				statement.setBinaryStream(index, inputStream);
			}
		}
	}

	/**
	 * Saves the files linked to the given entity. This method performs the following instructions :
	 * <ul>
	 * <li>It retrieves the field names of the {@code BLOB} or {@code CLOB} variables stored into the database.</li>
	 * <li>For each field, it attempts to get the associated {@code fileContainer}.</li>
	 * <li>If the fileContainer is not {@code null} and its name is set, it attempts to update the database with the file stored into the
	 * temporary directory.</li>
	 * </ul>
	 * 
	 * @param bean
	 *            Updated entity.
	 * @param ctx
	 *            Current context.
	 * @see #saveFile()
	 */
	public static void saveFiles(Entity bean, RequestContext ctx) {
		Key pk = bean.getPrimaryKey();

		if (!pk.isFull()) {
			return;
		}

		for (String varName : getFileFields(bean)) {
			FileDbManager manager = new FileDbManager(ctx, bean, varName);
			manager.saveFile();
		}
	}

	/**
	 * @param bean
	 *            An entity.
	 * @return A list of variables which are stored into the database with type {@code BLOB} or {@code CLOB}; or an empty list if the given
	 *         entity does not have such variables.
	 */
	private static List<String> getFileFields(Entity bean) {
		List<String> result = new ArrayList<>();
		EntityModel model = bean.getModel();

		for (String fieldName : model.getFields()) {
			EntityField field = model.getField(fieldName);
			SqlTypes type = field.getSqlType();

			if (field.isFromDatabase() && (SqlTypes.BLOB == type || SqlTypes.CLOB == type)) {
				result.add(fieldName);
			}
		}
		return result;
	}

	/**
	 * @return The complete table name corresponding to the current entity, aka {@code [schemaName.]tableName}.
	 * @see #addDoubleQuotes(String)
	 */
	private String table() {
		String identifier;
		String schemaName = null;
		String schemaId = entity.getModel().getDbSchemaName();

		if (schemaId != null && !schemaId.isEmpty()) {
			schemaName = MessageUtils.getServerProperty("schema." + schemaId);
		}
		if (schemaName == null || schemaName.isEmpty()) {
			// try default schema if any
			schemaName = MessageUtils.getServerProperty("schema.default");
		}

		if (schemaName != null && !schemaName.isEmpty()) {
			identifier = new StringBuilder(schemaName).append(".").append(model.dbName()).toString();
		} else {
			identifier = model.dbName();
		}
		return addDoubleQuotes(identifier);
	}

	/**
	 * @param varName
	 *            Variable's name.
	 * @return The name of the column corresponding to the given variable name.
	 * @see #addDoubleQuotes(String)
	 */
	private String column(String varName) {
		return addDoubleQuotes(model.getField(varName).getSqlName());
	}

	/**
	 * Adds double quotation marks ({@code "}) to the given identifier if the context's database is Oracle.
	 * <p>
	 * This method is used to prevent {@code ORA-01747 (invalid user.table.column, table.column, or column specification)} error while an
	 * identifier uses a reserved keyword. It always adds double quotes for Oracle, it does not make any lookup.
	 * </p>
	 * 
	 * @param identifier
	 *            Identifier (schema, table or column identifier). It must not be {@code null}.
	 * @return {@code "identifier"} if the database is Oracle; {@code identifier} otherwise.
	 * @throws NullPointerException
	 *             If {@code identifier} is {@code null}.
	 */
	private String addDoubleQuotes(String identifier) {
		if (DbConnection.getDbType() == DbConnection.Type.ORACLE) {
			return identifier.replaceAll("([^.]+)", "\"$1\"");
		}
		return identifier;
	}

}
