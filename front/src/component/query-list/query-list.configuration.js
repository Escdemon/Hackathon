module.exports = (function () {
  'use strict';

  tableConfiguration.$inject = ['customServiceProvider'];

  return tableConfiguration;

  /**
   * Adds custom methods for the component 'query-list'.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function tableConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('list-readonly', 'query-list', isListReadOnly);
    customServiceProvider.setNewFunction('column-title', 'query-list', getColumnTitle);
    customServiceProvider.setNewFunction('column-visible', 'query-list', isColumnVisible);
    customServiceProvider.setNewFunction('cell-css', 'query-list', getCellCss);

    /**
     * Indicates whether the list is readonly.
     *
     * @returns (string, {}, {}) => boolean.
     */
    function isListReadOnly() {
      return function() {
        return false;
      };
    }

    /**
     * Retrieves the label displayed into the table header from the column key.
     *
     * @returns (string, {}, {}) => String.
     */
    function getColumnTitle() {
      return function(entityName, entity, params) {
        return params.columnKey;
      };
    }

    /**
     * Indicates whether the column is visible.
     *
     * @returns (string, {}, {}) => boolean.
     */
    function isColumnVisible() {
      return function() {
        return true;
      };
    }

    /**
     * Add class css to cell.
     * @return {Function}
     */
    function getCellCss() {
      return function() {};
    }

  }
}());
