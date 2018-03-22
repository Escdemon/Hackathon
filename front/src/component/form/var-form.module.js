module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-form', [
      require('../../core/core.module.js'),
      'ui.bootstrap'
    ])
    .component('varForm', require('./var-form.component.js'))
    .config(require('./var-form.configuration.js'))
    .config(require('./var-form.translate.js'))
    .name;

}(window.angular));
