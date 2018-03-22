module.exports = (function(angular) {
  'use strict';

  return angular
    .module('commons', [
      require('../core/core.module.js'),
      require('./bread-crumb/bread-crumb.module.js'),
      'pascalprecht.translate'
    ])
    .component('message', require('./message/message.component.js'))
    .factory('messageService', require('./message/message.service.js'))
    .factory('titleService', require('./title/title.service.js'))
    .config(require('./translate/commons.translate.js'))
    .directive('autoScroll', require('./ui/auto-scroll.directive.js'))
    .directive('autoFocus', require('./ui/auto-focus.directive.js'))
    .directive('updateTitle', require('./title/update-title.directive.js'))
    .name;
}(window.angular));
