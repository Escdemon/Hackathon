module.exports = (function() {
  'use strict';

  stringLongConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../var-component.configuration.js');

  return stringLongConfiguration;

  /**
   * Adds custom methods for the component 'string-long'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function stringLongConfiguration(customServiceProvider) {
    commonConfig(customServiceProvider, 'string-long');
  }

}());
