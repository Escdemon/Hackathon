module.exports = (function(ng) {
  'use strict';

  return ng
    .module('component.group-fieldset', [
      'commons',
      'ui.bootstrap'
    ])
    .component('groupFieldset', require('./fieldset.component.js'))
    .config(require('./fieldset.configuration.js'))
    .name;

}(window.angular));
