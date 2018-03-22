module.exports = (function(ng) {
  'use strict';

  return ng
    .module('component.group-default', [
      'commons',
    ])
    .component('groupDefault', require('./default.component.js'))
    .config(require('./default.configuration.js'))
    .name;

}(window.angular));
