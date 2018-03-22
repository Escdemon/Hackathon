module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-date', [
      'commons',
      'ui.bootstrap'
    ])
    .component('varDate', require('./var-date.component.js'))
    .config(require('./var-date.configuration.js'))
    .name;

}(window.angular));
