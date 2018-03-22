module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-radio-button', [
      'commons',
      'ui.bootstrap'
    ])
    .component('varRadioButton', require('./var-radio-button.component.js'))
    .config(require('./var-radio-button.configuration.js'))
    .name;

}(window.angular));
