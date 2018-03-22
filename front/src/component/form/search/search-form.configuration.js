module.exports = (function() {
  'use strict';

  searchFormConfiguration.$inject = ['customServiceProvider'];

  return searchFormConfiguration;

  /**
   * Registers a new custom function to indicate whether searching is 'live' (on key press)
   * or not (the user has to click on a button to trigger the search).
   * The default implementation returns true.
   *
   * @param {customServiceProvider} customServiceProvider Service to manage custom functions.
   */
  function searchFormConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('load-data', 'search-form', defaultLoadData);

    defaultLoadData.$inject = ['$q'];
    function defaultLoadData($q) {
      return function(entityName, bean) {
        return $q.when(bean);
      };
    }
  }

}());
