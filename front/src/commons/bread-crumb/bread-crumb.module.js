module.exports = (function(angular) {
  'use strict';

  return angular
    .module('bread-crumb', [
      require('../../core/core.module.js')
    ])
    .component('breadCrumb', require('./bread-crumb.component.js'))
    .constant('breadCrumb', require('./bread-crumb.constant.js'))
    .run(require('./bread-crumb.run.js'))
    .name;
}(window.angular));
