module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.global-search', [
      require('../../core/core.module.js'),
      'ui.bootstrap'
    ])
    .component('globalSearch', require('./global-search.component.js'))
    .config(require('./global-search.configuration.js'))
    .config(require('./global-search.translate.js'))
    .name;

}(window.angular));
