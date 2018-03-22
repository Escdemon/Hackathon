module.exports = (function () {
  'use strict';

  comboConfiguration.$inject = ['customServiceProvider'];

  return comboConfiguration;

  /**
   * Adds custom methods for the component 'var-combo'.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function comboConfiguration(customServiceProvider) {
    var componentName = 'link-combo';
    customServiceProvider.setNewFunction('label', componentName, getLabelKey);
    customServiceProvider.setNewFunction('tooltip', componentName, getTooltipKey);
    customServiceProvider.setNewFunction('mandatory', componentName, isMandatory);
    customServiceProvider.setNewFunction('protected', componentName, isProtected);
    customServiceProvider.setNewFunction('visible', componentName, isVisible);
    customServiceProvider.setNewFunction('option-label', componentName, getOptionLabel);

    /**
     * Returns a function which retrieves the key of the label to display next to the comboBox.
     *
     * @returns (string, {}, {}) => string.
     */
    function getLabelKey() {
      return function() {
        return undefined;
      };
    }

    /**
     * Returns a function which retrieves the key of the tooltip to display onto the comboBox.
     *
     * @returns (string, {}, {}) => string.
     */
    function getTooltipKey() {
      return function() {
        return undefined;
      };
    }

    /**
     * Returns a function which indicates whether the selection is mandatory.
     *
     * @returns (string, {}, {}) => boolean.
     */
    function isMandatory() {
      return function() {
        return false;
      };
    }

    /**
     * Returns a function which indicates whether the comboBox is protected (selection is not possible).
     *
     * @returns (string, {}, {}) => boolean.
     */
    function isProtected() {
      return function() {
        return false;
      };
    }

    /**
     * Returns a function which indicates whether the comboBox is visible.
     *
     * @returns (string, {}, {}) => boolean.
     */
    function isVisible() {
      return function() {
        return true;
      };
    }

    /**
     * Returns a function which retrieves the the label to display into comboBox option .
     *
     * @returns (string, {}, {}) => string.
     */
    function getOptionLabel() {
      return function(entityName, bean, params) {
        return params.row[params.prefix + '_internalCaption'] || params.row.internalCaption || params.row.primaryKey;
      };
    }

  }
}());
