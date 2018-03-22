module.exports = (function(angular) {
  'use strict';

  return angular
    .module('home', [
      'app.core',
      require('../commons/commons.module.js'),
      require('../login/login.module.js'),
      'ui.bootstrap',
      'pascalprecht.translate'
    ])
    .component('home', require('./home.component.js'))
    .config(require('./home.route.js'))
    .config(require('./home.translate.js'))
    .name;

}(window.angular));
