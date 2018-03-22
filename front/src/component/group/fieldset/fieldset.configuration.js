module.exports = (function () {
  'use strict';

  fieldsetConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../group-configuration.js');

  return fieldsetConfiguration;

  /**
   * Registers custom methods for the component 'group-fieldset'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function fieldsetConfiguration(customServiceProvider) {
    var fieldset = 'group-fieldset';
    commonConfig.addCommonFunctions(customServiceProvider, fieldset);
    customServiceProvider.setNewFunction('collapsed', fieldset, isCollapsed);

    /**
     * Returns a function which indicates whether the fieldset is collapsed (the panel's body is hidden).
     * 
     * @returns (string, {}, {}) => Promise.
     */
    function isCollapsed($q) {
      return function() {
        return $q.when(true);
      };
    }
    isCollapsed.$inject = ['$q'];
  }

}());
