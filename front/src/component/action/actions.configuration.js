module.exports = (function () {
  'use strict';

  actionsConfiguration.$inject = ['customServiceProvider'];

  return actionsConfiguration;

  /**
   * Adds custom methods for the component 'actions'.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function actionsConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('max-display', 'actions', getMax);

    /**
     * Return max actions to display.
     * Example :
     * max is 2 with 4 actions, 2 actions are display + 1 button to show list.
     * max is 2 with 3 actions, 3 actions are display
     * max is 2 with 2 actions, 2 actions are display
     *
     * @returns (string, {}, {}) => boolean.
     */
    function getMax() {
      return function(entityName, bean, params) {
        return params.maxDisplay;
      };
    }
  }
}());
