// Generic custom methods for variable components.
module.exports = (function () {
  'use strict';

  /**
   * Registers custom methods for a group template component.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   * @param {string} componentType Component type.
   */
  function addCommonFunctions(customServiceProvider, componentType) {
    customServiceProvider.setNewFunction('label', componentType, getKey('labelKey'));
    customServiceProvider.setNewFunction('placeholder', componentType, getKey('placeholderKey'));
    customServiceProvider.setNewFunction('tooltip', componentType, getKey('tooltipKey'));
    customServiceProvider.setNewFunction('mandatory', componentType, isMandatory);
    customServiceProvider.setNewFunction('isProtected', componentType, isProtected);
    customServiceProvider.setNewFunction('isReadOnly', componentType, isReadOnly);
    customServiceProvider.setNewFunction('visible', componentType, isVisible);
    customServiceProvider.setNewFunction('amount-option', componentType, amountOptions);
  }

  /**
   * Returns a function which retrieves the key of the name to display.
   *
   * @returns (string, {}, {}) => Promise.
   */
  function getKey(name) {
    getNameKey.$inject = ['$q'];
    return getNameKey;
    function getNameKey($q) {
      return function (entityName, bean, params) {
        return $q.when(params[name]);
      };
    }
  }

  /**
   * Returns a function which indicates whether the input is mandatory.
   * 
   * @returns (string, {}, {}) => Promise.
   */
  function isMandatory($q) {
    return function() {
      return $q.when(false);
    };
  }
  isMandatory.$inject = ['$q'];

  /**
   * Returns a function which indicates whether the component is protected.
   * 
   * @returns (string, {}, {}) => Promise.
   */
  function isProtected($q) {
    return function() {
      return $q.when(false);
    };
  }
  isProtected.$inject = ['$q'];
  
  /**
   * Returns a function which indicates whether the component is read only.
   * 
   * @returns (string, {}, {}) => Promise.
   */
  function isReadOnly($q) {
    return function() {
      return $q.when(false);
    };
  }
  isReadOnly.$inject = ['$q'];

  /**
   * Returns a function which indicates whether the component is visible.
   * 
   * @returns (string, {}, {}) => Promise.
   */
  function isVisible($q) {
    return function() {
      return $q.when(true);
    };
  }
  isVisible.$inject = ['$q'];

  /**
   * Returns a function which indicates the default behavior for amounts
   * Position can be 'left' or 'right', currency is a string representing the used currency
   * @returns {position, currency}
   */
  function amountOptions() {
    return function() {
      return {
        position: 'right',
        currency: '€'
      };
    };
  }

  return addCommonFunctions;

}());
