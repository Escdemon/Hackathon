module.exports = (function () {
  'use strict';

  commonsTranslate.$inject = ['translateServiceProvider', 'App'];

  return commonsTranslate;

  function commonsTranslate(translateServiceProvider, App) {
    App.availableLocales.forEach(function (availableLocale) {
        translateServiceProvider.addPart(
          'common',
          availableLocale,
          require('./commons.translate-' + availableLocale + '.json'));
      }
    );
  }
}());
