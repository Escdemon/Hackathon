module.exports = (function () {
  'use strict';

  translate.$inject = ['translateServiceProvider', 'App'];

  return translate;

  function translate(translateServiceProvider, App) {
    App.availableLocales.forEach(function (availableLocale) {
      translateServiceProvider.addPart(
        'search-form',
        availableLocale,
        require('./search-form.translate-' + availableLocale + '.json')
      );
    });
  }
}());
