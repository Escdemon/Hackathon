module.exports = (function () {
  'use strict';

  headerConfiguration.$inject = ['customServiceProvider'];

  return headerConfiguration;

  /**
   * Adds custom methods for the component 'header'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function headerConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('user-avatar', 'var-header', getAvatarUrl);

    /**
     * Returns a function which retrieves the url for the user avatar to display in header
     * 
     * @returns (string, {}, {}) => string.
     */
    function getAvatarUrl() {
      return function() {
        return undefined;
      };
    }
  }
}());
