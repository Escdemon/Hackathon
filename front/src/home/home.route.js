module.exports = (function() {
  'use strict';

  homeRoute.$inject = ['$routeProvider'];
  return homeRoute;

  function homeRoute ($routeProvider) {
    $routeProvider.when('/', {
      template: '<home></home>'
    });
  }
}());
