module.exports = (function () {
  'use strict';

  actionConfiguration.$inject = ['customServiceProvider'];

  return actionConfiguration;

  /**
   * Adds custom methods for actions.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function actionConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('override-action', 'action', defaultAction);
    customServiceProvider.setNewFunction('menu-action', 'action', defaultAction);
    customServiceProvider.setNewFunction('cancel-action', 'action', cancelAction);
    customServiceProvider.setNewFunction('validate-action', 'action', validateAction);
    customServiceProvider.setNewFunction('custom-action', 'action', defaultAction);
    customServiceProvider.setNewFunction('next-action', 'action', nextAction);

    /**
     * A default action which does not do anything.
     *
     * @returns (string, {}, {}) => Promise.
     */
    function defaultAction($q) {
      return function() {
        return $q.when(undefined);
      };
    }
    defaultAction.$inject = ['$q'];

    /**
     * Indicates whether current action's cancellation should be performed.
     *
     * @returns (string, {}, {}) => Promise.
     */
    function cancelAction($q) {
      return function() {
        return $q.when(true);
      };
    }
    cancelAction.$inject = ['$q'];

    /**
     * Indicates whether the current action could be performed.
     *
     * @returns (string, {}, {}) => Promise.
     */
    function validateAction($q) {
      return function() {
        return $q.when(true);
      };
    }
    validateAction.$inject = ['$q'];

    /**
     * Retrieves the next action to prepare.
     *
     * @returns (string, {}, {}) => Promise.
     */
    function nextAction($q, entityModel) {
      return function(entityName, bean, params) {
        var model = entityModel.entity(entityName);
        if (model) {
          var currentAction = model.getAction(params.action.name.front);
          if (currentAction && currentAction.nextAction) {
            return $q.when({
              action: model.getAction(currentAction.nextAction)
            });
          }
        }
        return $q.when(undefined);
      };
    }
    nextAction.$inject = ['$q', 'entityModel'];

  }
}());
