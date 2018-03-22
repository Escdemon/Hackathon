module.exports = (function(angular) {
  'use strict';

  return angular
    .module('inner-template', [])
    .component('innerTemplate', require('./inner-template.component.js'))
    .name;

}(window.angular));
