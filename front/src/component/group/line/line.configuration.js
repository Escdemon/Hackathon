module.exports = (function () {
  'use strict';

  lineConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../group-configuration.js');

  return lineConfiguration;

  /**
   * Adds custom methods for the component 'line'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function lineConfiguration(customServiceProvider) {
    commonConfig.addCommonFunctions(customServiceProvider, 'group-line');
  }

}());
