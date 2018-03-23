module.exports = (function(angular) {
  'use strict';

  return angular
    .module('display', [
      'app.core',
      // NUVIA
      require('./balise/balise.module.js'),
      require('./localisation/localisation.module.js'),
      require('./photo/photo.module.js')
    ])
    .config(require('./display.configuration'))
    .name;

}(window.angular));
