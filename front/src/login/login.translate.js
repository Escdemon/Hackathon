module.exports = (function () {
  'use strict';

  translate.$inject = ['translateServiceProvider', 'App'];

  return translate;

  function translate(translateServiceProvider, App) {
    App.availableLocales.forEach(function (availableLocale) {
      translateServiceProvider.addPart(
        'login',
        availableLocale,
        require('./login.translate-' + availableLocale + '.json')
      );
    });
  }
}());

