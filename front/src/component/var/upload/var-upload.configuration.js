module.exports = (function() {
  'use strict';

  uploadConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../var-component.configuration.js');

  return uploadConfiguration;

  /**
   * Adds custom methods for the component 'var-upload'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function uploadConfiguration(customServiceProvider) {
    commonConfig(customServiceProvider, 'var-upload');
    customServiceProvider.setNewFunction('uploadLabel', 'var-upload', getUploadLabelKey);
    customServiceProvider.setNewFunction('validation', 'var-upload', getValidationRules);

    /**
     * Returns a function which retrieves the key of the label to display into the upload component.
     * 
     * @returns (string, {}, {}) => Promise.
     */
    function getUploadLabelKey($q) {
      return function() {
        return $q.when('var-upload.upload');
      };
    }
    getUploadLabelKey.$inject = ['$q'];

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
        return {};
      };
    }

  }
}());
