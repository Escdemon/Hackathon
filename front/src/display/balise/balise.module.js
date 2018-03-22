module.exports = (function(angular) {
  'use strict';

  return angular
    .module('app.balise', ['app.core'])
    .config(require('./balise.route.js'))
    .component('balise',
      require('./template/balise.component.js'))
    .config(require('./balise.translate.js'))
    .constant('balise', require('./balise.entity.json'))
    .config(require('../entity.configuration.js')(require('./balise.entity.json')))
    .name;
}(window.angular));
