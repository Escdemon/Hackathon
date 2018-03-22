module.exports = (function () {
  'use strict';

  /**
   * Returns a function which retrieves the key of the label to display into the panel heading.
   * 
   * @returns (string, {}, {}) => Promise.
   */
  function getTitleKey($q) {
    return function() {
      return $q.when(undefined);
    };
  }
  getTitleKey.$inject = ['$q'];

  /**
   * Returns a function which retrieves the key of the tooltip to display onto the panel heading.
   * 
   * @returns (string, {}, {}) => Promise.
   */
  function getTooltipKey($q) {
    return function() {
      return $q.when(undefined);
    };
  }
  getTooltipKey.$inject = ['$q'];

  /**
   * Returns a function which indicates whether the fieldset is protected.
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
   * Returns a function which indicates whether the fieldset is visible.
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
   * Registers custom methods for a group template component.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   * @param {string} componentType Component type.
   */
  function addCommonFunctions(customServiceProvider, componentType) {
    customServiceProvider.setNewFunction('title', componentType, getTitleKey);
    customServiceProvider.setNewFunction('tooltip', componentType, getTooltipKey);
    customServiceProvider.setNewFunction('isProtected', componentType, isProtected);
    customServiceProvider.setNewFunction('visible', componentType, isVisible);
  }

  return {
    addCommonFunctions: addCommonFunctions,
    getTitleKey: getTitleKey,
    getTooltipKey: getTooltipKey,
    isProtected: isProtected,
    isVisible: isVisible
  };

}());
