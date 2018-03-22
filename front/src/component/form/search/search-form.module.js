module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.search-form', [
      require('../../../core/core.module.js'),
      'ui.bootstrap'
    ])
    .component('searchForm', require('./search-form.component.js'))
    .config(require('./search-form.configuration.js'))
    .config(require('./search-form.translate.js'))
    .name;

}(window.angular));
