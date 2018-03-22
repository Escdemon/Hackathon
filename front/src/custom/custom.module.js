module.exports = (function (ng) {
  'use strict';

  return ng
    .module('custom', [
      require('../core/core.module.js')
    ])
    .config(require('./custom.configuration.js'))
    .name;
}(window.angular));
