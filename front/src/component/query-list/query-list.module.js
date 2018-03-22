module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.query-list', [
      require('./action/actions-list.module'),
      'cmelo.angularSticky',
      'ui.bootstrap'
    ])
    .directive('queryList', require('./query-list.directive.js'))
    .component('column', require('./column/column.component.js'))
    .config(require('./query-list.configuration.js'))
    .config(require('./query-list.translate.js'))
    .name;

}(window.angular));
