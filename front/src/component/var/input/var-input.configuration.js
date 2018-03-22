module.exports = (function() {
  'use strict';

  inputConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../var-component.configuration.js');

  return inputConfiguration;

  /**
   * Adds custom methods for the component 'var-input'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function inputConfiguration(customServiceProvider) {
    commonConfig(customServiceProvider, 'var-input');
  }

}());
