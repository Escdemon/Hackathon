module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.action', [
      'commons',
      'ui.bootstrap'
    ])
    .directive('action', require('./action.directive.js'))
    .component('actions', require('./actions.component.js'))
    .config(require('./action.configuration.js'))
    .config(require('./actions.configuration.js'))
    .name;

}(window.angular));
