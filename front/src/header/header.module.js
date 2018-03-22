module.exports = (function(angular) {
  'use strict';

  return angular
    .module('header', [
      'app.core'
    ])
    .component('varHeader', require('./var-header.component.js'))
    .config(require('./var-header.configuration.js'))
    .config(require('./header.translate.js'))
    .name;
}(window.angular));
