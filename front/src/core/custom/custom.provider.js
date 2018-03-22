module.exports = (function () {
  'use strict';

  return Provider;

  function Provider() {
    var implementations = {},
      defaultImplementations = {};

    this.$get = service;
    this.setNewFunction = setNewFunction;
    this.setImplementation = setImplementation;

    service.$inject = ['$injector'];

    function service($injector) {
      return {
        get: function (functionName) {
          return get(implementations[functionName], defaultImplementations[functionName]);
        }
      };
      function get(typeImplementation, defaultImplementations) {
        /**
         * Save getter to call.
         * @param {String} entityName name of entity
         * @param {String} type of component (var, link).
         * @param {Object} bean or beans.
         * @param {Object} params to select the function.
         */
        return function (entityName, type, bean, params) {
          var defaultGetter = defaultImplementations[type];
          var getter = defaultGetter;
          if (typeImplementation[type] && typeImplementation[type][entityName]) {
            getter = typeImplementation[type][entityName]
              .reduce(reduce, {impl: getter, checkValue: 0})
              .impl;
          }
          return $injector.invoke(getter)(entityName, bean, params, $injector.invoke(defaultGetter));

          function reduce(bestImpl, implToCheck) {
            var checkValue = checker(params, implToCheck.criteria);

            if (checkValue >= bestImpl.checkValue) {
              return {
                checkValue: checkValue,
                impl: implToCheck.impl
              };
            }
            return bestImpl;
          }
        };
      }
    }

    function setImplementation(functionName) {
      /**
       * Save getter to call.
       * @param {String} entityName name of entity
       * @param {String} type type of component (var, link).
       * @param {Function} getter function.
       * @param {Object} criteria object contains all criteria.
       */
      return function (entityName, type, getter, criteria) {
        implementations[functionName][type] = implementations[functionName][type] || {};
        implementations[functionName][type][entityName] = implementations[functionName][type][entityName] || [];
        implementations[functionName][type][entityName].push({impl: getter, criteria: criteria || {}});
      };
    }

    function setNewFunction(functionName, component, defaultImplementation) {
      implementations[functionName] = implementations[functionName] || {};
      defaultImplementations[functionName] = defaultImplementations[functionName] || {};
      defaultImplementations[functionName][component] = defaultImplementation;
    }

    /**
     * Return a number represent number of criteria match.
     * If no criteria given then return 0.
     * If all criteria haven't match then return -1.
     * If all criteria match then return number of criteria.
     * @param {Object} params params.
     * @param {Object} criteria criteria to check with params.
     * @return {Number} return -1, 0, or number of criteria match.
     */
    function checker(params, criteria) {
      if (!criteria || criteria === {}) {
        return 0;
      }
      var keys = Object.keys(criteria);
      var nbCriteria = keys.length;
      var criteriaMatch = 0;
      keys.forEach(function (nameParam) {
        if (criteria[nameParam] && params[nameParam] === criteria[nameParam]) {
          criteriaMatch++;
        }
      });
      if (criteriaMatch === nbCriteria) {
        return nbCriteria;
      }
      return -1;
    }
  }
}());
