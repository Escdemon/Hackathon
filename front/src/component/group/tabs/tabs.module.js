module.exports = (function(ng) {
  'use strict';

  return ng
    .module('component.group-tabs', [
      'commons',
    ])
    .component('groupTabs', require('./tabs.component.js'))
    .config(require('./tabs.configuration.js'))
    .component('tab', require('./tab/tab.component.js'))
    .config(require('./tab/tab.configuration.js'))
    .name;

}(window.angular));
