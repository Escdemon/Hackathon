package com.cgi.commons.db;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.cgi.commons.db.DbConnection.Type;
import com.cgi.commons.db.DbQuery.Cond;
import com.cgi.commons.db.DbQuery.Const;
import com.cgi.commons.db.DbQuery.JoinedLink;
import com.cgi.commons.db.DbQuery.Table;
import com.cgi.commons.db.DbQuery.Var;
import com.cgi.commons.ref.Constants;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField;
import com.cgi.commons.ref.entity.EntityField.Memory;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.utils.ApplicationUtils;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.reflect.DomainUtils;

/**
 * Build a SQL query.
 */
public class SqlBuilder {

	/** Logger. */
	protected static final Logger LOGGER = Logger.getLogger(SqlBuilder.class);

	/** Query. */
	protected DbQuery dbQuery;
	/** Where on join. */
	protected String whereJoin = "";
	/** Indicates if sort is done. */
	protected boolean sortDone = false; // SQL Server only, sort must be done in the row_number partition
	// parent query to build a sub query
	protected DbQuery parentQuery;

	/** Operators. */
	public enum SqlOp {
		/** Equal. */
		OP_EQUAL("=", 1),
		/** Different. */
		OP_N_EQUAL("<>", 1),
		/** Greater than. */
		OP_GREATER(">", 1),
		/** Lesser than. */
		OP_LESS("<", 1),
		/** Greater or equal. */
		OP_GREATER_OR_EQUAL(">=", 1),
		/** Lesser or equal. */
		OP_LESS_OR_EQUAL("<=", 1),
		/** In. */
		OP_IN("IN", 1),
		/** Not in. */
		OP_N_IN("NOT IN", 1),
		/** Like. */
		OP_LIKE("LIKE", 1),
		/** Not Like. */
		OP_N_LIKE("NOT LIKE", 1),
		/** Is Null. */
		OP_ISNULL("IS NULL", 0),
		/** Is Not Null. */
		OP_N_ISNULL("IS NOT NULL", 0),
		/** Between. */
		OP_BETWEEN("BETWEEN", 2);

		/** Value. */
		public final String val;
		public final int nbOps;

		/**
		 * Constructor.
		 * 
		 * @param value
		 *            Value.
		 */
		private SqlOp(String value, int ops) {
			val = value;
			nbOps = ops;
		}
	}

	/** Type SQL devant recevoir un UPPER. */
	public static final List<SqlTypes> STRING_SQL_TYPES = new ArrayList<SqlTypes>();
	static {
		STRING_SQL_TYPES.add(SqlTypes.VARCHAR2);
		STRING_SQL_TYPES.add(SqlTypes.VARCHAR);
		STRING_SQL_TYPES.add(SqlTypes.CHAR);
	}

	/** AND keyword */
	public static final String AND_COND = " AND ";
	/** OR keyword */
	public static final String OR_COND = " OR ";

	/**
	 * Constructor.
	 * 
	 * @param query
	 *            The DbQuery.
	 */
	public SqlBuilder(DbQuery query) {
		dbQuery = query;
	}

	/**
	 * Génération de la requête permettant de récupérer le prochain ID d'une séquence.
	 * 
	 * @param entity
	 *            The Entity
	 * @return SQL query
	 */
	public static String getNextSequenceIdQuery(Entity entity) {
		String dbName = DomainUtils.createDbName(entity.getClass().getSimpleName());
		return "select " + Constants.EXTENSION_SEQUENCE + dbName + ".nextVal from dual";
	}

	/**
	 * Génération de la requête permettant de récupérer le prochain ID d'une séquence.
	 * 
	 * @param sequenceName
	 *            Name of the sequence to use
	 * @return SQL query
	 */
	public static String getNextSequenceIdQuery(String sequenceName) {
		return "select " + Constants.EXTENSION_SEQUENCE + sequenceName + ".nextVal from dual";
	}

	/**
	 * Generation de la clause Select.
	 * 
	 * @param query
	 *            The Query.
	 * @param index
	 *            The Index.
	 * @return The Clause.
	 */
	protected String generateSelectClause() {
		String query = "SELECT ";

		if (dbQuery.distinct) {
			query = query.concat("DISTINCT ");
		}

		// Si on doit sélectionner toutes les variables ou qu'il n'y a aucune
		// variable de sortie définie, on sélectionne * sur toutes les tables
		// (T1.*, T2.*, etc.)
		if (dbQuery.forUpdate || dbQuery.outVars.size() == 0) {
			for (int i = 0; i < dbQuery.tables.size(); i++) {
				Table table = dbQuery.tables.get(i);
				if (i > 0) {
					query = query.concat(", ");
				}
				// all variables
				query = query.concat(table.alias + ".*");
			}
		} else {
			// Sinon, on ne sélectionne que les variables définies dans
			// outVars.
			StringBuilder selectClause = new StringBuilder();
			int index = 1;
			for (int i = 0; i < dbQuery.outVars.size(); i++) {
				Var outVar = dbQuery.outVars.get(i);
				if (!outVar.model.isFromDatabase()) {
					// Not a SQL variable, skip it
					continue;
				}
				if (selectClause.length() > 0) {
					selectClause.append(", ");
				}

				if (null != outVar.expr) {
					selectClause.append(outVar.expr);
				} else {
					selectClause.append(outVar.tableId);
					selectClause.append(".");
					selectClause.append(outVar.model.getSqlName());
				}
				dbQuery.indexes.put(outVar.tableId + "_" + outVar.model.getSqlName(), Integer.valueOf(index++));
				selectClause.append(" as ");
				selectClause.append(dbQuery.aliash(outVar));
			}
			query = query.concat(selectClause.toString());
			for (int i = 0; i < dbQuery.outConsts.size() && !dbQuery.count; i++) {
				Const c = dbQuery.outConsts.get(i);

				if (i > 0 || dbQuery.outVars.size() > 0) {
					query = query.concat(", ");
				}
				query = query.concat("'" + c.value + "' as " + c.name);
			}
		}
		return query;
	}

