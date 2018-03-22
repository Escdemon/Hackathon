module.exports = (function () {
  'use strict';

  entityFactory.$inject = ['$translate', 'Action', 'Link'];
  return entityFactory;

  function entityFactory($translate, Action, Link) {
    return Entity;

    /**
     * Create entity.
     * @param {Object} entityJson json who describe entity
     * @param {String} entityJson.name.front name of action
     * @param {String} entityJson.name.back name of action(back view)
     * @param {Object[]} entityJson.actions all action of entity
     * @param {String[]} entityJson.pk all field of primary key
     * @param {Object[]} entityJson.pkMap all string to append for serialize primary key indexed by field of primary key
     * @param {Object[]} entityJson.links list represent link present on object
     * @param {String} entityJson.links.name.front name of link
     * @param {String} entityJson.links.name.back name of link(back view)
     * @param {Object} entityJson.links.fk list of entity linked fields indexed by entity corresponding field
     * @param {Object[]} entityJson.queries list represent query of entity
     * @param {String} entityJson.queries.name.front name of query
     * @param {String} entityJson.queries.name.back name of query(back view)
     *
     * @constructor
     */
    function Entity(entityJson) {
      var separatorFields = ',,,',
        separatorValueKey = ':::';
      this.name = entityJson.name;
      this.pk = entityJson.pk;

      this.init = init;

      this.getAction = getAction;

      this.getActions = getActions;

      this.getLink = getLink;

      this.getQuery = getQuery;

      this.allowedValues = allowedValues;

      this.getStringPrimaryKey = getStringPrimaryKey;

      this.extractPrimaryKey = extractPrimaryKey;

      this.getPrimaryKeyFromString = getPrimaryKeyFromString;

      this.isPrimaryKeyFull = isPrimaryKeyFull;
      
      this.translatedAllowedValues = translatedAllowedValues;

      var that = this,
        actions = {},
        actionsEntity = [],
        links = {},
        queries = [],
        _allowedValues = {};

      // Implementations

      /**
       * Get action by name
       * @type {String} actionName name required action.
       * @returns {Action|Undefined} return action if find or undefined.
       * @function
       */
      function getAction(actionName) {
        return actions[actionName];
      }

      /**
       * Get all allowed actions for entity.
       * @returns {Action[]} return actions.
       * @function
       */
      function getActions() {
        return actionsEntity;
      }

      /**
       * Get link by name
       * @type {String} LinkName name required link.
       * @returns {Link|Undefined} return link if find or undefined.
       * @function
       */
      function getLink(linkName) {
        return links[linkName];
      }

      /**
       * Get query by name
       * @type {String} queryName name required query.
       * @returns {Object|Undefined} return query if find or undefined.
       * @function
       */
      function getQuery(queryName) {
        return queries[queryName];
      }

      /**
       * Retrieves the allowed values for a property of this entity.
       * @param {string} varName Name of the property.
       * @returns {array} An array which contains the allowed values. It may be empty.
       */
      function allowedValues(varName) {
        return _allowedValues[varName] || [];
      }

      /**
       * Init the Entity to create Action and Link.
       * @function
       */
      function init() {
        entityJson.actions.forEach(function (jsonAction) {
          var action = new Action(jsonAction, that);
          actions[jsonAction.name.front] = action;
          actionsEntity.push(action);
        });
        entityJson.links.forEach(function (link) {
          links[link.name.front] = new Link(link, that);
        });
        entityJson.queries.forEach(function (query) {
          queries[query.name.front] = query;
        });
        _allowedValues = entityJson.allowedValues || {};
      }

      /**
       * Give serialize given fields pk.
       * @param {Object[]} fields fields to serialize.
       * @return {String|null} primary key serialized or null.
       */
      function getStringPrimaryKey(fields) {
        if (!fields || that.pk.length > fields.length) {
          return null;
        }
        var preparedFields = [];
        that.pk.forEach(function (fieldPk, index) {
          preparedFields.push(fieldPk + separatorValueKey + entityJson.pkMap[fieldPk] + fields[index]);
        });
        return preparedFields.join(separatorFields);
      }

      function extractPrimaryKey(entity) {
        return that.pk.reduce(function (pkFields, fieldName) {
          pkFields.push(entity[fieldName]);
          return pkFields;
        },[]);
      }

      /**
       * Deserialize primary key.
       * @param {String} strPk string primary key
       * @return {Object|null} primary key, or undefined if can't deserialize.
       */
      function getPrimaryKeyFromString(strPk) {
        var primaryKey = {};
        if (!strPk) {
          return null;
        }
        var strFields = strPk.split(separatorFields);
        strFields.forEach(function(strField) {
          var elementField = strField.split(separatorValueKey);
          if (2 === elementField.length) {
            var value = elementField[1].slice(1);
            if (elementField[1].startsWith('I')) {
              value = parseInt(value);
            }
            if (elementField[1].startsWith('B')) {
              value = JSON.parse(value.toLowerCase());
            }
            if (elementField[1].startsWith('F') || elementField[1].startsWith('L')) {
              value = parseFloat(value);
            }
            primaryKey[elementField[0]] = value;
          }
        });
        return {} === primaryKey ? null : primaryKey;
      }

      /**
       * Indicates whether the given entity holds a complete primary key.
       * @param {Object} entity Entity to check.
       * @returns {Boolean} `true` if all primary key fields are set.
       */
      function isPrimaryKeyFull(entity) {
        var pk = extractPrimaryKey(entity);
        return pk.findIndex(isNotSet) === -1;
      }

      /**
       * Indicates whether the parameter is not set.
       * @param {*} value Value to check.
       * @returns {Boolean} `true` if `value` is `undefined` or `null` or is a string and its length equals to `0`.
       */
      function isNotSet(value) {
        return value === undefined || value === null || (typeof value === 'string' && value.trim().length === 0);
      }

      /**
       * Creates an array containing an objet for each allowedValue of a variable (value and translated label)
       * @param {String} varName The concerned variable's name
       * @returns {object} Array containing an objet for each allowedValue of a variable (value and translated label)
       */
      function translatedAllowedValues(varName) {
        var entity = this;
        return allowedValues(varName).map(function(allowedValue) {
          return createTranslatedAllowedValue(entity.name.front, varName, allowedValue);
        });
      }

      /**
       * Creates an object containing the value and the translated label for an allowedValue for a variable
       * @param {String} entityName The concerned entity's name (in kebabCase)
       * @param {String} varName The concerned variable's name (in kebabCase)
       * @param {object} allowedValue An object which contains a code and a value.
       * @returns {object} An object with a value and a label.
       */
      function createTranslatedAllowedValue(entityName, varName, allowedValue) {
        var option = {
          value: allowedValue.value
        };
        // build the allowedValue's translation key
        var key = entityName + '.' + varName + '-' + allowedValue.code;
        // Service $translate is used here because the filter throws an error "infinite $digest Loop".
        $translate(key).then(function(translation) {
          option.label = translation;
        });
        return option;
      }
    }
  }
}());
