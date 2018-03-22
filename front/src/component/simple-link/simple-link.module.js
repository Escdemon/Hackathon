module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.simple-link', [
      require('../../commons/commons.module.js'),
      'ui.bootstrap'
    ])
    .component('simpleLink', require('./simple-link.component.js'))
    .config(require('./simple-link.configuration'))
    .name;

}(window.angular));
