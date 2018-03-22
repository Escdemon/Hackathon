module.exports = (function(ng) {
  'use strict';

  return ng
    .module('component.group-line', [
      'commons',
      'ui.bootstrap'
    ])
    .component('groupLine', require('./line.component.js'))
    .config(require('./line.configuration.js'))
    .name;

}(window.angular));
