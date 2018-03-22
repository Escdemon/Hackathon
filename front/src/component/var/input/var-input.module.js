module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-input', [
      'commons',
      'ui.bootstrap'
    ])
    .component('varInput', require('./var-input.component.js'))
    .config(require('./var-input.configuration.js'))
    .name;

}(window.angular));
