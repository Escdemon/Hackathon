module.exports = (function (angular) {
  'use strict';

  return angular
    .module('core.model', [])
    .factory('Action', require('./action.class.js'))
    .factory('Link', require('./link.class.js'))
    .factory('Query', require('./query.class.js'))
    .factory('Entity', require('./entity.class.js'))
    .provider('entityModel', require('./entity-model.provider.js'))
    .name;
}(window.angular));
