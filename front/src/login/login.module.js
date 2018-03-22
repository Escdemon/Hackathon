module.exports = (function(angular) {
  'use strict';

  return angular
    .module('login', [
      'app.core',
      'ngRoute',
      'ngResource',
      'ui.bootstrap',
      'pascalprecht.translate'
    ])
    .factory('loginRouter', require('./login-router.service.js'))
    .component('login', require('./login.component.js'))
    .config(require('./login.route.js'))
    .config(require('./login.configuration.js'))
    .config(require('./login.translate.js'))
    .name;

}(window.angular));
