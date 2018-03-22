module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-time', [
      'commons',
      'ui.bootstrap',
      'ui.bootstrap.timepicker'
    ])
    .component('varTime', require('./var-time.component.js'))
    .config(require('./var-time.configuration.js'))
    .name;

}(window.angular));
