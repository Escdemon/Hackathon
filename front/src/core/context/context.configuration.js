module.exports = (function () {
  'use strict';

  comboConfiguration.$inject = ['customServiceProvider'];

  return comboConfiguration;

  /**
   * Adds custom methods for the component 'var-combo'.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function comboConfiguration(customServiceProvider) {
    var componentName = 'context';
    customServiceProvider.setNewFunction('title', componentName, getTitle);
    customServiceProvider.setNewFunction('titleTooltip', componentName, getTitleTooltip);

    getTitle.$inject = ['$translate'];
    /**
     * Returns a function which retrieves the key of the label to display next to the comboBox.
     *
     * @returns (string, {}, {}, ()) => string.
     */
    function getTitle($translate) {
      return function(entityName, bean, params) {
        return $translate(params.action.label);
      };
    }

    getTitleTooltip.$inject = ['$translate'];
    /**
     * Returns a function which retrieves the key of the label to display next to the comboBox.
     *
     * @returns (string, {}, {}, ()) => string.
     */
    function getTitleTooltip($translate) {
      return function(entityName, bean, params) {
        return $translate(params.action.title);
      };
    }
  }
}());
