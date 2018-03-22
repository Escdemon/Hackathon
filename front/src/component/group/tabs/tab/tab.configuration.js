module.exports = (function () {
  'use strict';

  tabConfiguration.$inject = ['customServiceProvider'];
  var cfg = require('../../group-configuration.js');

  return tabConfiguration;

  /**
   * Adds custom methods for the component 'tab'.
   * 
   * @param {customServiceProvider} customServiceProvider Service to register custom methods. 
   */
  function tabConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('title', 'tab', cfg.getTitleKey);
    customServiceProvider.setNewFunction('tooltip', 'tab', cfg.getTooltipKey);
    customServiceProvider.setNewFunction('visible', 'tab', cfg.isVisible);
  }

}());
