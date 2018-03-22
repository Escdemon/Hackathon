module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.string-long', [
      'commons',
      'ui.bootstrap'
    ])
    .component('stringLong', require('./string-long.component.js'))
    .config(require('./string-long.configuration.js'))
    .name;

}(window.angular));
