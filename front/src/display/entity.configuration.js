module.exports = (function () {
  'use strict';

  return function (json) {
    configuration.$inject = ['entityModelProvider'];

    return configuration;

    function configuration(entityModelProvider) {
      entityModelProvider.addEntity(json);
    }
  };
}());
