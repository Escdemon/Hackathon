module.exports = (function () {
  'use strict';

  varFormConfiguration.$inject = ['customServiceProvider'];

  return varFormConfiguration;

  /**
   * Adds custom methods for the component 'var-form'.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function varFormConfiguration(customServiceProvider) {
    var componentName = 'var-form';
    customServiceProvider.setNewFunction('entity-post-load', componentName, doNothing);
    customServiceProvider.setNewFunction('entity-unload', componentName, doNothing);
    customServiceProvider.setNewFunction('sub-action-visible', componentName, returnTrue);

    function doNothing() {
      return function() {};
    }

    function returnTrue() {
      return function() {
        return true;
      };
    }
  }
}());
