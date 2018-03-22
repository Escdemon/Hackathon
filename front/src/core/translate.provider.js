module.exports = (function() {
  'use strict';

  return TranslateProvider;

  function TranslateProvider() {
    var translateObjects = {};

    this.$get = service;
    this.addPart = addPart;

    service.$inject = ['$q'];
    function service ($q) {
      return getter;

      function getter(options) {
        var deferred = $q.defer();
        deferred.resolve(translateObjects[options.key]);
        return deferred.promise;
      }
    }

    function addPart(moduleName, languageKey, translateObject) {
      if (!translateObjects[languageKey]) {
        translateObjects[languageKey] = {};
      }
      translateObjects[languageKey][moduleName] = translateObject;
    }

  }
}());
