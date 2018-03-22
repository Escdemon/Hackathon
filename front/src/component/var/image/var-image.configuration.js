module.exports = (function() {
  'use strict';

  imageConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../var-component.configuration.js');

  return imageConfiguration;

  /**
   * Adds custom methods for the component 'var-image'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function imageConfiguration(customServiceProvider) {
    commonConfig(customServiceProvider, 'var-image');
    customServiceProvider.setNewFunction('validation', 'var-image', getValidationRules);

    /**
     * Returns a function which returns an object with validation rule. Example :
     * {
     *   size: {min: 10, max: '20MB'},
     *   width: {min: 100, max:10000},
     *   height: {min: 100, max: 300},
     *   ratio: '2x1',
     *   duration: {min: '10s', max: '5m'},
     *   pattern: '.jpg'
     * }
     * 
     * @returns (string, {}, {}) => object.
     */
    function getValidationRules() {
      return function() {
        return {
          pattern: 'image/*'
        };
      };
    }

  }
}());
