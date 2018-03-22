module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component.var-upload', [
      'commons',
      'ui.bootstrap',
      'ngFileUpload'
    ])
    .component('varUpload', require('./var-upload.component.js'))
    .config(require('./var-upload.configuration.js'))
    .config(require('./var-upload.translate.js'))
    .name;

}(window.angular));
