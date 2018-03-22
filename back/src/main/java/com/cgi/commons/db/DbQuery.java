package com.cgi.commons.db;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.cgi.commons.db.DbConnection.Type;
import com.cgi.commons.db.SqlBuilder.SqlOp;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.ref.entity.EntityField;
import com.cgi.commons.ref.entity.EntityField.Memory;
import com.cgi.commons.ref.entity.EntityField.SqlTypes;
import com.cgi.commons.ref.entity.EntityModel;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.KeyModel;
import com.cgi.commons.ref.entity.LinkModel;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.reflect.DomainUtils;

/**
 * This class represents a query to the database. It is not thread-safe.
 */
public class DbQuery implements Cloneable {

	/** Join type. */
	public enum Join {
		/** Inner join. */
		STRICT,
		/** Outer join. */
		LOOSE,
		/** Cartesian product. */
		NONE;
	}

	/** Variable visibility. */
	public enum Visibility {
		/** Visible. */
		VISIBLE,
		/** Invisible. */
		INVISIBLE,
		/** Protected. */
		PROTECTED;
	}

	/**
	 * Représente une "variable" de la requête SQL. Une variable possède un nom (le même que le nom du champ sur l'entité), un nom affichable, un
	 * type. Une variable référence une table particulière dans laquelle on va la récupérer.
	 */
	public static class Var implements Cloneable {
		/** Name. */
		public String name = null;
		/** External Name. */
		public String extern = null;
		/** Alias of the variable in the query. */
		public String alias = null;
		/** Id of the table of the variable. */
		public String tableId = null;
		/** Model of the field. */
		public EntityField model = null;
		/** Visibility of the variable. */
		public Visibility visibility = Visibility.VISIBLE;
		/** Expression. */
		public String expr = null;
		/** Indicates if variable is grouping. */
		public boolean isGrouping = false;

		/**
		 * Constructor.
		 * 
		 * @param pName
		 *            Name.
		 * @param pExternName
		 *            External Name.
		 * @param pTableId
		 *            Id of the table of the variable.
		 * @param pField
		 *            Model of the field.
		 */
		public Var(String pName, String pExternName, String pTableId, EntityField pField) {
			name = pName;
			extern = pExternName;
			if (extern == null) {
				extern = pField.getSqlName();
			}
			tableId = pTableId;
			model = pField;

			if (null != pField.getSqlExpr()) {
				expr = pField.getSqlExpr().replace(":tableAlias", tableId);
			}
		}

		/**
		 * Returns the variable alias.
		 * 
		 * @return {@code tableId + _ + name}.
		 */
		public String getColumnAlias() {
			if (alias != null) {
				return alias;
			}
			return tableId + "_" + name;
		}

