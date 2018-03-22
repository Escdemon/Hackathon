module.exports = (function () {
  'use strict';

  actionConfiguration.$inject = ['customServiceProvider'];

  return actionConfiguration;

  /**
   * Adds custom methods for the component 'action'.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function actionConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('css', 'action', get('css'));
    customServiceProvider.setNewFunction('icon', 'action', get('icon'));
    customServiceProvider.setNewFunction('label', 'action', get('label'));
    customServiceProvider.setNewFunction('tooltip', 'action', get('tooltip'));

    /**
     * Return function to get function to get default value.
     * @param {String} name name into param to return value.
     * @return {Function}
     */
    function get(name) {
      return function() {
        return function(entityName, bean, params) {
          return params[name];
        };
      };
    }
  }
}());
