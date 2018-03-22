module.exports = (function(angular) {
  'use strict';

  return angular
    .module('app.core.custom', [])
    .provider('customService', require('./custom.provider.js'))
    .name;

}(window.angular));
