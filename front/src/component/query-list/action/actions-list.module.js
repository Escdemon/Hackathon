module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.query-list.actions-list', [])
    .component('actionsList', require('./actions-list.component.js'))
    .name;

}(window.angular));
