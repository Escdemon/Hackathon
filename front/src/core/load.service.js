module.exports = (function () {
  'use strict';

  loadFactory.$inject = ['restService', 'contextService', '$q'];
  return loadFactory;

  function loadFactory(restService, contextService, $q) {
    return {
      loadData: loadData
    };

    /**
     *
     * @param {Action} action to load data
     * @param {String[]} pks pks
     * @param {Object[]} [options] to give at query.
     */
    function loadData(action, pks, options) {
      var entity = action.entity;
      var previousData = contextService.getCurrent().getData();
      if (previousData) {
        return $q.when(previousData);
      }
      var result;
      if (action.hasLinkProcess()) {
        result = restService.query(entity, entity.name.front)
          .then(function(response) {
            return response.data.results;
          });
      } else if (action.hasSingleInput()) {
        result = restService
          .entity(action, pks[0])
          .then(function(response) {
            return response.data;
          });
      } else if (action.hasMultipleInput()) {
        result = restService
          .multipleEntity(pks, action)
          .then(function(response) {
            return response.data;
          });
      } else if (action.hasQueryInput()) {
        result = restService
          .list(action, {}, options)
          .then(function(response) {
            return response.data;
          });
      } else if (action.hasNoInput()) {
        result = restService
          .noInput(action)
          .then(function(response) {
            return response.data;
          });
      }
      return result.then(function(data) {
        var currentContext = contextService.getCurrent();
        currentContext.setData(data);
        var fnAfterLoad = currentContext.getFunction('afterBackLoad');
        if (fnAfterLoad) {
          fnAfterLoad(data);
        }
        return data;
      });
    }
  }
}());
