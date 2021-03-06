module.exports = (function() {
  'use strict';

  dateConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../var-component.configuration.js');

  return dateConfiguration;

  /**
   * Adds custom methods for the component 'var-radio-button'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function dateConfiguration(customServiceProvider) {
    commonConfig(customServiceProvider, 'var-radio-button');
  }

}());
