module.exports = (function(angular) {
  'use strict';

  return angular
    .module('app.core.context', [
      require('../custom/custom.module.js')
    ])
    .factory('Context', require('./context.class.js'))
    .factory('contextService', require('./context.service.js'))
    .config(require('./context.configuration.js'))
    .name;

}(window.angular));
