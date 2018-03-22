module.exports = (function(ng) {
  'use strict';

  return ng
    .module('component.combo', [
      'commons',
      'ui.bootstrap'
    ])
    .component('varCombo', require('./var-combo.component.js'))
    .config(require('./var-combo.configuration.js'))
    .component('linkCombo', require('./link-combo.component.js'))
    .config(require('./link-combo.configuration.js'))
    .name;

}(window.angular));
