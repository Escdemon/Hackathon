module.exports = (function () {
  'use strict';

  comboConfiguration.$inject = ['customServiceProvider'];
  var commonConfig = require('../var/var-component.configuration.js');

  return comboConfiguration;

  /**
   * Adds custom methods for the component 'var-combo'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function comboConfiguration(customServiceProvider) {
    var comboType = 'var-combo';
    commonConfig(customServiceProvider, comboType);
    customServiceProvider.setNewFunction('options', comboType, initOptions);

    /**
     * Retrieves the function used to initialize the available options into the combo-box.
     * 
     * This implementation returns a function which returns the allowed values for the given entity/variable.
     * 
     * 
     * @param {Object} $q Angular service to manage Promise.
     * @param {Object} entityModel Service to get the entity model.
     * @returns (string, {}, {}, Function) => Promise
     * @see Entity.translatedAllowedValues
     */
    function initOptions($q, entityModel) {
      return function(entityName, bean, params) {
        var entity = entityModel.entity(entityName);
        var allowedValues = entity.translatedAllowedValues(params.varName);
        return $q.when(allowedValues);
      };
    }
    initOptions.$inject = ['$q', 'entityModel'];

  }

}());
