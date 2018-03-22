module.exports = (function(angular) {
  'use strict';

  return angular
    .module('app.localisation', ['app.core'])
    .config(require('./localisation.route.js'))
    .component('localisation',
      require('./template/localisation.component.js'))
    .config(require('./localisation.translate.js'))
    .constant('localisation', require('./localisation.entity.json'))
    .config(require('../entity.configuration.js')(require('./localisation.entity.json')))
    .name;
}(window.angular));
