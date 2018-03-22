module.exports = (function () {
  'use strict';
  return displayRoute;

  /**
   * @param {String} template template name.
   * @param {String} entity entity name.
   * @param {String} action action name.
   * @param {String} [nextAction] next action name.
   *
   * @returns {Object} Object who's describe route.
   */
  function displayRoute(template, entity, action, nextAction) {
    dataLoad.$inject = ['$route', 'loadService', 'entityModel'];

    var resolve = {
      data: dataLoad,
      cache: ['loadService', function (loadService) {
        return loadService.loadCache();
      }],
      action: ['$q', 'entityModel', function ($q, entityModel) {
        return $q.when(entityModel.action(entity, action));
      }],
      fromBefore: ['contextService', function (contextService) {
        return contextService.fromBefore();
      }],
      fromNext: ['contextService', function (contextService) {
        return contextService.fromNext();
      }]
    };

    if (nextAction) {
      resolve['next-action'] = ['$q', 'entityModel', function ($q, entityModel) {
        return $q.when(entityModel.action(entity, nextAction));
      }];
    }

    return {
      template: template,
      resolve: resolve
    };

    function dataLoad($route, loadService, entityModel) {
      var entityObj = entityModel.entity(entity);
      var objAction = entityObj.getAction(action);
      var additionalParams;
      if (objAction.hasMultipleInput()) {
        var id = $route.current.params.id;
        if (!Array.isArray(id)) {
          id = [id];
        }
        additionalParams = {ids: id};
      } else if (objAction.hasSingleInput()) {
        additionalParams = {id: $route.current.params.id};
      }
      return loadService.loadData(
        entityObj,
        objAction,
        additionalParams
      );
    }
  }
}());
