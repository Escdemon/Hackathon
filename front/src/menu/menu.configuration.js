module.exports = (function () {
  'use strict';

  MenuConfiguration.$inject = ['customServiceProvider'];
  return MenuConfiguration;

  /**
   * Adds custom methods for the menu.
   *
   * @param {customServiceProvider} customServiceProvider Service to register custom methods.
   */
  function MenuConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('entries', 'menu', getEntries);
    customServiceProvider.setNewFunction('count-timer', 'menu', getTimer);

    /**
     * Returns a function which retrieves new entries to display into the menu.
     *
     * @returns (string, {}, {}) => Promise.
     */
    function getEntries($q) {
      return function () {
        return $q.when([]);
      };
    }

    getEntries.$inject = ['$q'];

    /**
     * Returns a function which retrieves the number of milliseconds
     * between two server call to get a menu counter.
     * 
     * It returns 300 000 by default, which means 5 minutes.
     * 
     * @returns (string, {}, {}) => Number
     */
    function getTimer() {
      return function() {
        return 1000 * 60 * 5;
      };
    }

  }
}());
