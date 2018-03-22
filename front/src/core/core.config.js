module.exports = (function() {
  'use strict';

  configuration.$inject = ['$translateProvider'];

  return configuration;

  function configuration($translateProvider) {
    $translateProvider.useLoader('translateService');
  }
}());