	/**
	 * Méthode getPrivateTable.
	 * 
	 * @param entity
	 *            The Entity.
	 * @return The Table.
	 */
	protected Table getPrivateTable(Entity entity) {
		for (Table t : dbQuery.tables) {
			if (t.entity.equals(entity)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Generation des clauses Join pour une table.
	 * 
	 * @param table
	 *            The Table.
	 */
	protected void generateJoinString(Table table) {
		String dstJoin = "";
		if (DbConnection.getDbType() == Type.ORACLE) {
			if (table.bExtJoin) {
				dstJoin = "(+)";
			} else if (getPrivateTable(table.entity) != null && getPrivateTable(table.entity).isOuterJoined) {
				dstJoin = "(+)";
			}
		} else {
			if (table.bExtJoin) {
				table.join = "LEFT JOIN";
				table.isOuterJoined = true;
			} else {
				table.join = "JOIN";
			}
		}
		table.joinCond = "";

		if (!dstJoin.equals("")) {
			table.isOuterJoined = true;
		}

		for (JoinedLink link : table.joinedLinks) {
			List<String> srcKeyList = link.srcKey.getFields();
			List<String> dstKeyList = link.dstKey.getFields();
			for (int i = 0; i < srcKeyList.size(); i++) {
				if (needAndOperator(table.joinCond)) {
					table.joinCond = table.joinCond.concat(AND_COND);
				}

				String srcKeyExt = link.srcEntity.getModel().getField(srcKeyList.get(i)).getSqlName();
				String dstKeyExt = link.dstEntity.getModel().getField(dstKeyList.get(i)).getSqlName();

				table.joinCond = table.joinCond.concat(link.srcAlias + "." + srcKeyExt + " = " + table.alias + "." + dstKeyExt + dstJoin);
			}

			// Add user defined join cond
			if (!table.userJoinConds.isEmpty()) {
				for (Cond c : table.userJoinConds) {
					table.joinCond = addCond(c, table.joinCond);
				}
			}

			// move join conditions to where clause
			if (DbConnection.getDbType() == Type.ORACLE) {
				if (needAndOperator(whereJoin)) {
					whereJoin += AND_COND;
				}
				whereJoin += table.joinCond;
				table.joinCond = "";
			}
		}
	}

	/**
	 * Generation de la clause From.
	 * 
	 * @return The Clause.
	 */
	protected String generateFromClause() {
		String query = "";
		query = query.concat(" FROM ");
		whereJoin = "";
		List<Table> tablesToAdd;

// see fixme for filterTables()
//		if (dbQuery.count) {
//			tablesToAdd = dbQuery.filterTables();
//		} else {
		tablesToAdd = dbQuery.tables;
//		}
		for (int i = 0; i < tablesToAdd.size(); i++) {

			if (i > 0) {
				if (DbConnection.getDbType() == Type.ORACLE) {
					query = query.concat(",");
				}
			}
			Table table = tablesToAdd.get(i);
			generateJoinString(table);

			if (table.joinCond.trim().length() > 0) {
				query += " " + table.join + " ";
			} else if (i > 0 && DbConnection.getDbType() != Type.ORACLE) {
				// cartesian product, join condition must be into the where clause
				query += ", ";
			}
			String schemaName = null;
			String schemaId = table.entity.getModel().getDbSchemaName();
			if (!schemaId.isEmpty())
				schemaName = MessageUtils.getServerProperty("schema." + schemaId);
			if (schemaName == null || schemaName.isEmpty())
				// try default schema if any
				schemaName = MessageUtils.getServerProperty("schema.default");

			if (schemaName != null && !schemaName.isEmpty()) {
				query = query.concat(schemaName + "." + table.extern + " " + table.alias);
			} else {
				query = query.concat(table.extern + " " + table.alias);
			}
			if (table.joinCond.length() > 0) {
				query = query.concat(" ON " + table.joinCond);
			}
		}
		return query;
	}

	/**
	 * Génération de la clause where.
	 * 
	 * @return The Clause.
	 */
	protected String generateWhereClause() {
		String whereClause = "";
		if (!dbQuery.whereConds.isEmpty() || whereJoin.length() > 0) {
			for (Cond cond : dbQuery.whereConds) {
				whereClause = addCond(cond, whereClause);
			}
		}

		if (!whereClause.isEmpty())
			whereClause = " where " + whereClause;

		if (!whereJoin.isEmpty()) {
			if (whereClause.isEmpty())
				whereClause = " where " + whereJoin;
			else
				whereClause += AND_COND + "(" + whereJoin + ")";
		}

		return whereClause;
	}

	protected String generateGroupBy() {
		// **** group by induced by expression use in select clause
		String groupBy = addGroupByFromSelect();
		String havingClause = "";

		for (Cond cond : dbQuery.havingConds) {
			switch (cond.type) {
			case IS_SEPARATOR:
				havingClause = addSeparator(cond, havingClause);
				break;
			default:
				havingClause = addHavingCond(cond.colAliases.get(0), cond.tableAliases.get(0), cond.op,
						cond.valuesList.get(0), cond.valuesList.get(1), havingClause, groupBy);
				break;
			}
		}
		// **** group by defined in query
		for (Cond cond : dbQuery.groupByConds) {
			Var outVar = dbQuery.findOutVar(cond.colAliases.get(0), cond.tableAliases.get(0));
			groupBy = addGroupByElement(groupBy, outVar);
		}

		if (!groupBy.isEmpty()) {
			groupBy = " group by " + groupBy;
			if (!havingClause.isEmpty())
				groupBy += " having " + havingClause;
		}
		return groupBy;
	}

	/**
	 * Renvoie la requête SQL.
	 * 
	 * @return The SQL Query.
	 */
	public static String toSql(DbQuery dbQuery) {
		return ApplicationUtils.getApplicationLogic().getSqlBuilder(dbQuery).toSql();
	}

	/**
	 * Get the SQL for a sub-query.<br>
	 * We need the parent query to control references to its columns.
	 * 
	 * @param parentQuery
	 * @param subQuery
	 * @return The SQL query
	 */
	private static String toSql(DbQuery parentQuery, DbQuery subQuery) {
		SqlBuilder b = ApplicationUtils.getApplicationLogic().getSqlBuilder(subQuery);
		b.parentQuery = parentQuery;
		return b.toSql();
	}

	/**
	 * Renvoie la requête SQL.
	 * 
	 * DB2 row selection: https://www.ibm.com/developerworks/community/blogs/SQLTips4DB2LUW/entry/limit_offset?lang=en
	 * SELECT internal$1.* FROM (
	 *   SELECT internal$2.*, ROW_NUMBER() OVER (ORDER BY T1_GVPE01 ASC) AS internal$rownum FROM (
	 *     SELECT T1.GVPERG as T1_GVPERG, T1.GVPE01 as T1_GVPE01, T1.GVPE02 as T1_GVPE02 FROM GVPED0 T1
	 *   ) internal$2
	 * ) internal$1 WHERE internal$1.internal$rownum > 0 AND internal$1.internal$rownum <= 200;				
	 * 
	 * @return The SQL Query.
	 */
	public String toSql() {
		dbQuery.indexes.clear();
		dbQuery.bindValues = new ArrayList<Object>();

		String query = "";

		// **** opening the outer query for counting rows or paging results
		if (dbQuery.count) {
			query = "SELECT COUNT(1) FROM (";

		} else if (dbQuery.maxRownum > 0) {
			if (DbConnection.getDbType() == Type.DB2) {
				query = "SELECT internal$1.* FROM (SELECT internal$2.*, ROW_NUMBER() OVER (";
				if (!dbQuery.sortVars.isEmpty()) {
					query += "ORDER BY " + dbQuery.getOrderByClause();
				}
				query += ") AS internal$rownum FROM (";
				sortDone = true;

			} else if (DbConnection.getDbType() == Type.ORACLE) {
				query = "SELECT * FROM (SELECT sub.*, rownum as ROWNUM_ROWNUM FROM (";

			} else if (DbConnection.getDbType() == Type.SQLSERVER) {
				query = "SELECT * FROM (SELECT sub.*, ROW_NUMBER() OVER (";
				if (dbQuery.sortVars.isEmpty()) {
					query += "ORDER BY (SELECT NULL)";
				} else {
					query += "ORDER BY " + dbQuery.getOrderByClause();
				}
				query += ") AS ROWNUM FROM (";
				sortDone = true;
			}
		}

		// **** build original query
		query += generateSelectClause();
		query += generateFromClause();
		query += generateWhereClause();
		query += generateGroupBy();
		
		if (!sortDone && !dbQuery.count && !dbQuery.sortVars.isEmpty()) {
			// add order by clause
			query = query.concat(" ORDER BY " + dbQuery.getOrderByClause());
		}

		// **** closing the outer query
		if (dbQuery.count) {
			query = query.concat(")");
			if (DbConnection.getDbType() != Type.ORACLE) {
				query = query.concat(" AS COUNT_SUBSELECT_ALIAS");
			}

		} else if (dbQuery.maxRownum > 0) {
			if (DbConnection.getDbType() == Type.ORACLE) {
				/* the mixing of ROWNUM and ROWNUM_ROWNUM is intentional : the ROWNUM<=X clause is optimized by Oracle for performance */
				query = query.concat(") sub ) WHERE ROWNUM_ROWNUM > " + dbQuery.minRownum + " AND ROWNUM <= " + dbQuery.maxRownum);

			} else if (DbConnection.getDbType() == Type.SQLSERVER) {
				query = query.concat(") sub ) rows WHERE rows.ROWNUM > " + dbQuery.minRownum + " AND rows.ROWNUM <= " + dbQuery.maxRownum);

			} else if (DbConnection.getDbType() == Type.DB2) {
				query = query.concat(") internal$2) internal$1 WHERE internal$1.internal$rownum > " + dbQuery.minRownum 
						+ " AND internal$1.internal$rownum <= "+ (dbQuery.minRownum + dbQuery.maxRownum));

			} else if (DbConnection.getDbType() == Type.PostgreSQL) {
				query = query.concat(" LIMIT " + dbQuery.maxRownum + " OFFSET " + dbQuery.minRownum);

			} else {
				query = query.concat(" LIMIT " + dbQuery.minRownum + ", " + dbQuery.maxRownum);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("SQL Query :\n" + query);
			StringBuilder params = new StringBuilder();
			for (Object o : dbQuery.bindValues) {
				params.append(params.length() == 0 ? "" : ", ").append(o);
			}
			LOGGER.debug("SQL Params : " + params.toString());
		}
		
		if (dbQuery.forUpdate) {
			query = query + " FOR UPDATE ";
		}
		return query;
	}

	/**
	 * Ajout d'une condition 'inclus dans' le résultat d'une autre query.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param subQuery
	 *            query retournant la liste de valeurs
	 * @param bNot
	 *            true=négation (NOT IN)
	 * @param whereClause
	 *            The Where Clause.
	 * @return The Clause.
	 */

	protected String addCondIn(String colAlias, String tableAlias, DbQuery subQuery, boolean bNot, String whereClause) {
		String subQueryString = SqlBuilder.toSql(dbQuery, subQuery);
		if (subQuery == null || subQueryString.isEmpty()) {
			// Pas de subquery
			return whereClause;
		}

		String op = "IN";
		if (bNot) {
			op = "NOT IN";
		}

		Var inVar = dbQuery.getInVar(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("addCondIn: unknown column " + tableAlias + "." + colAlias+" in query "+dbQuery.name);
		}

		if (needAndOperator(whereClause)) {
			whereClause = whereClause.concat(AND_COND);
		}

		// add subquery in where clause
		whereClause = whereClause.concat(inVar.getExpression() + " " + op + " (");
		whereClause = whereClause.concat(subQueryString);
		whereClause = whereClause.concat(")");

		// And add parameters
		dbQuery.bindValues.addAll(subQuery.getBindValues());
		return whereClause;
	}

	/**
	 * Ajout d'une condition.
	 * 
	 * @param colAlias
	 *            - nom de variable
	 * @param tableAlias
	 *            - alias
	 * @param operator
	 *            - opérateur
	 * @param valeur1
	 *            - valeur de comparaison
	 * @param valeur2
	 *            - valeur 2 de comparaison
	 * @param whereClause
	 *            The Where Clause.
	 * @return The Clause.
	 */
	protected String addCond(String colAlias, String tableAlias, SqlOp operator, Object valeur1, Object valeur2, String whereClause) {
		Var inVar = dbQuery.getInVar(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("addCond: unknown column " + tableAlias + "." + colAlias + " in query " + dbQuery.name);
		}
		
		SqlOp op = operator;
		// si la valeur est null et si on peut remplacer l'opérateur...
		if (valeur1 == null) {
			if (op == SqlOp.OP_EQUAL || op == SqlOp.OP_LIKE) {
				op = SqlOp.OP_ISNULL;

			} else if (op == SqlOp.OP_N_EQUAL || op == SqlOp.OP_N_LIKE) {
				op = SqlOp.OP_N_ISNULL;
			}
		}

		if (needAndOperator(whereClause)) {
			whereClause = whereClause.concat(AND_COND);
		}

		boolean noCase = isCaseInsensitiveSearch(inVar);
		if (noCase) {
			whereClause = whereClause.concat("UPPER");
		}

		whereClause = whereClause.concat("(" + inVar.getExpression() + ")");

		whereClause = whereClause.concat(" " + op.val + " ");
		if (op.nbOps > 0) {
			if (noCase) {
				whereClause = whereClause.concat("UPPER");
			}

			String function = parseDefaultValue(inVar, valeur1);
			if (null != function) {
				whereClause = whereClause.concat(function);
			} else {
				whereClause = whereClause.concat("(?)");
				dbQuery.bindValues.add(parse(inVar, valeur1));
			}

			if (op.nbOps > 1) {
				// SqlOp.OP_BETWEEN
				function = parseDefaultValue(inVar, valeur2);
				if (null != function) {
					whereClause = whereClause.concat(AND_COND + function);
				} else {
					whereClause = whereClause.concat(" AND (?)");
					dbQuery.bindValues.add(parse(inVar, valeur2));
				}
			}
		}
		return whereClause;
	}

	/**
	 * Add a subquery condition.
	 * 
	 * @param colName
	 *            Name of the variable to add cond to
	 * @param tableAlias
	 *            Table alias for the given variable
	 * @param op
	 *            SQL operator
	 * @param subQuery
	 *            The sub select query to add to the cond
	 * @param whereClause
	 *            The current where Clause.
	 * @return The Clause.
	 */
	protected String addCond(String colName, String tableAlias, SqlOp op, DbQuery subQuery, String whereClause) {
		String subQueryString = SqlBuilder.toSql(dbQuery, subQuery);
		if (subQuery == null || subQueryString.isEmpty()) {
			// Pas de subquery
			return whereClause;
		}

		Var inVar = dbQuery.getInVar(colName, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("addCond: unknown column " + tableAlias + "." + colName + " in query " + dbQuery.name);
		}

		if (needAndOperator(whereClause)) {
			whereClause = whereClause.concat(AND_COND);
		}

		// add subquery in where clause
		whereClause = whereClause.concat(inVar.getExpression() + " " + op.val + " (");
		whereClause = whereClause.concat(subQueryString);
		whereClause = whereClause.concat(")");

		// And add parameters
		dbQuery.bindValues.addAll(subQuery.getBindValues());
		return whereClause;
	}

	protected String addCond(Cond cond, String whereClause) {
		Object value1 = null;
		if (cond.valuesList.size() > 0)
			value1 = cond.valuesList.get(0);

		Object value2 = null;
		if (cond.valuesList.size() > 1)
			value2 = cond.valuesList.get(1);

		switch (cond.type) {
		case IS_EXIST_COND:
			whereClause = addCondExists(cond.subQuery, !cond.op.equals(SqlOp.OP_IN), whereClause);
			break;
		case IS_SUB_QUERY:
			if (cond.op == SqlOp.OP_IN || cond.op == SqlOp.OP_N_IN)
				whereClause = addCondIn(cond.colAliases.get(0), cond.tableAliases.get(0), cond.subQuery, !cond.op.equals(SqlOp.OP_IN),
						whereClause);
			else
				whereClause = addCond(cond.colAliases.get(0), cond.tableAliases.get(0), cond.op, cond.subQuery, whereClause);
			break;
		case IS_VALUE_LIST:
			whereClause = addCondInList(cond.colAliases.get(0), cond.tableAliases.get(0), cond.valuesList, !cond.op.equals(SqlOp.OP_IN),
					whereClause);
			break;
		case IS_SEPARATOR:
			whereClause = addSeparator(cond, whereClause);
			break;
		case IS_CONCAT_COND:
			whereClause = addCondLikeConcat(cond.colAliases, cond.tableAliases, value1.toString(), !cond.op.equals(SqlOp.OP_LIKE),
					whereClause);
			break;
		case IS_BETWEEN_2_COLS:
			whereClause = addCond(cond.colAliases.get(0), cond.tableAliases.get(0), cond.op, cond.colAliases.get(1),
					cond.tableAliases.get(1), whereClause);
			break;
		default:
			whereClause = addCond(cond.colAliases.get(0), cond.tableAliases.get(0), cond.op, value1,
					value2, whereClause);
			break;
		}

		return whereClause;
	}

	/**
	 * Ajout d'une condition 'existe dans' subquery.
	 * 
	 * @param subquery
	 *            - query de seléction de valeurs
	 * @param bNot
	 *            - true=négation (NOT EXISTS)
	 * @param whereClause
	 *            The Where Clause.
	 * @return The Clause.
	 */
	protected String addCondExists(DbQuery subquery, boolean bNot, String whereClause) {
		String rel = "EXISTS";
		if (bNot) {
			rel = "NOT EXISTS";
		}

		if (needAndOperator(whereClause)) {
			whereClause = whereClause.concat(AND_COND);
		}

		String subQueryString = SqlBuilder.toSql(dbQuery, subquery);
		whereClause = whereClause.concat(rel + " (" + subQueryString + ")");
		// add binded values from subquery
		List<Object> eBindValues = subquery.getBindValues();
		for (Object obj : eBindValues) {
			dbQuery.bindValues.add(obj);
		}
		return whereClause;
	}

	/**
	 * Ajout d'une condition 'between' sur 'group by' (clause 'having').
	 * 
	 * @param colAlias
	 *            - alias de la colonne
	 * @param tableAlias
	 *            - alias de la table
	 * @param op
	 *            - opérateur
	 * @param valeur1
	 *            - première valeur de comparaison
	 * @param valeur2
	 *            - seconde valeur de comparaison
	 * @param havingClause
	 *            The Having Clause.
	 * @param whereClause
	 *            The Where Clause.
	 * @return The Clause.
	 */
	protected String addHavingCond(String colAlias, String tableAlias, SqlOp op, Object valeur1, Object valeur2, String havingClause,
			String whereClause) {
		if (colAlias == null) {
			throw new TechnicalException("addHavingCond: unknown alias 'null' for table " + tableAlias);
		}

		// si la valeur est null et si on peut remplacer l'opérateur...
		if (valeur1 == null || op == SqlOp.OP_ISNULL || op == SqlOp.OP_N_ISNULL) {
			if (op == SqlOp.OP_EQUAL || op == SqlOp.OP_LIKE || op == SqlOp.OP_ISNULL) {
				addHavingIsNull(colAlias, tableAlias, false, havingClause);
				return havingClause;
			}
			if (op == SqlOp.OP_N_EQUAL || op == SqlOp.OP_N_LIKE || op == SqlOp.OP_N_ISNULL) {
				addHavingIsNull(colAlias, tableAlias, true, havingClause);
				return havingClause;
			}
		}

		Var inVar = dbQuery.findOutVarByAlias(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("ToyDbQuery.addHavingCond: unknown column " + tableAlias + "." + colAlias+" in query "+dbQuery.name);
		}

		if (needAndOperator(havingClause)) {
			havingClause = havingClause.concat(AND_COND);
		}

		boolean noCase = isCaseInsensitiveSearch(inVar);
		if (noCase) {
			havingClause = havingClause.concat("UPPER");
		}

		havingClause = havingClause.concat("(" + dbQuery.aliash(inVar) + ")");

		havingClause = havingClause.concat(" " + op.val + " ");
		if (noCase) {
			havingClause = havingClause.concat("UPPER");
		}

		String function = parseDefaultValue(inVar, valeur1);
		if (null != function) {
			havingClause = havingClause.concat(function);
		} else {
			havingClause = havingClause.concat("(?)");
			dbQuery.bindValues.add(parse(inVar, valeur1));
		}

		if (op.val.equals(SqlOp.OP_BETWEEN.val)) {
			function = parseDefaultValue(inVar, valeur2);
			if (null != function) {
				havingClause = havingClause.concat(AND_COND + function);
			} else {
				havingClause = havingClause.concat(" AND (?)");
				dbQuery.bindValues.add(parse(inVar, valeur2));
			}
		}
		return havingClause;
	}

	/**
	 * Ajout une condition IS NULL/IS NOT NULL sur 'group by' (clause 'having').
	 * 
	 * @param colAlias
	 *            Alias de la colonne
	 * @param tableAlias
	 *            Alias de la table
	 * @param notNull
	 *            vrai pour tester IS NOT NULL, faux pour tester IS NULL
	 * @param havingClause
	 *            The Having Clause.
	 * @return The Clause.
	 */
	protected String addHavingIsNull(String colAlias, String tableAlias, boolean notNull, String havingClause) {
		String value = SqlOp.OP_ISNULL.val;
		if (notNull) {
			value = SqlOp.OP_N_ISNULL.val;
		}

		Var inVar = dbQuery.findOutVarByAlias(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("addHavingIsNull: unknown column " + tableAlias + "." + colAlias+" in query "+dbQuery.name);
		}
		if (needAndOperator(havingClause)) {
			havingClause = havingClause.concat(AND_COND);
		}

		havingClause = havingClause.concat(inVar.tableId + "." + inVar.model.getSqlName() + " " + value);
		return havingClause;
	}

	/**
	 * Ajout d'une condition 'inclus dans' liste de valeurs.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param objs
	 *            liste de valeurs
	 * @param bNot
	 *            true=négation (NOT IN)
	 * @param whereClause
	 *            The Where Clause.
	 * @return The Clause.
	 */
	protected String addCondInList(String colAlias, String tableAlias, List<?> objs, boolean bNot, String whereClause) {
		if (objs == null || objs.size() == 0) {
			// Liste vide. On ajoute pas de condition.
			return whereClause;
		}

		String op = "IN";
		if (bNot) {
			op = "NOT IN";
		}

		Var inVar = dbQuery.getInVar(colAlias, tableAlias);
		if (inVar == null) {
			throw new TechnicalException("addCondInList: unknown column " + tableAlias + "." + colAlias+" in query "+dbQuery.name);
		}

		if (needAndOperator(whereClause)) {
			whereClause = whereClause.concat(AND_COND);
		}

		// add subquery in where clause
		whereClause = whereClause.concat(inVar.getExpression() + " " + op + " (");
		for (int i = 0; i < objs.size(); i++) {
			whereClause = whereClause.concat("(?)");
			dbQuery.bindValues.add(parse(inVar, objs.get(i)));
			if (i < (objs.size() - 1)) {
				whereClause = whereClause.concat(", ");
			}
		}
		whereClause = whereClause.concat(")");

		return whereClause;
	}

	/**
	 * Used when building the final SQL statement.<br>
	 * If select clause has 'group by' functions like avg() then add standard columns as 'group by' clauses.
	 */
	protected String addGroupByFromSelect() {
		String groupBy = "";
		boolean hasGrouping = false;
		for (int i = 0; i < dbQuery.outVars.size() && !hasGrouping; i++) {
			Var var = dbQuery.outVars.get(i);
			if (var.isGrouping) {
				hasGrouping = true;
			}
		}
		if (hasGrouping) {
			// build groupby list already defined in query
			List<Var> groupByVars = new ArrayList<DbQuery.Var>(dbQuery.groupByConds.size());
			for (Cond cond : dbQuery.groupByConds) {
				Var outVar = dbQuery.findOutVar(cond.colAliases.get(0), cond.tableAliases.get(0));
				groupByVars.add(outVar);
			}
			for (Var outVar : dbQuery.outVars) {
				if (!outVar.isGrouping && outVar.model.isFromDatabase() && !groupByVars.contains(outVar)) {
					groupBy = addGroupByElement(groupBy, outVar);
				}
			}
		}
		return groupBy;
	}

	/**
	 * Add a like Concat condition.
	 * 
	 * @param colAliases
	 *            Aliases of the columns.
	 * @param tableAliases
	 *            Aliases of the tables.
	 * @param paramValue
	 *            Value to compare.
	 * @param bNot
	 *            If true, Operator is Not Like.
	 * @param whereClause
	 *            The Where Clause.
	 * @return The Clause.
	 */
	protected String addCondLikeConcat(List<String> colAliases, List<String> tableAliases, String paramValue, boolean bNot, String whereClause) {
		// Check all input variables
		ArrayList<Var> inVars = new ArrayList<Var>();
		for (int i = 0; i < colAliases.size(); i++) {
			String cAlias = colAliases.get(i);
			String tAlias = tableAliases.get(i);
			Var inVar = dbQuery.getInVar(cAlias, tAlias);
			if (inVar == null) {
				throw new TechnicalException("ToyDbQuery.addCond: unknown column " + tAlias + "." + cAlias+" in query "+dbQuery.name);
			}
			inVars.add(inVar);
		}

		// Contact all columns (joined with and espace)
		StringBuilder currentClause = new StringBuilder();
		for (int k = 0; k < inVars.size(); k++) {
			Var inVarTmp = inVars.get(k);
			String columnExpr = inVarTmp.getExpression();

			if (!inVarTmp.model.isAlpha()) {
				// Not an alpha column, cast it
				columnExpr = dbQuery.getToStringFunction(columnExpr, inVarTmp.model);
			}

			// We use ifNull function to avoir getting null inside the concat string expression.
			columnExpr = dbQuery.getIfNullFunction(DbConnection.getDbType()) + "(" + columnExpr + ", '')";
			if (k < inVars.size() - 1) {
				currentClause.append("CONCAT(").append(columnExpr).append(",");
				currentClause.append("CONCAT(' ',");
			} else {
				// Last column
				currentClause.append(columnExpr);
			}
		}

		// Close all concats
		int nbreConcat = 2 * inVars.size() - 2;
		for (int p = 0; p < nbreConcat; p++) {
			currentClause.append(")");
		}

		// Fake outVar for the query
		EntityField varModel = new EntityField("TOYCONCATSEARCH", SqlTypes.VARCHAR2, 8000, 0, Memory.NO, false, false);
		Var outVar = new Var("TOYCONCATSEARCH", null, null, varModel);

		String value = paramValue;
		// Handle insensitive search
		if (isCaseInsensitiveSearch(outVar)) {
			currentClause.insert(0, "UPPER(").append(")");
			value = value.toUpperCase();
		}

		if (!value.startsWith("%")) {
			value = "%" + value;
		}
		if (!value.endsWith("%")) {
			value = value.concat("%");
		}

		// Create final where clause
		String rel = "LIKE";
		if (bNot) {
			rel = "NOT LIKE";
		}
		if (needAndOperator(whereClause)) {
			whereClause = whereClause.concat(AND_COND);
		}
		whereClause = whereClause.concat(currentClause.toString() + " " + rel + " ?");
		dbQuery.bindValues.add(parse(outVar, value));

		return whereClause;
	}

	/**
	 * Add a condition between two columns.
	 * 
	 * @param colAlias1
	 *            First column alias
	 * @param tableAlias1
	 *            First column table
	 * @param operator
	 *            Sql operator between two columns (equals, greater than, etc.)
	 * @param colAlias2
	 *            Second column alias
	 * @param tableAlias2
	 *            Second column table
	 * @param whereClause
	 *            The Where Clause.
	 * @return The Clause.
	 */
	protected String addCond(String colAlias1, String tableAlias1, SqlOp operator, String colAlias2, String tableAlias2, String whereClause) {
		Var inVar1 = dbQuery.getInVar(colAlias1, tableAlias1);
		if (inVar1 == null && parentQuery != null) {
			inVar1 = parentQuery.getInVar(colAlias1, tableAlias1);
		}
		if (inVar1 == null) {
			throw new TechnicalException("ToyDbQuery.addCond: unknown column " + tableAlias1 + "." + colAlias1+" in query "+dbQuery.name);
		}
		Var inVar2 = dbQuery.getInVar(colAlias2, tableAlias2);
		if (inVar2 == null && parentQuery != null) {
			inVar2 = parentQuery.getInVar(colAlias2, tableAlias2);
		}
		if (inVar2 == null) {
			throw new TechnicalException("ToyDbQuery.addCond: unknown column " + tableAlias2 + "." + colAlias2+" in query "+dbQuery.name);
		}

		if (needAndOperator(whereClause)) {
			whereClause = whereClause.concat(AND_COND);
		}

		boolean noCase = isCaseInsensitiveSearch(inVar1) && isCaseInsensitiveSearch(inVar2);
		if (noCase) {
			whereClause = whereClause.concat("UPPER");
		}
		whereClause = whereClause.concat("(" + inVar1.getExpression() + ")");
		whereClause = whereClause.concat(" " + operator.val + " ");
		if (noCase) {
			whereClause = whereClause.concat("UPPER");
		}
		whereClause = whereClause.concat("(" + inVar2.getExpression() + ")");
		return whereClause;
	}

	/**
	 * Ajoute AND à la fin de clause.
	 * 
	 * @param clause
	 *            The Clause.
	 * @return The Clause.
	 */
	protected String andOperator(String clause) {
		if (needAndOperator(clause)) {
			return AND_COND;
		}
		return "";
	}

	/**
	 * Ajoute OR à la fin de clause.
	 * 
	 * @param clause
	 *            The Clause.
	 * @return The Clause.
	 */
	protected String orOperator(String clause) {
		if (needOrOperator(clause)) {
			return OR_COND;
		}
		return "";
	}
	
	/**
	 * Check if given where clause need to add a "and" keyword.
	 * 
	 * @param whereClause Clause to check
	 * @return true if "and" keyword is needed
	 */
	protected boolean needAndOperator(String whereClause) {
		String where = whereClause.trim();
		// True if alredy some clause, not a group start and not a OR cond
		return (where.length() > 0 && !where.endsWith("(") && !where.endsWith(" " + OR_COND.trim()) && !where.endsWith(" " + AND_COND.trim()));
	}

	/**
	 * Check if given where clause need to add a "or" keyword.
	 * 
	 * @param whereClause
	 *            Clause to check
	 * @return true if "or" keyword is needed
	 */
	protected boolean needOrOperator(String whereClause) {
		return needAndOperator(whereClause);
	}

	/**
	 * Add a Group By element
	 * 
	 * @param colAlias
	 *            The column Alias.
	 * @param tableAlias
	 *            The table Alias.
	 */
	protected String addGroupByElement(String groupBy, Var outVar) {
		if (!outVar.isGrouping && outVar.model.isFromDatabase()) {
			groupBy += (groupBy.isEmpty() ? "" : ",")
					+ outVar.tableId
					+ (outVar.model.getMemory() != Memory.SQL ? "." : "_")
					+ outVar.model.getSqlName();
		}
		return groupBy;
	}

	/**
	 * Parse a variable.
	 * 
	 * @param var
	 *            Variable.
	 * @param value
	 *            Value.
	 * @return The value parsed depends on the variable type.
	 */
	public static Object parse(Var var, Object value) {
		Object outValue = value;
		if (value != null && value instanceof String) {
			String sValue = (String) value;
			if (var.model.hasDefinedValues() && var.model.isDefCode(sValue)) {
				if (var.model.getSqlType() == SqlTypes.BOOLEAN) {
					outValue = var.model.getBooleanDefValue(sValue);
				} else if (var.model.getSqlType() == SqlTypes.INTEGER) {
					outValue = Integer.parseInt(var.model.getDefValue(sValue));
				} else {
					outValue = var.model.getDefValue(sValue);
				}
			} else if (var.model.getSqlType() == SqlTypes.INTEGER) {
				try {
					outValue = Integer.parseInt(sValue);
				} catch (NumberFormatException ex) {
					throw new TechnicalException("La valeur \"" + sValue + "\" n'est pas correcte. ");
				}
			} else if (var.model.getSqlType() == SqlTypes.TIME) {
				outValue = new Time(((Date) value).getTime());
			} else if (var.model.getSqlType() == SqlTypes.TIMESTAMP) {
				outValue = new Timestamp(((Date) value).getTime());
			}
		}
		return outValue;
	}

	/**
	 * Parse a variable with default value.
	 * 
	 * @param var
	 *            Variable.
	 * @param value
	 *            Value.
	 * @return The value parsed depends on the variable type.
	 */
	public static String parseDefaultValue(Var var, Object value) {
		String outValue = null;

		if ("*TODAY".equals(value) || "*NOW".equals(value)) {

			if (var.model.getSqlType() == SqlTypes.DATE) {
				if (DbConnection.getDbType() == Type.ORACLE) {
					outValue = "SYSDATE";
				} else if (DbConnection.getDbType() == Type.PostgreSQL) {
					outValue = "current_date";
				} else {
					outValue = "CURRENT_DATE";
				}

			} else if (var.model.getSqlType() == SqlTypes.TIME) {

				if (DbConnection.getDbType() == Type.ORACLE) {
					outValue = "SYSDATE";
				} else if (DbConnection.getDbType() == Type.PostgreSQL) {
					outValue = "current_time";
				} else {
					outValue = "CURRENT_TIME";
				}

			} else if (var.model.getSqlType() == SqlTypes.TIMESTAMP) {

				if (DbConnection.getDbType() == Type.PostgreSQL) {
					outValue = "clock_timestamp()";
				} else {
					outValue = "CURRENT_TIMESTAMP";
				}
			}
		}
		return outValue;
	}

	/**
	 * Add a separator on a clause.
	 * 
	 * @param cond
	 *            The Condition.
	 * @param clause
	 *            The Clause.
	 * @return The Clause.
	 */
	protected String addSeparator(Cond cond, String clause) {
		String separator = "";
		if (cond.valuesList.get(0).toString().equals("AND"))
			separator = andOperator(clause);
		else if (cond.valuesList.get(0).toString().equals("OR"))
			separator = orOperator(clause);
		else
			separator = cond.valuesList.get(0).toString();
		return clause.concat(separator);
	}

	/**
	 * Check if the where clause on the given variable should be done case sensitive or not.<br>
	 * Default is INsensitive if :
	 * <ul>
	 * <li>DbQuery is a case INsensitive search</li>
	 * <li>AND inVar is a VARCHAR2</li>
	 * </ul>
	 * 
	 * @param inVar Current var
	 * @return true if condition should be done case INsensitive
	 */
	protected boolean isCaseInsensitiveSearch(Var inVar) {
		return dbQuery.isCaseInsensitiveSearch() && inVar.model.getSqlType() == SqlTypes.VARCHAR2;
	}
	
	/**
	 * Clear DbQuery indexes map.
	 */
	protected void clearDbQueryIndexes() {
		dbQuery.indexes.clear();
	}

}
