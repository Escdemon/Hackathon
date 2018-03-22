module.exports = (function() {
  'use strict';

  title.$inject = ['$rootScope'];
  return title;

  function title ($rootScope) {
    var service = {
      set: set
    };
    return service;
    function set(title) {
      $rootScope.title = title;
    }
  }
}(window.angular));
