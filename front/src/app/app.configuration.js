module.exports = (function() {
  'use strict';

  configuration.$inject = [
    '$locationProvider',
    '$routeProvider',
    '$translateProvider',
    '$httpProvider',
    'tmhDynamicLocaleProvider',
    'loginServiceProvider',
    'restServiceProvider',
    'Url',
    'App'];

  return configuration;

  function configuration(
    $locationProvider,
    $routeProvider,
    $translateProvider,
    $httpProvider,
    tmhDynamicLocaleProvider,
    loginServiceProvider,
    restServiceProvider,
    Url,
    App
  ) {
    $locationProvider.hashPrefix('!');

    // Registers a set of locale keys the app will work with. Use this method in combination with determinePreferredLanguage
    $translateProvider.registerAvailableLanguageKeys(App.availableLocales, App.aliasLanguage);
    // Tells angular-translate which language tag should be used as a result when determining the current browser language.
    // To know more on BCP 47 go to https://en.wikipedia.org/wiki/IETF_language_tag
    $translateProvider.uniformLanguageTag('bcp47');
    // Tells angular-translate to try to determine on its own which language key to set as preferred language.
    $translateProvider.determinePreferredLanguage();
    // Tells the module which of the registered translation tables to use when missing translations at initial startup by passing a language key.
    $translateProvider.fallbackLanguage(App.fallbackLocale);
    // Enable escaping of HTML
    $translateProvider.useSanitizeValueStrategy('sce');

    // Set path pattern of angular locale file
    tmhDynamicLocaleProvider.localeLocationPattern('i18n/angular-locale_{{locale}}.js');

    $routeProvider.otherwise({redirectTo: '/'});

    $httpProvider.interceptors.push('backendRouter');
    $httpProvider.interceptors.push('translateRouter');
    loginServiceProvider.setDefaultUrl(Url.backend);
    restServiceProvider.setBackendUrl(Url.backend);
  }
}());
