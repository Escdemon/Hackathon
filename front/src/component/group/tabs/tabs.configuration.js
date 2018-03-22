module.exports = (function () {
  'use strict';

  defaultConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../group-configuration.js');

  return defaultConfiguration;

  /**
   * Adds custom methods for the component 'default'.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function defaultConfiguration(customServiceProvider) {
    commonConfig.addCommonFunctions(customServiceProvider, 'group-tabs');
    customServiceProvider.setNewFunction('tabToOpen', 'group-tabs', getTabToOpen);

    getTabToOpen.$inject = ['$q'];
    /**
     * Returns a function which retrieves the index of the tab to open while the template is loaded.
     *
     * @returns (string, {}, {}) => Promise.
     */
    function getTabToOpen($q) {
      return function () {
        return $q.when(0);
      };
    }
  }
}());
