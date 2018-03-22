module.exports = (function() {
  'use strict';

  configuration.$inject = ['$httpProvider'];
  function configuration($httpProvider) {
    $httpProvider.interceptors.push('loginRouter');
  }

  return configuration;
}());
