module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-label', [
      require('../../commons/commons.module.js'),
      'ui.bootstrap'
    ])
    .component('varLabel', require('./var-label.component.js'))
    .config(require('./var-label.configuration.js'))
    .name;

}(window.angular));
