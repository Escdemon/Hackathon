module.exports = (function () {
  'use strict';

  translate.$inject = ['translateServiceProvider', 'App'];

  return translate;

  function translate(translateServiceProvider, App) {
    App.availableLocales.forEach(function (availableLocale) {
      translateServiceProvider.addPart(
        'query-list',
        availableLocale,
        require('./query-list.translate-' + availableLocale + '.json')
      );
    });
  }
}());

