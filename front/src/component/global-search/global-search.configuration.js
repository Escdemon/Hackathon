module.exports = (function() {
  'use strict';

  globalSearchConfiguration.$inject = ['customServiceProvider'];

  return globalSearchConfiguration;

  /**
   * Registers a new custom function to indicate whether searching is 'live' (on key press)
   * or not (the user has to click on a button to trigger the search).
   * The default implementation returns true.
   * 
   * @param {customServiceProvider} customServiceProvider Service to manage custom functions.
   */
  function globalSearchConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('live-search', 'global-search', defaultLiveSearch);

    function defaultLiveSearch() {
      return function() {
        return true;
      }
    }
  }

}());