		/**
		 * @return "{@code expr}" si elle est différente de {@code null}, sinon "{@code tableId.sqlName}".
		 */
		public String getExpression() {
			if (expr != null) {
				return expr;
			}
			return tableId + "." + model.getSqlName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((tableId == null) ? 0 : tableId.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Var other = (Var) obj;
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			if (tableId == null) {
				if (other.tableId != null) {
					return false;
				}
			} else if (!tableId.equals(other.tableId)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return tableId + '.' + name;
		}

		/**
		 * Clone a Var.
		 *
		 * @return The Var cloned.
		 */
		@Override
		protected Var clone() {
			try {
				Var var = (Var) super.clone();
				return var;
			} catch (CloneNotSupportedException e) {
				throw new TechnicalException(e.getMessage(), e);
			}
		}
	}

	/**
	 * Types de condition.
	 */
	public enum CondTypes {
		/** Between 2 columns. */
		IS_BETWEEN_2_COLS,
		/** Separators. */
		IS_SEPARATOR,
		/** List of values. */
		IS_VALUE_LIST,
		/** Sub queries. */
		IS_SUB_QUERY,
		/** Condition of existance. */
		IS_EXIST_COND,
		/** Condition with concat. */
		IS_CONCAT_COND,
		/** Default. */
		DEFAULT;
	};

	/**
	 * Représente une "Condition" de la requête SQL. Une condition peut être associée à la clause where/having, goupBy ou orderBy ainsi que sur
	 * les jointures.
	 */
	public static class Cond implements Cloneable {
		@Override
		protected Cond clone() {
			try {
				Cond cond = (Cond) super.clone();

				cond.valuesList = new ArrayList<Object>();
				for (Object c : valuesList) {
					cond.valuesList.add(c);
				}

				cond.colAliases = new ArrayList<String>();
				for (String c : colAliases) {
					cond.colAliases.add(c);
				}

				cond.tableAliases = new ArrayList<String>();
				for (String c : tableAliases) {
					cond.tableAliases.add(c);
				}
				return cond;
			} catch (CloneNotSupportedException e) {
				throw new TechnicalException(e.getMessage(), e);
			}
		}

		// Propriétés
		/** Operator. */
		public SqlOp op;
		/** Sub Query. */
		public DbQuery subQuery;
		/** List of values. */
		public List<Object> valuesList = new ArrayList<Object>();
		/** Aliases of columns. */
		public List<String> colAliases = new ArrayList<String>();
		/** Aliases of tables. */
		public List<String> tableAliases = new ArrayList<String>();
		/** Type of the condition. */
		public CondTypes type = CondTypes.DEFAULT;

		/**
		 * Create a condition on a column.
		 * 
		 * @param colAlias
		 *            Alias of the column.
		 * @param tableAlias
		 *            Alias of the table.
		 * @param op
		 *            Operator.
		 * @param valeur1
		 *            Value 1.
		 * @param valeur2
		 *            Value 2.
		 */
		public Cond(String colAlias, String tableAlias, SqlOp op, Object valeur1, Object valeur2) {
			this.colAliases.add(colAlias);
			this.tableAliases.add(tableAlias);
			this.op = op;
			this.valuesList.add(valeur1);
			this.valuesList.add(valeur2);
		}

		/**
		 * Create a condition between two columns.
		 * 
		 * @param colAlias1
		 *            Alias of the first column.
		 * @param tableAlias1
		 *            Alias of the first table.
		 * @param operator
		 *            Operator.
		 * @param colAlias2
		 *            Alias of the second column.
		 * @param tableAlias2
		 *            Alias of the second table.
		 */
		public Cond(String colAlias1, String tableAlias1, SqlOp operator, String colAlias2, String tableAlias2) {
			this.colAliases.add(colAlias1);
			this.tableAliases.add(tableAlias1);
			this.colAliases.add(colAlias2);
			this.tableAliases.add(tableAlias2);
			this.op = operator;
			type = CondTypes.IS_BETWEEN_2_COLS;
		}

		/**
		 * Create a condition on a list of values.
		 * 
		 * @param colAlias
		 *            Alias of the column.
		 * @param tableAlias
		 *            Alias of the table.
		 * @param objs
		 *            List of values.
		 * @param operator
		 *            Operator.
		 */
		@SuppressWarnings("unchecked")
		public Cond(String colAlias, String tableAlias, List<?> objs, SqlOp operator) {
			this.colAliases.add(colAlias);
			this.tableAliases.add(tableAlias);
			this.op = operator;
			this.valuesList = (List<Object>) objs;
			type = CondTypes.IS_VALUE_LIST;
		}

		/**
		 * Create a separator on the query. For exemple it can add parenthesis.
		 * 
		 * @param separator
		 *            The String for separation.
		 */
		public Cond(String separator) {
			this.valuesList.add(separator);
			type = CondTypes.IS_SEPARATOR;
		}

		/**
		 * Create a condition about a sub query.
		 * 
		 * @param colAlias
		 *            Alias of the column.
		 * @param tableAlias
		 *            Alias of the table.
		 * @param subQuery
		 *            Sub Query.
		 * @param operator
		 *            Operator.
		 */
		public Cond(String colAlias, String tableAlias, DbQuery subQuery, SqlOp operator) {
			this.colAliases.add(colAlias);
			this.tableAliases.add(tableAlias);
			this.subQuery = subQuery;
			this.op = operator;
			type = CondTypes.IS_SUB_QUERY;
		}

		/**
		 * Create a condition "exist" on a sub query.
		 * 
		 * @param subquery
		 *            The Sub Query.
		 * @param opIn
		 *            Operator.
		 */
		public Cond(DbQuery subquery, SqlOp opIn) {
			this.subQuery = subquery;
			this.op = opIn;
			type = CondTypes.IS_EXIST_COND;
		}

		/**
		 * Create a condition CONCAT.
		 * 
		 * @param colAliases
		 *            list of the aliases of the columns.
		 * @param tableAliases
		 *            list of the aliases of the tables.
		 * @param paramValue
		 *            Value.
		 * @param opLike
		 *            Operator.
		 */
		public Cond(List<String> colAliases, List<String> tableAliases, String paramValue, SqlOp opLike) {
			this.op = opLike;
			this.colAliases = colAliases;
			this.tableAliases = tableAliases;
			this.valuesList.add(paramValue);
			type = CondTypes.IS_CONCAT_COND;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 * TODO define DbQuery.hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((colAliases == null) ? 0 : colAliases.hashCode());
			result = prime * result + ((op == null) ? 0 : op.hashCode());
			result = prime * result + ((subQuery == null) ? 0 : subQuery.hashCode());
			result = prime * result + ((tableAliases == null) ? 0 : tableAliases.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((valuesList == null) ? 0 : valuesList.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 * TODO define DbQuery.equals()
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Cond other = (Cond) obj;
			if (colAliases == null) {
				if (other.colAliases != null)
					return false;
			} else if (!colAliases.equals(other.colAliases))
				return false;
			if (op != other.op)
				return false;
			if (subQuery == null) {
				if (other.subQuery != null)
					return false;
			} else if (!subQuery.equals(other.subQuery))
				return false;
			if (tableAliases == null) {
				if (other.tableAliases != null)
					return false;
			} else if (!tableAliases.equals(other.tableAliases))
				return false;
			if (type != other.type)
				return false;
			if (valuesList == null) {
				if (other.valuesList != null)
					return false;
			} else if (!valuesList.equals(other.valuesList))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Cond [colAliases=" + colAliases + ", tableAliases=" + tableAliases + ", op=" + op + ", valuesList=" + valuesList + "]";
		}
	}

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(DbQuery.class);

	/** Constant for ascending (ASC). */
	public static final String ASC = "ASC";
	/** Constant for descending (DESC). */
	public static final String DESC = "DESC";

	/** Indicates if search is done with (false) or without (true) case sensitive. */
	protected boolean caseInsensitiveSearch = true;
	/** Indicates if there is a distinct. */
	protected boolean distinct = false;
	/**
	 * Help for the first add column (when a query is build, all the columns of the main entity are added. If we add a column manualy for the
	 * first time, columns are removed and the new column is added).
	 */
	protected boolean firstAddColumn = true;
	/** Indicates if query is for update or not. */
	protected boolean forUpdate = false;
	/** Name of the query. */
	protected String name;

	/** Tables. */
	protected List<Table> tables = new ArrayList<DbQuery.Table>();

	/** Variables for Where clause. */
	protected List<Var> inVars = new ArrayList<DbQuery.Var>();
	/** Variable for Select clause. */
	protected List<Var> outVars = new ArrayList<DbQuery.Var>();
	/** Constants for Select clause. */
	protected List<Const> outConsts = new ArrayList<DbQuery.Const>();
	/** Variables for Order By clause. */
	protected List<SortVar> sortVars = new ArrayList<DbQuery.SortVar>();
	/** Binded values for Conditions. */
	protected List<Object> bindValues = new ArrayList<Object>();

	/** Conditions for where clause. */
	protected List<Cond> whereConds = new ArrayList<Cond>();
	/** Conditions for having clause. */
	protected List<Cond> havingConds = new ArrayList<Cond>();
	/** Conditions for join clause. */
	protected List<Cond> joinConds = new ArrayList<Cond>();
	/** Conditions for groupBy clause. */
	protected List<Cond> groupByConds = new ArrayList<Cond>();

	/** Max row num. */
	protected int maxRownum = -1;
	/** Min row num. */
	protected int minRownum = 0;
	/** ResultSet Fetch size */
	protected int fetchSize = 0;

	/** Indicates if the query is a count query. */
	protected boolean count = false;

	/** Indicates if the query is exposed as a web service. */
	protected boolean isExposedAsWebservice = false;
	/** Query has been secured through dbSecure logic */
	protected boolean secured = false;

	/** Map of the indexes. */
	protected Map<String, Integer> indexes = new HashMap<String, Integer>();

	/**
	 * Returns the index of the alias.
	 * 
	 * @param name
	 *            The alias from we want the index
	 * @return The index of alias name in the query. This should be used to get data from result set instead of accessing RS via names.
	 */
	public int getIndex(String name) {
		if (indexes.get(name) != null) {
			return indexes.get(name).intValue();
		}
		StringBuilder aliasList = new StringBuilder();
		for (String aliasName : indexes.keySet()) {
			aliasList.append(aliasName);
			aliasList.append(", ");
		}
		throw new TechnicalException("Index doesn't exist in query. Existing alias are : " + aliasList);
	}
	
	/**
	 * Returns the first index found for entityName, varname.
	 * 
	 * @param entityName Name of entity which holds variable varName
	 * @param varName The variable we want the index
	 * @return The index of variable in the query. This should be used to get data from result set instead of accessing RS via names.
	 */
	public int getIndex(String entityName, String varName) {
		if (varName == null) {
			throw new TechnicalException("Var name is null, cannot find matching alias.");
		}

		String name = null;
		for (Var v : outVars) {
			if (entityName == null || entityName.equals(getEntity(v.tableId))) {
				if (varName.equals(v.name)) {
					name = v.tableId + "_" + v.model.getSqlName();
					break;
				}
			}
		}
		return getIndex(name);
	}

	/**
	 * Gets the column alias created by the query to identify in a unique way the selected value. This alias can be passed to the getIndex()
	 * method in order to get the resultSet index.
	 * 
	 * @param entityName
	 *            Entity (=table) name
	 * @param name
	 *            Variable (=column) name
	 * @return the alias used by the query to identify the column selected. The alias can be hashed if it's longer than 30 characters.
	 */
	public String getColumnAlias(String entityName, String name) {
		if (name == null) {
			throw new TechnicalException("Var name is null, cannot find matching alias.");
		}

		for (Var v : outVars) {
			if (entityName == null || entityName.equals(getEntity(v.tableId))) {
				if (name.equals(v.name)) {
					return aliash(v);
				}
			}
		}
		return null;
	}

	/**
	 * Constant column.
	 */
	public static class Const {
		/** Name. */
		String name;
		/** Value. */
		String value;
	}

	/** Sorting. */
	private static class SortVar implements Cloneable {
		/** Var of the sort. */
		Var inVar = null;
		/** Direction : "ASC" or "DESC". */
		String direction = null;
		/** Indicates if the sort is categorized. */
		boolean categorize = false;

		/**
		 * Constructor.
		 * 
		 * @param v
		 *            Var of the sort.
		 * @param d
		 *            Direction : "ASC" or "DESC".
		 * @param c
		 *            Indicates if the sort is categorized.
		 */
		protected SortVar(Var v, String d, boolean c) {
			inVar = v;
			direction = d;
			categorize = c;
		}

		@Override
		public String toString() {
			return "SortVar [inVar=" + inVar + ", direction=" + direction + ", " + Boolean.toString(categorize) + "]";
		}

		/**
		 * Defined only on inVar
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((inVar == null) ? 0 : inVar.hashCode());
			return result;
		}

		/**
		 * Defined only on inVar
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SortVar other = (SortVar) obj;
			if (inVar == null) {
				if (other.inVar != null)
					return false;
			} else if (!inVar.equals(other.inVar))
				return false;
			return true;
		}

		/**
		 * Clone a SortVar.
		 *
		 * @return The SortVar cloned.
		 */
		@Override
		protected SortVar clone() {
			try {
				// note: cloned SortVar.inVar still references the original instance of Var
				SortVar var = (SortVar) super.clone();
				return var;
			} catch (CloneNotSupportedException e) {
				throw new TechnicalException(e.getMessage(), e);
			}
		}
	}

	/**
	 * Représente une jointure sur un lien.
	 */
	protected static class JoinedLink implements Cloneable {
		/** Source Entity. */
		public Entity srcEntity;
		/** Destination Entity. */
		public Entity dstEntity;
		/** Source Key Model. */
		public KeyModel srcKey;
		/** Destination Key Model. */
		public KeyModel dstKey;
		/** Source Alias. */
		public String srcAlias;

		/**
		 * Constructor.
		 * 
		 * @param src
		 *            Source Entity.
		 * @param dst
		 *            Destination Entity.
		 * @param alias
		 *            Source Alias.
		 * @param dstK
		 *            Destination Key Model.
		 * @param srcK
		 *            Source Key Model.
		 */
		public JoinedLink(Entity src, Entity dst, String alias, KeyModel dstK, KeyModel srcK) {
			srcEntity = src;
			dstEntity = dst;
			srcKey = srcK;
			dstKey = dstK;
			srcAlias = alias;
		}

		/**
		 * Clone a JoinedLink.
		 * 
		 * @return The JoinedLink cloned.
		 */
		@Override
		public JoinedLink clone() {
			try {
				JoinedLink join = (JoinedLink) super.clone();
				return join;
			} catch (CloneNotSupportedException e) {
				throw new TechnicalException(e.getMessage(), e);
			}
		}
	}

	/**
	 * Table of the From.
	 */
	protected static class Table implements Cloneable {
		/** Entity. */
		Entity entity = null;
		/** Alias. */
		String alias = null;
		/** External Name. */
		String extern = null;
		/** Join. */
		String join = "";
		/** Condition for the Join. */
		String joinCond = "";
		/** Indicates an outer joined. */
		boolean isOuterJoined = false;
		/** List of the links joined. */
		public List<JoinedLink> joinedLinks = new ArrayList<JoinedLink>();
		/** Indicates an external joined. */
		public boolean bExtJoin;
		/** Condition for the Join added by the user. */
		List<Cond> userJoinConds = new ArrayList<Cond>();

		/**
		 * Constructor.
		 * 
		 * @param classe
		 *            The Entity.
		 * @param as
		 *            The alias.
		 */
		protected Table(Entity classe, String as) {
			entity = classe;
			alias = as;
			extern = classe.getModel().dbName();
		}

		@Override
		public String toString() {
			return "Table [entity=" + entity + ", alias=" + alias + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((extern == null) ? 0 : extern.hashCode());
			result = prime * result + ((alias == null) ? 0 : alias.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Table other = (Table) obj;
			if (extern == null) {
				if (other.extern != null) {
					return false;
				}
			} else if (!extern.equals(other.extern)) {
				return false;
			}
			if (alias == null) {
				if (other.alias != null) {
					return false;
				}
			} else if (!alias.equals(other.alias)) {
				return false;
			}
			return true;
		}

		/**
		 * Clone a Table.
		 * 
		 * @return The Table cloned.
		 */
		@Override
		public Table clone() {
			try {
				Table table = (Table) super.clone();
				table.joinedLinks = new ArrayList<JoinedLink>(joinedLinks.size());
				for (JoinedLink l : joinedLinks) {
					table.joinedLinks.add(l.clone());
				}
				table.userJoinConds = new ArrayList<Cond>(userJoinConds.size());
				for (Cond c : userJoinConds) {
					table.userJoinConds.add(c.clone());
				}
				return table;
			} catch (CloneNotSupportedException e) {
				throw new TechnicalException(e.getMessage(), e);
			}
		}
	}

	/**
	 * Operator Logic.
	 */
	public enum LogicOperator {
		/** Or. */
		OR("OR"),
		/** And. */
		AND("AND"),
		/** Equals. */
		EQUALS("="),
		/** Not Equals. */
		NEQUALS("<>"),
		/** Superior. */
		SUP(">"),
		/** Superior or Equals. */
		SUPEQUALS(">="),
		/** Inferior. */
		INF("<"),
		/** Inferior or Equals. */
		INFEQUALS("<=");

		/** Operator. */
		private String op;

		/**
		 * Constructor.
		 * 
		 * @param op
		 *            Operator.
		 */
		LogicOperator(String op) {
			this.setOp(op);
		}

		/**
		 * Setter for the operator.
		 * 
		 * @param op
		 *            The operator.
		 */
		public void setOp(String op) {
			this.op = op;
		}

		/**
		 * Getter for the operator.
		 * 
		 * @return The operator.
		 */
		public String getOp() {
			return op;
		}
	};

	/**
	 * Operator on value.
	 */
	public enum ValuableOperator {
		/** Is Null. */
		ISNULL("IS NULL"),
		/** Is_Not_Nill. */
		ISNOTNULL("IS NOT NULL");

		/** Operator. */
		private String op;

		/**
		 * Constructor.
		 * 
		 * @param op
		 *            Operator.
		 */
		ValuableOperator(String op) {
			this.setOp(op);
		}

		/**
		 * Setter for the operator.
		 * 
		 * @param op
		 *            The operator.
		 */
		public void setOp(String op) {
			this.op = op;
		}

		/**
		 * Getter for the operator.
		 * 
		 * @return The operator.
		 */
		public String getOp() {
			return op;
		}
	}

	/**
	 * Set distinct - indicates if there is a distinct into the query.
	 * 
	 * @param d
	 *            Disctint.
	 */
	public void setDistinct(boolean d) {
		distinct = d;
	}

	/**
	 * Generate the order by clause.
	 * 
	 * @return The order by clause in SQL.
	 */
	public String getOrderByClause() {
		if (sortVars.isEmpty()) {
			return null;
		}
		StringBuilder order = new StringBuilder();
		for (SortVar sVar : sortVars) {
			if (!"".equals(order.toString())) {
				order.append(", ");
			}
			if ("".equals(sVar.inVar.tableId)) {
				order.append(sVar.inVar.model.getSqlName()).append(" ").append(sVar.direction);
			} else {
				order.append(aliash(sVar.inVar)).append(" ").append(sVar.direction);
			}
		}
		return order.toString();
	}

	/** Max lenght of the identifiers. */
	public static int IDENTIFIER_MAX_LEN = 30;

	/**
	 * Return the hashed alias for a variable.
	 * 
	 * @param v
	 *            The variable.
	 * @return Hashed alias.
	 */
	public String aliash(Var v) {
		if (v.alias != null) {
			return aliash(v.tableId + "_" + v.alias);
		}
		return aliash(v.tableId + "_" + v.model.getSqlName());

	}

	/**
	 * Please, notice my incredible pun on the method name. If name > 30 characters, we'll create an hashed unique alias out of it.
	 * 
	 * @param name
	 *            The maybe too long name.
	 * @return A name that makes less than 30 characters. 30 is the limit because it's the smallest maximum of all known databases.
	 */
	public String aliash(String name) {
		if (name == null || name.length() <= IDENTIFIER_MAX_LEN) {
			return name;
		}
		String hashName = String.valueOf(name.hashCode());
		if (hashName.startsWith("-")) {
			hashName = hashName.substring(1);
		}

		if (hashName.length() >= IDENTIFIER_MAX_LEN) {
			return "A" + hashName.substring(hashName.length() - IDENTIFIER_MAX_LEN + 1);
		} else {
			return name.substring(0, IDENTIFIER_MAX_LEN - hashName.length() - 1) + hashName;
		}
	}

	/**
	 * Filter the table to keep only really used tables.
	 * 
	 * @return the list of tables.
	 */
	public List<Table> filterTables() {
		Set<Table> filteredTables = new LinkedHashSet<Table>(tables.size());
		// When there's a group by clause, do not remove any table
		// FIXME missing groupBy induced by expression use in select clause
		if (groupByConds != null && !groupByConds.isEmpty()) {
			return tables;
		}

		for (int i = 0; i < tables.size(); i++) {
			Table table = tables.get(i);

			if (null != table.join && table.join.indexOf("LEFT") > -1) {
				String alias = table.alias;

				if (clauseContainTable(whereConds, alias)
						|| includeTable(i, alias, new ArrayList<String>())) {

					filteredTables.add(table);
				}

			} else {
				filteredTables.add(table);
			}
		}
		return new ArrayList<Table>(filteredTables);
	}

	/**
	 * Include table.
	 * 
	 * @param i
	 *            The position.
	 * @param alias
	 *            Table alias.
	 * @param stackTables
	 *            Stack Tables.
	 * @return True if ok, else false.
	 */
	private boolean includeTable(int i, String alias, List<String> stackTables) {
		boolean result = false;

		for (int j = 0; j < tables.size() && !result; j++) {
			if (i == j) {
				continue;
			}
			Table joinTable = tables.get(j);
			String joinTableAlias = joinTable.alias;
			if (null != joinTable.joinCond && joinTable.joinCond.indexOf(alias) > -1) {
				// It exists a join condition based on this table alias, we need to include it
				if ((DbConnection.getDbType() != Type.ORACLE) || clauseContainTable(whereConds, joinTableAlias)) {
					return true;
				} else if (!stackTables.contains(joinTableAlias)) {
					stackTables.add(joinTableAlias);
					result = includeTable(j, joinTableAlias, stackTables);
				}
			}
		}
		return result;
	}

	/**
	 * Ajout d'une classe.
	 * 
	 * @param entity
	 *            Classe à ajouter
	 * @param tableAlias
	 *            Alias de table <i>null</i>=Auto (Tnnn)
	 * @param addOutVars
	 *            indicates if variables of the entity are added.
	 * @return the Table created.
	 */
	private Table addOneClass(Entity entity, String tableAlias, boolean addOutVars) {
		// Tester l'alias de table (si fourni)
		String tAlias = "T" + (tables.size() + 1);
		if (tableAlias != null && tableAlias.length() > 0) {
			if (getTable(tableAlias) != null) {
				throw new TechnicalException("Alias " + tableAlias + " déjà défini.");
			} else {
				tAlias = tableAlias;
			}
		}

		// Ok, ajouter l'entité
		Table table = new Table(entity, tAlias);
		tables.add(table);

		// Ajout des variables de table
		EntityModel model = entity.getModel();
		Set<String> fields = model.getFields();
		for (String fieldName : fields) {
			EntityField field = model.getField(fieldName);

			if (!field.isFromDatabase()) {
				// Not a SQL variable, skip it
				continue;
			}
			if (addOutVars) {
				Var outVar = new Var(fieldName, null, table.alias, field);
				outVars.add(outVar);
			}

			Var inVar = new Var(fieldName, null, table.alias, field);
			inVars.add(inVar);
		}
		return table;
	}

	/**
	 * Ajouter une nouvelle classe class1Name d'alias class1Alias à la requête, liée avec le lien linkName à une classe d'alias class2Alias avec
	 * une jointure de type joinType.<br>
	 * équivalence 1.2: JOIN_TYPE_STRICT si la clé source du lien de jointure est obligatoire
	 * 
	 * @param e1Name
	 *            Nom de l'entité à ajouter
	 * @param e1Alias
	 *            Alias de l'entité à ajouter, <i>null</i>=Alias automatique
	 * @param linkName
	 *            Relation d'une classe existante vers l'entité rajoutée, <i>null</i>=Lien automatique
	 * @param e2Alias
	 *            Alias de la table liée, <i>null</i>=Alias automatique
	 * @param joinType
	 *            type de jointure, cf constantes JOIN_TYPE_xxx
	 * @param addOutVars
	 *            Add all entity variables to the select clause.
	 * @return The DbQuery.
	 */
	public DbQuery addEntity(String e1Name, String e1Alias, String linkName, String e2Alias, Join joinType, boolean addOutVars) {
		return addEntity(e1Name, e1Alias, linkName, e2Alias, joinType, addOutVars, null);
	}

	/**
	 * Ajouter une nouvelle classe class1Name d'alias class1Alias à la requête, liée avec le lien linkName à une classe d'alias class2Alias avec
	 * une jointure de type joinType.<br>
	 * équivalence 1.2: JOIN_TYPE_STRICT si la clé source du lien de jointure est obligatoire
	 * 
	 * @param e1Name
	 *            Nom de l'entité à ajouter
	 * @param e1Alias
	 *            Alias de l'entité à ajouter, <i>null</i>=Alias automatique
	 * @param linkName
	 *            Relation d'une classe existante vers l'entité rajoutée, <i>null</i>=Lien automatique
	 * @param e2Alias
	 *            Alias de la table liée, <i>null</i>=Alias automatique
	 * @param joinType
	 *            type de jointure, cf constantes JOIN_TYPE_xxx
	 * @param addOutVars
	 *            Add all entity variables to the select clause.
	 * @param joinCond
	 *            Condition to add on join clause.
	 * @return The DbQuery.
	 */
	public DbQuery addEntity(String e1Name, String e1Alias, String linkName, String e2Alias, Join joinType, boolean addOutVars,
			Cond joinCond) {
		Entity e1 = DomainUtils.newDomain(e1Name);
		// This is the query first table, we just need to add the table
		if (tables.isEmpty()) {
			addOneClass(e1, e1Alias, addOutVars);
			return this;
		}
		if (getTable(e1Name, e1Alias) != null && linkName == null) {
			// Table with this alias is already present in query, we log a warning and end the process.
			LOGGER.warn("DbQuery.addEntity: Table " + e1Alias + "." + e1Name + " already in query");
			return this;
		}
		// No Join, we add the table. It will produce a cartesian product.
		if (joinType == Join.NONE) {
			addOneClass(e1, e1Alias, addOutVars);
			return this;
		}
		/*
		 * Ce n'est pas la première classe : essayer de trouver si sa clé primaire est referencée qu'une seule fois dans l'une des entités déjà
		 * définies OU C'est la première classe d'une SubQuery avec un lien
		 */
		LinkModel uniqueLink = null;
		Entity srcEntity = null;
		String srcId = null;
		for (int i = 0; i < tables.size(); i++) {
			Table table = tables.get(i);
			for (String lnkName : table.entity.getModel().getLinkNames()) {
				LinkModel link = table.entity.getModel().getLinkModel(lnkName);
				if (link.isTransient()) {
					// Do not use virtual links
					continue;
				}
				// check links to class
				if (link.getRefEntityName().equals(e1Name)) {
					if (linkName == null) {
						if (uniqueLink != null) {
							LOGGER.warn("ToyDbQuery.addEntity: more than one link exists from query's classes. Class " + e1Name
									+ " added with link " + uniqueLink);
						} else {
							uniqueLink = link;
							srcEntity = table.entity;
							srcId = table.alias;
						}
					} else if (linkName.equals(lnkName) && (e2Alias == null || e2Alias.equals(table.alias))) {
						uniqueLink = link;
						srcEntity = table.entity;
						srcId = table.alias;
						break;
					}
				}
			}
			if (uniqueLink == null) {
				for (String lnkName : table.entity.getModel().getBackRefNames()) {
					LinkModel link = table.entity.getModel().getBackRefModel(lnkName);
					if (link.isTransient()) {
						// Do not use virtual links
						continue;
					}
					// check links from class
					if (link.getEntityName().equals(e1Name)) {
						if (linkName == null) {
							if (uniqueLink != null) {
								LOGGER.warn("DbQuery.addEntity: more than one link exists to query's classes. Class " + e1Name
										+ " added with link " + uniqueLink);
							} else {
								uniqueLink = link;
								srcEntity = table.entity;
								srcId = table.alias;
							}
						} else if (linkName.equals(lnkName) && (e2Alias == null || e2Alias.equals(table.alias))) {
							uniqueLink = link;
							srcEntity = table.entity;
							srcId = table.alias;
							break;
						}
					}
				}
			}
		}

		if (uniqueLink == null) {
			handleAddEntityException(linkName, e1Name, e2Alias);
		} else if (uniqueLink.getLinkName().equalsIgnoreCase(linkName) && getTable(e1Name, e1Alias) != null) {
			// entity already in query with same link
			LOGGER.warn("DbQuery.addEntity: Class " + (e1Alias == null ? "" : e1Alias + ".") + e1Name + ", link " + linkName
					+ " to/from alias " + e2Alias + " already exists in query.");
			return this;
		}

		// OK. Ajouter l'entité si la table existe deja, on rajoute que le lien
		Table table = getTable(e1Name, e1Alias);
		if (table == null) {
			table = addOneClass(e1, e1Alias, addOutVars);
			// Add join condition
			if (joinCond != null)
				table.userJoinConds.add(joinCond);
		}

		// Ajouter les liens
		KeyModel srcKey = null;
		KeyModel dstKey = null;
		if (uniqueLink.getRefEntityName().equals(e1Name)) {
			srcKey = srcEntity.getModel().getLinkModel(uniqueLink.getLinkName());
			dstKey = e1.getModel().getKeyModel();
		} else {
			srcKey = srcEntity.getModel().getKeyModel();
			dstKey = e1.getModel().getLinkModel(uniqueLink.getLinkName());
		}
		table.bExtJoin = (joinType == Join.LOOSE);
		table.joinedLinks.add(new JoinedLink(srcEntity, e1, srcId, dstKey, srcKey));
		return this;
	}
	
	/**
	 * An exception occured while adding a link to the query, this methods throws a TechnicalException that describes the error. <br/>
	 * Existing query is : SELECT * FROM TABLE_XXX e2Alias;  <br/>
	 * New query would be : SELECT * FROM TABLE_XXX e2Alias, e1Name e1Alias WHERE e1Alias.x = e2Alias.y;  <br/>
	 * 
	 * @param linkName	Link used to add e1 to the query
	 * @param e1Name	Entity we tried to add
	 * @param e2Alias	Table alias used to create the link
	 */
	private void handleAddEntityException(String linkName, String e1Name, String e2Alias) { 
		String message = "DbQuery.addEntity: Failed to add class " + e1Name; 
		if (linkName == null) {
			message += ", no link found to/from query's classes.";
		} else if (e2Alias == null) {
			message += ", link " + linkName + " not found in query's classes.";
		} else {
			message += ", link " + linkName + " to/from alias " + e2Alias + " not found in query's classes neither in the mother query.";
		}
		throw new TechnicalException(message);
	}

	/**
	 * retourne la table dont on connait l'alias utilisé dans la recherche d'une query mère.
	 * 
	 * @param tableAlias
	 *            alias de la table
	 * @return The Table.
	 */
	private Table getTable(String tableAlias) {
		for (Table t : tables) {
			if (t.alias.equals(tableAlias)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * retourne la table dont on connait le nom et l'alias.
	 * 
	 * @param className
	 *            nom de l'entité
	 * @param tableAlias
	 *            alias de l'entité, null si on ne le connait pas.
	 * @return The Table.
	 */
	private Table getTable(String className, String tableAlias) {
		for (Table t : tables) {
			if (t.entity.name().equalsIgnoreCase(className) && (tableAlias == null || tableAlias.equalsIgnoreCase(t.alias))) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Constructor.
	 * 
	 * @param entityName
	 *            première classe de la requête
	 * @param tableAlias
	 *            alias facultatif utilisé pour cette entité
	 */
	public DbQuery(String entityName, String tableAlias) {
		addEntity(entityName, tableAlias, Join.STRICT);
	}

	/**
	 * Constructor.
	 * 
	 * @param entityName
	 *            première classe de la requête
	 *            
	 * @deprecated use DbQuery(String entityName, String tableAlias) instead. tableAlias should always be present
	 */
	public DbQuery(String entityName) {
		this(entityName, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param ctx
	 *            current request context (not used)
	 * @param entityName
	 *            première classe de la requête
	 * @param tableAlias
	 *            alias facultatif utilisé pour cette entité
	 * 
	 * @deprecated use DbQuery(String entityName, String tableAlias) instead. RequestContext is not used anymore.
	 */
	@Deprecated
	public DbQuery(RequestContext ctx, String entityName, String tableAlias) {
		addEntity(entityName, tableAlias, Join.STRICT);
	}

	/**
	 * Constructor.
	 * 
	 * @param ctx
	 *            current request context (not used)
	 * @param entityName
	 *            première classe de la requête
	 * 
	 * @deprecated use DbQuery(String entityName, String tableAlias) instead. RequestContext is not used anymore.
	 */
	@Deprecated
	public DbQuery(RequestContext ctx, String entityName) {
		this(ctx, entityName, null);
	}

	/**
	 * Add a new entity to the query.
	 * 
	 * @see #addEntity(String, String, String, String, Join)
	 * @param entityName
	 *            Name of the entity.
	 * @param entityAlias
	 *            Alias of the entity.
	 * @param joinType
	 *            Type of join.
	 * @return The DbQuery.
	 */
	public DbQuery addEntity(String entityName, String entityAlias, Join joinType) {
		addEntity(entityName, entityAlias, null, null, joinType);
		return this;
	}

	/**
	 * Add a new entity to the query.
	 * 
	 * @param e1Name
	 *            Name of the entity to add
	 * @param e1Alias
	 *            Alias of the new table
	 * @param linkName
	 *            Link to use for join with previous tables of th query
	 * @param e2Alias
	 *            Alias of the table to use for join
	 * @param joinType
	 *            Join type
	 * @return The DbQuery.
	 */
	public DbQuery addEntity(String e1Name, String e1Alias, String linkName, String e2Alias, Join joinType) {
		addEntity(e1Name, e1Alias, linkName, e2Alias, joinType, true);
		return this;
	}

	/**
	 * Ajout d'une condition égal à une clé Remarque: Les variables à valeurs nulles ne sont pas ajoutées.
	 * 
	 * @param key
	 *            clé de comparaison
	 * @param tableAlias
	 *            alias
	 * @return The DbQuery.
	 */
	public DbQuery addCondKey(Key key, String tableAlias) {
		if (key == null) {
			// La clé est nulle, on ajoute pas de condition.
			return this;
		}
		for (String fieldName : key.getModel().getFields()) {
			if (key.getValue(fieldName) != null) {
				addCondEq(fieldName, tableAlias, key.getValue(fieldName));
			}
		}
		return this;
	}

	/**
	 * Ajout d'une condition d'égalité.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param valeur
	 *            valeur de comparaison
	 * @return The DbQuery.
	 */

	public DbQuery addCondEq(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_EQUAL, valeur, null);
		return this;
	}

	/**
	 * Ajout d'une condition de non égalité. Gére le cas des non null si valeur vaut null.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param valeur
	 *            valeur de comparaison
	 * @return The DbQuery.
	 */
	public DbQuery addCondNEq(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_N_EQUAL, valeur, null);
		return this;
	}

	/**
	 * Add a greater than condition.
	 * 
	 * @param colAlias
	 *            Alias of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param valeur
	 *            The value to compare with.
	 * @return The DbQuery.
	 */
	public DbQuery addCondGT(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_GREATER, valeur, null);
		return this;
	}

	/**
	 * Add a greater or equal condition.
	 * 
	 * @param colAlias
	 *            Alias of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param valeur
	 *            The value to compare with.
	 * @return The DbQuery.
	 */
	public DbQuery addCondGE(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_GREATER_OR_EQUAL, valeur, null);
		return this;
	}

	/**
	 * Add a lesser than condition.
	 * 
	 * @param colAlias
	 *            Alias of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param valeur
	 *            The value to compare with.
	 * @return The DbQuery.
	 */
	public DbQuery addCondLT(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_LESS, valeur, null);
		return this;
	}

	/**
	 * Add a lesser or equal condition.
	 * 
	 * @param colAlias
	 *            Alias of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param valeur
	 *            The value to compare with.
	 * @return The DbQuery.
	 */
	public DbQuery addCondLE(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_LESS_OR_EQUAL, valeur, null);
		return this;
	}

	/**
	 * Add a like condition.
	 * 
	 * @param colAlias
	 *            Alias of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param valeur
	 *            The value to compare with.
	 * @return The DbQuery.
	 */
	public DbQuery addCondLike(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_LIKE, valeur, null);
		return this;
	}

	/**
	 * Add a not like condition.
	 * 
	 * @param colAlias
	 *            Alias of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param valeur
	 *            The value to compare with.
	 * @return The DbQuery.
	 */
	public DbQuery addCondNLike(String colAlias, String tableAlias, Object valeur) {
		addCond(colAlias, tableAlias, SqlOp.OP_N_LIKE, valeur, null);
		return this;
	}

	/**
	 * Add a between condition.
	 * 
	 * @param colAlias
	 *            Alias of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param valeur1
	 *            The first value to compare with.
	 * @param valeur2
	 *            The second value to compare with.
	 * @return The DbQuery.
	 */
	public DbQuery addCondBetween(String colAlias, String tableAlias, Object valeur1, Object valeur2) {
		addCond(colAlias, tableAlias, SqlOp.OP_BETWEEN, valeur1, valeur2);
		return this;
	}

	/**
	 * Add a condition.
	 * 
	 * @param colAlias
	 *            Alias of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param op
	 *            The operator.
	 * @param valeur1
	 *            The first value to compare with.
	 * @param valeur2
	 *            The second value to compare with.
	 * @return The DbQuery.
	 */
	private DbQuery addCond(String colAlias, String tableAlias, SqlOp op, Object valeur1, Object valeur2) {
		whereConds.add(new Cond(colAlias, tableAlias, op, valeur1, valeur2));
		return this;
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
	 */
	public DbQuery addCond(String colAlias1, String tableAlias1, SqlOp operator, String colAlias2, String tableAlias2) {
		whereConds.add(new Cond(colAlias1, tableAlias1, operator, colAlias2, tableAlias2));
		return this;
	}

	/**
	 * Add a condition using a sub select. Ex: WHERE colA > (SELECT max(val) FROM table)<br>
	 * <b>WARNING : Use this method only if you understand want you do, this could crash or lead to bad performances</b>
	 * 
	 * @param colAlias1
	 *            First column alias
	 * @param tableAlias1
	 *            First column table
	 * @param operator
	 *            Sql operator between two columns (equals, greater than, etc.)
	 * @param subQuery
	 *            Subquery to use (can reference main query if you want)
	 */
	public DbQuery addCond(String colAlias1, String tableAlias1, SqlOp operator, DbQuery subQuery) {
		whereConds.add(new Cond(colAlias1, tableAlias1, subQuery, operator));
		return this;
	}

	/**
	 * Ajout d'une condition 'existe dans' subquery.
	 * 
	 * @param subquery
	 *            query de seléction de valeurs
	 * @param bIn
	 *            false=négation (NOT EXISTS)
	 * @return The DbQuery.
	 */
	public DbQuery addCondExists(DbQuery subquery, boolean bIn) {
		if (bIn) {
			whereConds.add(new Cond(subquery, SqlOp.OP_IN));
		} else {
			whereConds.add(new Cond(subquery, SqlOp.OP_N_IN));
		}
		return this;
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
	 * @return The DbQuery.
	 */
	public DbQuery addCondLikeConcat(List<String> colAliases, List<String> tableAliases, String paramValue, boolean bNot) {
		if (bNot) {
			whereConds.add(new Cond(colAliases, tableAliases, paramValue, SqlOp.OP_N_LIKE));
		} else {
			whereConds.add(new Cond(colAliases, tableAliases, paramValue, SqlOp.OP_LIKE));
		}
		return this;
	}

	/**
	 * Ajout d'une condition sur 'group by' (clause 'having') Pour l'opérateur Between, utilisez addHavingCondBetween.
	 * 
	 * @param colAlias
	 *            alias de la colonne
	 * @param tableAlias
	 *            alias de la table
	 * @param op
	 *            opérateur
	 * @param valeur
	 *            valeur de comparaison
	 * @return The DbQuery.
	 */
	public DbQuery addHavingCond(String colAlias, String tableAlias, SqlOp op, Object valeur) {
		if (op == SqlOp.OP_BETWEEN) {
			throw new TechnicalException("addHavingCond: illegal operator " + op.val + "Use addHavingCondBetween");
		}
		havingConds.add(new Cond(colAlias, tableAlias, op, valeur, null));
		return this;
	}

	/**
	 * Ajout d'une condition 'between' sur 'group by' (clause 'having').
	 * 
	 * @param colAlias
	 *            - alias de la colonne
	 * @param tableAlias
	 *            - alias de la table
	 * @param valeur1
	 *            - première valeur de comparaison
	 * @param valeur2
	 *            - seconde valeur de comparaison
	 * @return The DbQuery.
	 */
	public DbQuery addHavingCondBetween(String colAlias, String tableAlias, Object valeur1, Object valeur2) {
		havingConds.add(new Cond(colAlias, tableAlias, SqlOp.OP_BETWEEN, valeur1, valeur2));
		return this;
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
	 * @return The DbQuery.
	 */
	public DbQuery addCondInList(String colAlias, String tableAlias, List<?> objs) {
		return addCondInList(colAlias, tableAlias, objs, false);
	}

	/**
	 * Ajout d'une condition '(non) inclus dans' liste de valeurs.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param objs
	 *            liste de valeurs
	 * @param bNot
	 *            true=négation (NOT IN)
	 * @return The DbQuery.
	 */
	public DbQuery addCondInList(String colAlias, String tableAlias, List<?> objs, boolean bNot) {
		if (objs != null && objs.size() > 0) {
			SqlOp op = bNot ? SqlOp.OP_N_IN : SqlOp.OP_IN;
			whereConds.add(new Cond(colAlias, tableAlias, objs, op));
		}
		return this;
	}

	/**
	 * Ajout d'une condition IS NULL.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @param notNull
	 *            vrai pour tester IS NOT NULL, faux pour tester IS NULL
	 * @return The DbQuery.
	 */
	public DbQuery addCondIsNull(String colAlias, String tableAlias, boolean notNull) {
		if (notNull) {
			whereConds.add(new Cond(colAlias, tableAlias, SqlOp.OP_N_ISNULL, (Object) null, (Object) null));
		} else {
			whereConds.add(new Cond(colAlias, tableAlias, SqlOp.OP_ISNULL, (Object) null, (Object) null));
		}
		return this;
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
	 *            Négation de la condition. Si vrai --> NOT IN
	 * @return The DbQuery.
	 */
	public DbQuery addCondIn(String colAlias, String tableAlias, DbQuery subQuery, boolean bNot) {
		whereConds.add(new Cond(colAlias, tableAlias, subQuery, (bNot ? SqlOp.OP_N_IN : SqlOp.OP_IN)));
		return this;
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
	 * @return The DbQuery.
	 */
	public DbQuery addCondIn(String colAlias, String tableAlias, DbQuery subQuery) {
		addCondIn(colAlias, tableAlias, subQuery, false);
		return this;
	}

	/**
	 * Add a Sort.
	 * 
	 * @param colName
	 *            Name of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param direction
	 *            The direction ("ASC" or "DESC")
	 * @param firstInPos
	 *            if true, the sort is added at the beginning of the sort list.
	 * @param categorize
	 *            if categorized.
	 * @return The DbQuery.
	 */
	private DbQuery addSort(String colName, String tableAlias, String direction, boolean firstInPos, boolean categorize) {
		Var inVar = getInVar(colName, tableAlias);
		if (inVar == null) {
			// We didn't found var in inVars, we look in outVars with alias (used for "custom" sorts on expressions)
			inVar = findOutVar(colName, tableAlias);
		}
		if (inVar != null) {
			SortVar sVar = new SortVar(inVar, direction, categorize);
			if (firstInPos) {
				sortVars.remove(sVar);
				sortVars.add(0, sVar);
			} else if (!sortVars.contains(sVar)) {
				sortVars.add(sVar);
			}
		}
		return this;
	}

	/**
	 * Add a Sort by ASC.<br>
	 * Existing sort on same variable will not be replaced.
	 * 
	 * @param column
	 *            Name of the column.
	 * @return The DbQuery.
	 */
	public DbQuery addSortBy(String column) {
		addSort(column, null, ASC, false, false);
		return this;
	}

	/**
	 * Add a Sort by ASC.<br>
	 * Existing sort on same variable will not be replaced.
	 * 
	 * @param column
	 *            Name of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @return The DbQuery.
	 */
	public DbQuery addSortBy(String column, String tableAlias) {
		addSort(column, tableAlias, ASC, false, false);
		return this;
	}

	/**
	 * Add a Sort by DESC.<br>
	 * Existing sort on same variable will not be replaced.
	 * 
	 * @param column
	 *            Name of the column.
	 * @return The DbQuery.
	 */
	public DbQuery addSortByDesc(String column) {
		addSort(column, null, DESC, false, false);
		return this;
	}

	/**
	 * Add a Sort by DESC.<br>
	 * Existing sort on same variable will not be replaced.
	 * 
	 * @param column
	 *            Name of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @return The DbQuery.
	 */
	public DbQuery addSortByDesc(String column, String tableAlias) {
		addSort(column, tableAlias, DESC, false, false);
		return this;
	}

	/**
	 * Add a Sort.<br>
	 * Existing sort on same variable will not be replaced.
	 * 
	 * @param column
	 *            Name of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param direction
	 *            The direction ("ASC" or "DESC")
	 * @return The DbQuery.
	 */
	public DbQuery addSortBy(String column, String tableAlias, String direction) {
		addSortBy(column, tableAlias, direction, false);
		return this;
	}

	/**
	 * Add a Sort categorized.<br>
	 * Existing sort on same variable will not be replaced.
	 * 
	 * @param column
	 *            Name of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param direction
	 *            The direction ("ASC" or "DESC")
	 * @return The DbQuery.
	 */
	public DbQuery addCategorizedSortBy(String column, String tableAlias, String direction) {
		addSort(column, tableAlias, direction, false, true);
		return this;
	}

	/**
	 * Add a Sort.
	 * 
	 * @param column
	 *            Name of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @param direction
	 *            The direction ("ASC" or "DESC")
	 * @param firstInPos
	 *            if true, the sort is added at the beginning of the sort list.
	 * @return The DbQuery.
	 */
	public DbQuery addSortBy(String column, String tableAlias, String direction, boolean firstInPos) {
		if (!(ASC.equalsIgnoreCase(direction) || DESC.equalsIgnoreCase(direction))) {
			direction = ASC;
		}
		addSort(column, tableAlias, direction, firstInPos, false);
		return this;
	}

	/**
	 * check for variable and return Var instance if found.
	 * 
	 * @param colName
	 *            Name of the column.
	 * @param tableAlias
	 *            Alias of the table.
	 * @return Var instance if found
	 */
	public Var getInVar(String colName, String tableAlias) {
		if (colName != null)
			for (Var inVar : inVars) {
				if ((inVar.name.equals(colName) || inVar.name.equals(DomainUtils.createDbName(colName)))
						&& (tableAlias == null || inVar.tableId.equals(tableAlias))) {
					return inVar;
				}
			}
		return null;
	}

	/**
	 * Get the bind values.
	 * 
	 * @return the bind values.
	 */
	public List<Object> getBindValues() {
		return bindValues;
	}

	/**
	 * Ajouter une colonne.
	 * 
	 * @param varName
	 *            variable
	 * @param tableAlias
	 *            alias
	 * @return The DbQuery.
	 */
	public DbQuery addColumn(String varName, String tableAlias) {
		addColumn(varName, tableAlias, null, Visibility.VISIBLE);
		return this;
	}

	/**
	 * Ajouter une colonne à valeur constante.
	 * 
	 * @param name
	 *            Le nom de la colonne
	 * @param value
	 *            La valeur de la colonne
	 * @return The DbQuery.
	 */
	public DbQuery addColumnConst(String name, String value) {
		Const c = new Const();
		c.name = name;
		c.value = value;
		outConsts.add(c);
		return this;
	}

	/**
	 * Ajouter une colonne.
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param as
	 *            alias de sortie
	 * @return The DbQuery.
	 */
	public DbQuery addColumn(String varName, String tableAlias, String as) {
		addColumn(varName, tableAlias, as, Visibility.VISIBLE);
		return this;
	}

	/**
	 * Ajouter une colonne.
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param v
	 *            Visibility
	 * @return The DbQuery.
	 */
	public DbQuery addColumn(String varName, String tableAlias, Visibility v) {
		addColumn(varName, tableAlias, null, v);
		return this;
	}

	/**
	 * Ajouter une colonne.
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param as
	 *            alias de sortie
	 * @param v
	 *            Visibility
	 * @return The DbQuery.
	 */
	public Var addColumn(String varName, String tableAlias, String as, Visibility v) {
		Var outVar = findOutVar(varName, tableAlias);
		if (outVar == null) {
			throw new TechnicalException("addColumn: variable " + varName + " not found.");
		}

		if (firstAddColumn) {
			removeAllColumns();
			firstAddColumn = false;
		}

		if (v != null && v != Visibility.VISIBLE) {
			outVar.visibility = v;
		}

		if (!outVars.contains(outVar)) {
			outVars.add(outVar);
		}
		outVar.alias = as;
		return outVar;
	}

	/**
	 * Ajouter une colonne.
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param as
	 *            alias de sortie
	 * @param v
	 *            Visibility
	 * @param expr
	 *            expression sql
	 * @return The DbQuery.
	 */
	public Var addColumn(String varName, String tableAlias, String as, Visibility v, String expr) {
		Var outVar = addColumn(varName, tableAlias, as, v);

		if (expr != null && !expr.isEmpty()) {
			outVar.isGrouping = true;
			if (outVar.expr != null && outVar.expr.length() > 0) {
				outVar.expr = MessageFormat.format(expr, new Object[] { outVar.expr });
			} else {
				outVar.expr = MessageFormat.format(expr, new Object[] { outVar.tableId + "." + outVar.extern });
			}
		}
		return outVar;
	}

	/**
	 * Supprimer une colonne.
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @param as
	 *            alias de sortie
	 * @return The DbQuery.
	 */
	public DbQuery removeColumn(String varName, String tableAlias, String as) {
		List<Var> newOutVars = new ArrayList<DbQuery.Var>();
		for (Var v : outVars) {
			if (!v.name.equals(varName) || !v.tableId.equals(tableAlias)) {
				newOutVars.add(v);
			}
		}
		outVars = newOutVars;
		return this;
	}

	/**
	 * Enléve toutes les colonnes en sortie.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery removeAllColumns() {
		outVars.clear();
		groupByConds.clear();
		return this;
	}

	/**
	 * Ajoute toutes colonnes d'une table.
	 * 
	 * @param tableAlias
	 *            alias
	 * @return The DbQuery.
	 */
	public DbQuery addAllColumns(String tableAlias) {
		Table t = getTable(tableAlias);
		// Ajout des variables de table
		Set<String> fields = t.entity.getModel().getFields();
		for (String fieldName : fields) {
			Var outVar = new Var(fieldName, null, tableAlias, t.entity.getModel().getField(fieldName));
			if (!outVars.contains(outVar)) {
				outVars.add(outVar);
			}

			Var inVar = new Var(fieldName, null, tableAlias, t.entity.getModel().getField(fieldName));
			if (!inVars.contains(inVar)) {
				inVars.add(inVar);
			}
		}
		return this;
	}

	/**
	 * Enlève toutes les colonnes en sortie de la table.
	 * 
	 * @param tableAlias
	 *            alias
	 * @return The DbQuery.
	 */
	public DbQuery removeOutVars(String tableAlias) {
		for (int i = outVars.size() - 1; i >= 0; i--) {
			Var oV = outVars.get(i);
			if (oV.tableId.equals(tableAlias)) {
				outVars.remove(oV);
			}
		}
		return this;
	}

	/**
	 * Retourner une variable en sortie.
	 * 
	 * @param alias
	 *            alias de la colonne
	 * @param tableAlias
	 *            alias de la table (facultatif)
	 * @return The DbQuery.
	 */
	public Var findOutVarByAlias(String alias, String tableAlias) {
		for (Var var : outVars) {
			if (var.alias != null && aliash(var).equals(aliash(tableAlias + "_" + var.alias))) {
				return findOutVar(var.name, tableAlias);
			} else if (var.alias == null && alias != null && alias.equals(tableAlias + "_" + var.name)) {
				return findOutVar(var.name, tableAlias);
			}
		}
		throw new TechnicalException("findOutVar: '" + alias + "' invalid alias for table alias '" + tableAlias + "'");
	}

	/**
	 * Retourner une variable en sortie.
	 * 
	 * @param varName
	 *            variable de classe
	 * @param tableAlias
	 *            alias (facultatif)
	 * @return The DbQuery.
	 */
	public Var findOutVar(String varName, String tableAlias) {
		if (varName == null || varName.length() == 0) {
			throw new TechnicalException("findOutVar: invalid column");
		}

		Table table = null;
		// rechercher l'alias de table
		if (tableAlias != null && tableAlias.length() > 0) {
			table = getTable(tableAlias);
		}

		if (table == null) {
			return null;
		}

		if (table.entity.getModel().getField(varName) != null) {
			// find existing outvar
			Var o = new Var(varName, null, table.alias, table.entity.getModel().getField(varName));
			if (outVars.contains(o)) {
				return outVars.get(outVars.indexOf(o));
			} else if (table.entity.getModel().getField(varName) != null) {
				return o;
			}
		}
		return null;
	}

	/**
	 * Get the main Entity.
	 * 
	 * @return entité de la première table de la query
	 */
	public Entity getMainEntity() {
		if (tables.isEmpty()) {
			return null;
		}
		return (tables.get(0)).entity;
	}

	/**
	 * Get the alias of the main entity.
	 * 
	 * @return alias de la première table de la query
	 */
	public String getMainEntityAlias() {
		if (tables.isEmpty()) {
			return null;
		}
		return (tables.get(0)).alias;
	}

	/**
	 * Get an entity thanks to this alias.
	 * 
	 * @param alias
	 *            alias de la table
	 * @return nom de l'entité de la table
	 */
	public String getEntity(String alias) {
		if (alias == null || tables.isEmpty()) {
			return null;
		}
		for (Table t : tables) {
			if (alias.equals(t.alias)) {
				return t.entity.name();
			}
		}
		return null;
	}

	/**
	 * Récupère la liste des alias des tables.
	 * 
	 * @return The list of the table aliases.
	 */
	public List<String> getAliases() {
		List<String> aliases = new ArrayList<String>();
		for (Table t : tables) {
			aliases.add(t.alias);
		}
		return aliases;
	}

	/**
	 * Get the alias of an entity.
	 * 
	 * @param entityName
	 *            The Name of the Entity
	 * @return récupère l'alias d'une entité.
	 */
	public String getAlias(String entityName) {
		if (entityName == null || tables.isEmpty()) {
			return null;
		}
		for (Table t : tables) {
			if (entityName.equals(t.entity.name())) {
				return t.alias;
			}
		}
		return null;
	}

	@Override
	public DbQuery clone() {
		try {
			DbQuery query = (DbQuery) super.clone();

			query.indexes = new HashMap<String, Integer>();

			query.whereConds = new ArrayList<Cond>();
			for (Cond c : whereConds) {
				query.whereConds.add(c.clone());
			}
			query.havingConds = new ArrayList<Cond>();
			for (Cond c : havingConds) {
				query.havingConds.add(c.clone());
			}
			query.joinConds = new ArrayList<Cond>();
			for (Cond c : joinConds) {
				query.joinConds.add(c.clone());
			}
			query.groupByConds = new ArrayList<Cond>();
			for (Cond c : groupByConds) {
				query.groupByConds.add(c.clone());
			}
			query.tables = new ArrayList<DbQuery.Table>();
			for (DbQuery.Table t : tables) {
				query.tables.add(t.clone());
			}
			query.inVars = new ArrayList<DbQuery.Var>();
			for (DbQuery.Var v : inVars) {
				query.inVars.add(v.clone());
			}
			query.outVars = new ArrayList<DbQuery.Var>();
			for (DbQuery.Var v : outVars) {
				query.outVars.add(v.clone());
			}
			query.sortVars = new ArrayList<DbQuery.SortVar>();
			for (DbQuery.SortVar v : sortVars) {
				query.sortVars.add(v.clone());
			}
			query.bindValues = new ArrayList<Object>();
			for (Object o : bindValues) {
				query.bindValues.add(o);
			}
			query.outConsts = new ArrayList<DbQuery.Const>();
			for (DbQuery.Const c : outConsts) {
				query.outConsts.add(c);
			}
			return query;
		} catch (CloneNotSupportedException e) {
			throw new TechnicalException(e.getMessage(), e);
		}
	}

	/**
	 * Returns a list a expected variables from this query. They are the variable models, not the values. They do not always match with variables
	 * in an entity.
	 * 
	 * @return the list a expected variables from this query
	 */
	public List<DbQuery.Var> getOutVars() {
		return outVars;
	}

	/**
	 * Returns the expected variables from this query reduced to the one linked to the main entity.
	 * 
	 * @return the expected variables from this query reduced to the one linked to the main entity.
	 */
	public List<DbQuery.Var> getMainEntityOutVars() {
		List<Var> results = new ArrayList<DbQuery.Var>();
		for (DbQuery.Var var : getOutVars()) {
			if (var.tableId.equals(getMainEntityAlias())) {
				results.add(var);
			}
		}

		return results;
	}

	/**
	 * Set the max Row Num.
	 * 
	 * @param maxRownum
	 *            the max Row Num.
	 */
	public void setMaxRownum(int maxRownum) {
		this.maxRownum = maxRownum;
	}

	/**
	 * Get the max Row Num.
	 * 
	 * @return the max Row Num.
	 */
	public int getMaxRownum() {
		return maxRownum;
	}

	/**
	 * Le nom de la requête. Attention, pour les requêtes non nommées, ce nom peut être null.
	 * 
	 * @return Le nom de la requête s'il est renseigné, null sinon.
	 */

	public String getName() {
		return name;
	}

	/**
	 * Set the name of the query.
	 * 
	 * @param name
	 *            The Name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Indicates if the query is a count query.
	 * 
	 * @return The count Query.
	 */
	public boolean isCount() {
		return count;
	}

	/**
	 * Set if the query is a count query.
	 * 
	 * @param count
	 *            true for a count query.
	 */
	public void setCount(boolean count) {
		this.count = count;
	}

	/**
	 * Vérifie si la requete est NON sensible à la casse.
	 * 
	 * @return the caseInsensitiveSearch
	 */

	public boolean isCaseInsensitiveSearch() {
		return caseInsensitiveSearch;
	}

	/**
	 * Détermine si la requete est NON sensible à la casse.<br>
	 * - true : <b>NON</b> sensible<br>
	 * - false : sensible.
	 * 
	 * @param caseInsensitiveSearch
	 *            the caseInsensitiveSearch to set
	 */

	public void setCaseInsensitiveSearch(boolean caseInsensitiveSearch) {
		this.caseInsensitiveSearch = caseInsensitiveSearch;
	}

	/**
	 * Ajout d'une fonction de regroupement (GROUP BY)<br>
	 * Existing groupBy on same variable won't be replaced.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @return The DbQuery.
	 */

	public DbQuery addGroupBy(String colAlias, String tableAlias) {
		Cond c = new Cond(colAlias, tableAlias, null, null, null);
		if (!groupByConds.contains(c))
			groupByConds.add(c);
		return this;
	}

	/**
	 * Add a group by expression.
	 * 
	 * @param varName
	 *            Name of the Var.
	 * @param tableAlias
	 *            Alias of the Table.
	 * @param expr
	 *            Expression.
	 * @param as
	 *            Out Alias.
	 * @return The Var.
	 */
	private Var addGroupColumn(String varName, String tableAlias, String expr, String as) {
		Var outVar = addColumn(varName, tableAlias, as, Visibility.VISIBLE, expr);
		outVar.isGrouping = true;
		return outVar;
	}

	/**
	 * Ajouter une colonne utilisant la fonction AVG(colAlias).
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de classe (facultatif)
	 * @return The DbQuery.
	 */
	public DbQuery addAvg(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "avg({0})", null);
		return this;
	}

	/**
	 * Ajouter une colonne utilisant la fonction SUM(colAlias).
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de classe (facultatif)
	 * @return The DbQuery.
	 */
	public DbQuery addSum(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "sum({0})", null);
		return this;
	}

	/**
	 * Ajouter une colonne utilisant la fonction DISTINCT(colAlias).
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de classe (facultatif)
	 * @return The DbQuery.
	 */
	public DbQuery addDistinct(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "distinct({0})", null);
		return this;
	}

	/**
	 * Ajouter une fonction maximum.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @return The DbQuery.
	 */
	public DbQuery addMax(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "max({0})", null);
		return this;
	}

	/**
	 * Ajouter une fonction minimum.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias
	 * @return The DbQuery.
	 */
	public DbQuery addMin(String colAlias, String tableAlias) {
		addGroupColumn(colAlias, tableAlias, "min({0})", null);
		return this;
	}

	/**
	 * Ajout d'une colonne de comptage.
	 * 
	 * @param colName
	 *            nom de variable
	 * @param tableAlias
	 *            alias de table, <code>null</code>=facultatif
	 * @param asName
	 *            alias de la colonne
	 * @param bDistinct
	 *            <code>true</code>=count(distinct COLONNE) <code>false</code>=count(COLONNE) <br>
	 * @return The DbQuery.
	 */
	public DbQuery addCount(String colName, String tableAlias, String asName, boolean bDistinct) {
		Var outVar = null;
		if (bDistinct) {
			outVar = addGroupColumn(colName, tableAlias, "count(distinct {0})", asName);
		} else {
			outVar = addGroupColumn(colName, tableAlias, "count({0})", asName);
		}
		// override column type
		outVar.model = new EntityField(outVar.model.getSqlName(), SqlTypes.INTEGER, 10, 0, Memory.NO, true, false);
		return this;
	}

	/**
	 * Ajout d'une expression decode : decode (<b>tableAlias.colAlias</b>, <b>args[0]</b> {args[1], args[2], ...}) as <b>asName</b>.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de table, <code>null</code>=facultatif
	 * @param asName
	 *            alias de colonne
	 * @param args
	 *            Valeurs de decode
	 * @return The DbQuery.
	 */
	public DbQuery addDecode(String colAlias, String tableAlias, String asName, Object[] args) {
		if (args == null || args.length == 0) {
			throw new TechnicalException("addDecode() : missing decode values");
		}
		Var outVar = findOutVar(colAlias, tableAlias);
		boolean isValue = true;
		StringBuffer expr = new StringBuffer("decode({0}");
		for (int i = 0; i < args.length; i++) {
			if (isValue) {
				String function = SqlBuilder.parseDefaultValue(outVar, args[i]);
				if (null != function) {
					expr.append(", " + function);
				} else {
					expr.append(", ?");
					bindValues.add(SqlBuilder.parse(outVar, args[i]));
				}
			} else {
				expr.append(", " + args[i].toString());
			}
			isValue = !isValue;
		}
		expr.append(")");
		addColumn(colAlias, tableAlias, asName, Visibility.VISIBLE, expr.toString());
		return this;
	}

	/**
	 * Returns the appropriate ifNull equivalent depending on DB type.
	 * 
	 * @param connectionType
	 *            Database type (ORACLE, MySQL, PostgreSQL, etc.)
	 * @return name of the ifnull equivalent function to use (ifnull, nvl, coalesce, etc.)
	 */
	public String getIfNullFunction(Type connectionType) {
		if (DbConnection.getDbType() == Type.ORACLE) {
			return "nvl";
		} else if (DbConnection.getDbType() == Type.PostgreSQL) {
			return "COALESCE";
		} else {
			return "ifnull";
		}
	}

	/**
	 * Returns the appropriate toString equivalent depending on DB type.
	 * 
	 * @param columnExpr Column Expression to convert
	 * @return function to convert the given columnExpr to string (convert, TO_CHAR, CAST, etc.)
	 */
	public String getToStringFunction(String columnExpr, EntityField entityField) {
		switch (DbConnection.getDbType()) {
		case DB2:
			if (entityField.getSqlType() == SqlTypes.INTEGER) {
				// Integer
				return "CHAR(" + columnExpr + ")";
				
			} else if (entityField.getSqlType() == SqlTypes.DECIMAL) {
				// Number
				return "CHAR(" + columnExpr + ", ',')";
				
			} else if (entityField.getSqlType() == SqlTypes.DATE || entityField.getSqlType() == SqlTypes.TIME
					|| entityField.getSqlType() == SqlTypes.TIMESTAMP) {
				// Date
				String db2column = columnExpr;
				if (entityField.getSqlType() == SqlTypes.DATE || entityField.getSqlType() == SqlTypes.TIME) {
					// Need to convert date into timstamp
					db2column = "TIMESTAMP_ISO(" + db2column + ")";
				}
				return "VARCHAR_FORMAT(" + db2column + ", 'DD/MM/YYYY HH24:MI:SS')";
				
			} else {
				// Can't format
				return columnExpr;
			}
		case MySQL:
			return "CAST(" + columnExpr + " AS CHAR)";
		case ORACLE:
			return "TO_CHAR(" + columnExpr + ")";
		case PostgreSQL:
			String format = "";
			// Handle needed output format
			if (entityField.getSqlType() == SqlTypes.DECIMAL || entityField.getSqlType() == SqlTypes.INTEGER) {
				// Number
				format = StringUtils.repeat("9", entityField.getSqlSize());
				if (entityField.getSqlAccuracy() > 0) {
					int intPart = entityField.getSqlSize() - entityField.getSqlAccuracy();
					format = format.substring(0, intPart) + "D" + format.substring(intPart);
				}
				format = "S" + format;
			} else if (entityField.getSqlType() == SqlTypes.DATE
					 || entityField.getSqlType() == SqlTypes.TIME
					 || entityField.getSqlType() == SqlTypes.TIMESTAMP) {
				// Date
				format = "day DD/MM/YYYY HH:MI:SS";
			} else {
				// Can't format
				return columnExpr;
			}
			return "to_char(" + columnExpr + ", " + format + ")";
		case SQLSERVER:
			return "CONVERT(varchar(MAX), " + columnExpr + ")";
		}
		
		return columnExpr;
	}

	/**
	 * Ajout d'une expression ifnull : ifnull (<b>tableAlias.colAlias</b>, <b>valueIfNull</b>) as <b>asName</b>.
	 * 
	 * @param colAlias
	 *            nom de variable
	 * @param tableAlias
	 *            alias de table, <code>null</code>=facultatif
	 * @param asName
	 *            alias de colonne
	 * @param valueIfNull
	 *            valeur si null
	 * @return The DbQuery.
	 */
	public DbQuery addIfNull(String colAlias, String tableAlias, String asName, Object valueIfNull) {
		if (valueIfNull == null) {
			throw new TechnicalException("addIfNull() : missing value");
		}
		Var outVar = findOutVar(colAlias, tableAlias);
		String function = SqlBuilder.parseDefaultValue(outVar, valueIfNull);
		String ifNullFunction = getIfNullFunction(DbConnection.getDbType());
		String expr;
		if (null != function) {
			expr = ifNullFunction + "({0}, " + function + ")";
		} else {
			expr = ifNullFunction + "({0}, ?)";
			bindValues.add(SqlBuilder.parse(outVar, valueIfNull));
		}
		addColumn(colAlias, tableAlias, asName, Visibility.VISIBLE, expr.toString());
		return this;
	}

	/**
	 * Ajoute AND à la fin de clause WHERE.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery and() {
		whereConds.add(new Cond("AND"));
		return this;
	}

	/**
	 * Ajoute OR à la fin de clause WHERE.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery or() {
		whereConds.add(new Cond("OR"));
		return this;
	}

	/**
	 * Ajoute AND à la fin de clause HAVING.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery havingAnd() {
		havingConds.add(new Cond("AND"));
		return this;
	}

	/**
	 * Ajoute OR à la fin de clause HAVING.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery havingOr() {
		havingConds.add(new Cond("OR"));
		return this;
	}

	/**
	 * Ajoute une parenthése ouvrante en fin de clause WHERE.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery startGroupCondition() {
		whereConds.add(new Cond("("));
		return this;
	}

	/**
	 * Ajoute une parenthése en fin de clause WHERE.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery endGroupCondition() {
		whereConds.add(new Cond(")"));
		return this;
	}

	/**
	 * Ajoute une parenthése ouvrante en fin de clause HAVING.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery startHavingGroupCondition() {
		havingConds.add(new Cond("("));
		return this;
	}

	/**
	 * Ajoute une parenthése en fin de clause HAVING.
	 * 
	 * @return The DbQuery.
	 */
	public DbQuery endHavingGroupCondition() {
		havingConds.add(new Cond(")"));
		return this;
	}

	/**
	 * Indicates if the query is for update.
	 * 
	 * @return True for update.
	 */
	public boolean isForUpdate() {
		return forUpdate;
	}

	/**
	 * Set if the query is for update.
	 * 
	 * @param forUpdate
	 *            True for update.
	 */
	public void setForUpdate(boolean forUpdate) {
		this.forUpdate = forUpdate;
	}

	/**
	 * Reset the Sort.
	 */
	public void resetSort() {
		this.sortVars = new ArrayList<DbQuery.SortVar>();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Finds an out var from the query with.
	 * 
	 * @param columnAlias
	 *            Column alias which is (tableAlias_varName)
	 * @return Var corresponding to the alias, null if there's no such Var
	 */
	public Var getOutVar(String columnAlias) {
		if (columnAlias != null) {
			for (Var v : outVars) {
				if (columnAlias.equals(v.getColumnAlias())) {
					return v;
				}
			}
		}
		return null;
	}

	/**
	 * Lists all category break (order is important) for the query.
	 * 
	 * @return Columns on which we need to break the results for display
	 */
	public List<String> getCategoryBreak() {
		List<String> categoryBreak = new ArrayList<String>();
		for (SortVar s : sortVars) {
			if (s.categorize) {
				categoryBreak.add(s.inVar.getColumnAlias());
			}
		}
		return categoryBreak;
	}

	/**
	 * Get the min row num.
	 * 
	 * @return The min row num.
	 */
	public int getMinRownum() {
		return minRownum;
	}

	/**
	 * Set the min row num.
	 * 
	 * @param minRownum
	 *            The min rown num.
	 */
	public void setMinRownum(int minRownum) {
		this.minRownum = minRownum;
	}

	/**
	 * Indicates if the query is exposed as a web service.
	 * 
	 * @return True if the query is exposed as a web service.
	 */
	public boolean isExposedAsWebservice() {
		return isExposedAsWebservice;
	}

	/**
	 * Set if the query is exposed as a web service.
	 * 
	 * @param isExposedAsWebservice
	 *            True if the query is exposed as a web service.
	 */
	public void setExposedAsWebservice(boolean isExposedAsWebservice) {
		this.isExposedAsWebservice = isExposedAsWebservice;
	}

	/**
	 * Indicates if the clause contains a table.
	 * 
	 * @param clause
	 *            The Clause.
	 * @param tableAlias
	 *            The table to search.
	 * @return True if found.
	 */
	private boolean clauseContainTable(List<Cond> clause, String tableAlias) {
		for (Cond cond : clause) {
			if (cond.tableAliases.contains(tableAlias)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the fetchSize
	 */
	public int getFetchSize() {
		return fetchSize;
	}

	/**
	 * @param fetchSize
	 *            the fetchSize to set
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/**
	 * Check if any of the out vars is a grouping var
	 * 
	 * @return true|false
	 */
	public boolean hasGroupingColumn() {
		for (Var v : outVars) {
			if (v.isGrouping) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Columns from the Select clause.<br>
	 * if all columns are selected, the list is empty.
	 * 
	 * @return list of the select columns as pair of (tableAlias, varName)
	 */
	public List<Pair<String, String>> getColumnList() {
		List<Pair<String, String>> l = new ArrayList<Pair<String, String>>(outVars.size());
		for (Var var : outVars) {
			Pair<String, String> p = new ImmutablePair<String, String>(var.tableId, var.name);
			l.add(p);
		}
		return l;
	}

	/**
	 * Entities from the From clause.
	 * 
	 * @return list of the entities as pair of (tableAlias, entityName)
	 */
	public List<Pair<String, String>> getTableList() {
		List<Pair<String, String>> l = new ArrayList<Pair<String, String>>(tables.size());
		for (Table table : tables) {
			Pair<String, String> p = new ImmutablePair<String, String>(table.alias, table.entity.name());
			l.add(p);
		}
		return l;
	}

	/**
	 * Conditions from the Where clause.<br>
	 * 
	 * @return list of cloned conditions
	 */
	public List<Cond> getConditionList() {
		List<Cond> l = new ArrayList<Cond>(whereConds.size());
		for (Cond condition : whereConds) {
			l.add(condition.clone());
		}
		return l;
	}

	/**
	 * Conditions from the GroupBy clause.<br>
	 * 
	 * @return list of columns as pair of (tableAlias, columnAlias)
	 */
	public List<Pair<String, String>> getGroupByList() {
		List<Pair<String, String>> l = new ArrayList<Pair<String, String>>(groupByConds.size());
		for (Cond v : groupByConds) {
			l.add(new ImmutablePair<String, String>(v.tableAliases.get(0), v.colAliases.get(0)));
		}
		return l;
	}

	/**
	 * Columns from the Sortby clause.
	 * 
	 * @return list of columns as pair of (tableAlias, varName)
	 */
	public List<Pair<String, String>> getSortByList() {
		List<Pair<String, String>> l = new ArrayList<Pair<String, String>>(sortVars.size());
		for (SortVar table : sortVars) {
			l.add(new ImmutablePair<String, String>(table.inVar.alias, table.inVar.name));
		}
		return l;
	}
	
	/**
	 * Remove all where conditions from the query.
	 */
	public void resetWhereCond() {
		this.whereConds = new ArrayList<Cond>();
	}
	
	/**
	 * Sets key models to use on a specific join. This method may be useful to change a keys on a join from a table to itself.
	 * 
	 * @param tableAlias Alias of joined table
	 * @param joinAlias Alias of referenced table
	 * @param newSrcKey New source keyModel
	 * @param newDstKey New destination keyModel
	 */
	public void setJoinKeys(String tableAlias, String joinAlias, KeyModel newSrcKey, KeyModel newDstKey) {
		Table table = getTable(tableAlias);
		if (table == null) {
			throw new TechnicalException("DbQuery.setKeys: invalid table " + tableAlias + " in query " + name);
		}
		for (JoinedLink joinedLink : table.joinedLinks) {
			if (joinAlias != null && joinAlias.equals(joinedLink.srcAlias)) {
				joinedLink.srcKey = newSrcKey;
				joinedLink.dstKey = newDstKey;
				break;
			}
		}
	}

	/**
	 * Add a join condition on the given table
	 * 
	 * @param tableAlias Alias of the joined table
	 * @param colAlias Alias of the column
	 * @param op Condition operator
	 * @param valeur1 First value for the operator (null if not relevant)
	 * @param valeur2 Second value for the operator (null if not relevant)
	 * @return
	 */
	public DbQuery addJoinCond(String tableAlias, String colAlias, SqlOp op, Object valeur1, Object valeur2) {
		Table table = getTable(tableAlias);
		if (table == null) {
			throw new TechnicalException("DbQuery.addJoinCond: invalid table " + tableAlias + " in query " + name);
		}

		table.userJoinConds.add(new Cond(colAlias, tableAlias, op, valeur1, valeur2));
		return this;
	}
}
