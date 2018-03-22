module.exports = (function (angular) {
  'use strict';

  return angular
    .module('app.core', [
      // Angular modules
      'ngRoute',
      'ngSanitize',
      'ngResource',
      // Cross application modules
      // 3rd party modules
      'ui.bootstrap',
      'pascalprecht.translate',
      'cmelo.angularSticky',
      'tmh.dynamicLocale',

      // Sub-module
      require('./model/model.module.js'),
      require('./custom/custom.module.js'),
      require('./context/context.module.js')
    ])
    .constant('App', require('./app.constant.js'))
    .factory('loadService', require('./load.service.js'))
    .factory('backendRouter', require('./backend-router.service.js'))
    .factory('translateRouter', require('./translate-router.service.js'))
    .factory('utilsService', require('./utils.service.js'))
    .factory('Route', require('./route.class.js'))
    .provider('restService', require('./rest.provider.js'))
    .provider('translateService', require('./translate.provider.js'))
    .provider('loginService', require('./login.provider.js'))
    .config(require('./action.configuration.js'))
    .config(require('./core.config.js'))
    .config(require('./core.translate'))
    .name;

}(window.angular));
