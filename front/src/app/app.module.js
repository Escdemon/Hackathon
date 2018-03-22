module.exports = (function(angular) {
  'use strict';

  // Declare app level module which depends on views, and components
  return angular
    .module('app', [
      // shared modules
      require('../core/core.module.js'),
      require('../commons/commons.module.js'),

      // Feature
      require('../menu/menu.module.js'),
      require('../home/home.module.js'),
      require('../login/login.module.js'),
      require('../header/header.module.js'),
      require('../component/component.module.js'),
      require('../display/display.module.js'),
      require('../custom/custom.module.js')
    ])
    .constant('Url', require('./url.constant.js'))
    .config(require('./app.configuration.js'))
    .config(require('../core/action.configuration.js'))
    .name;
}(window.angular));
