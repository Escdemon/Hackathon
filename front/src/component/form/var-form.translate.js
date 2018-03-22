module.exports = (function () {
  'use strict';

  translate.$inject = ['translateServiceProvider', 'App'];

  return translate;

  function translate(translateServiceProvider, App) {
    App.availableLocales.forEach(function (availableLocale) {
      translateServiceProvider.addPart(
        'var-form',
        availableLocale,
        require('./var-form.translate-' + availableLocale + '.json')
      );
    });
  }
}());
