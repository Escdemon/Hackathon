module.exports = (function(angular) {
  'use strict';

  return angular
    .module('menu', [
      'ui.bootstrap',
      'pascalprecht.translate'
    ])
    .directive('menu', require('./menu.directive.js'))
    .factory('counterService', require('./counter.service.js'))
    .config(require('./menu.configuration.js'))
    .config(require('./menu.translate.js'))    .name;

}(window.angular));
