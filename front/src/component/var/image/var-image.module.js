module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-image', [
      'commons',
      'ui.bootstrap',
      'ngFileUpload'
    ])
    .component('varImage', require('./var-image.component.js'))
    .config(require('./var-image.configuration.js'))
    .config(require('./var-image.translate.js'))
    .name;

}(window.angular));
