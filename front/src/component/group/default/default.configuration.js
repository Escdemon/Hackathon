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
    commonConfig.addCommonFunctions(customServiceProvider, 'group-default');
  }

}());
