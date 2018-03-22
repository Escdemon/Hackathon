module.exports = (function () {
  'use strict';

  homeTranslate.$inject = ['translateServiceProvider', 'App'];

  return homeTranslate;

  function homeTranslate(translateServiceProvider, App) {
    App.availableLocales.forEach(function (availableLocale) {
      translateServiceProvider.addPart(
        'home',
        availableLocale,
        require('./home.translate-' + availableLocale + '.json')
      );
    });
  }
}());
