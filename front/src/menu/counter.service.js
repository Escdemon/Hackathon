module.exports = (function () {
  'use strict';

  CounterFactory.$inject = ['$http', '$q', 'customService', 'Url'];
  return CounterFactory;

  function CounterFactory($http, $q, customService, Url) {
    var cache = {};

    return {
      count: count,
      invalidate: invalidate
    };

    /**
     * 
     * 
     * @param {string} entityName
     * @param {string} queryName
     * @returns 
     */
    function count(entityName, queryName) {
      var entityCache = cache[entityName];

      if (!entityCache) {
        entityCache = {};
        cache[entityName] = entityCache;
      }
      var entry = entityCache[queryName];

      if (entry && entry.expiration > new Date().getTime()) {
        return entry.count;
      }

      var timer = entry ? entry.timer : customService.get('count-timer')(entityName, 'menu', {}, {queryName: queryName});
      var expiration = new Date().getTime() + timer;
      entityCache[queryName] = {
        expiration: expiration,
        timer: timer
      };

      $http({
        url: Url.backend + entityName + '/query-counts',
        method: 'GET',
        params: {
          'q': [queryName]
        }
      }).then(function(response) {
        if (response.data) {
          entityCache[queryName].count = response.data[queryName.toUpperCase().replace(/-/g, '_')];
        }
      });
    }

    /**
     * Removes entries from the cache.
     * If the query name is not provided, all queries for the given entity are removed.
     * 
     * @param {string} entityName Entity name.
     * @param {string} queryName Query name.
     */
    function invalidate(entityName, queryName) {
      // No entity name, nothing to do.
      if (!entityName) {
        return;
      }
      var entityCache = cache[entityName];
      if (entityCache) {
        if (queryName !== undefined) {
          entityCache[queryName] = undefined;
        } else {
          // Removes all queries for the given entity.
          cache[entityName] = undefined;
        }
      }
    }

  }
}());
