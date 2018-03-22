module.exports = (function () {
  'use strict';

  return factory;

  function factory() {
    return Route;
    /**
     * Create new Route.
     * @param {Action} action action for route
     * @param {String|String[]} [pks] pks to go.
     * @param {Number} [idContext] context.
     * @constructor
     */
    function Route(action, pks, idContext) {
      /**
       * Create path like :
       * - entity/id/action
       * - entity/action
       * @returns {string} path of route
       * @function
       */
      this.path = function () {
        var queryParam = false;
        var path = '/' + action.entity.name.front + '/' + action.name.front;
        if (pks) {
          if (Array.isArray(pks)) {
            path += '?id=' + pks.join('&id=');
            queryParam = true;
          } else {
            path += '/' + pks;
          }
        }
        if (idContext !== undefined) {
          path += queryParam ? '&' : '?';
          path += idContext;
        }
        return path;
      };
    }
  }
}());
