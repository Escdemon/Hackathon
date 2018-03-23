module.exports = (function(angular) {
  'use strict';

  return angular
    .module('app.photo', ['app.core'])
    .config(require('./photo.route.js'))
    .component('photo',
      require('./template/photo.component.js'))
    .config(require('./photo.translate.js'))
    .constant('photo', require('./photo.entity.json'))
    .config(require('../entity.configuration.js')(require('./photo.entity.json')))
    .name;
}(window.angular));
