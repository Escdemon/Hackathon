module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-datetime', [
      'commons',
      'ui.bootstrap',
      'ui.bootstrap.datetimepicker'
    ])
    .component('varDatetime', require('./var-datetime.component.js'))
    .config(require('./var-datetime.configuration.js'))
    .name;

}(window.angular));
