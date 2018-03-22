# Translate

## Description

[commons.translate.js](./commons.translate.js) is a configuration file to register translations for genric messages. It does not exposes any public API.

## Translations
Translations are managed by [Angular translate](https://angular-translate.github.io/) and [Angular Dynamic Locale](https://github.com/lgalfaso/angular-dynamic-locale#angular-dynamic-locale) to change the user locale.

Each component or module may declare its translations. This is done by a configuration file generally named `component-name.translate.js`. This file registers translations using a file `component-name-translate-{locale}.json` for each available locale.

```javascript
// component-name.translate.js
module.exports = (function() {
  'use strict';

  // Injects angular-translate and the constant App
  componentNameTranslate.$inject = ['translateServiceProvider', 'App'];
  return componentNameTranslate;

  function componentNameTranslate(translateServiceProvider, App) {
    // Registers translations for each locale
    App.availableLocales.forEach(function(locale) {
      translateServiceProvider.addPart('component-name', locale, require('./component-name.translate-' + locale + '.json'));
    });
  }
}());

// component-name.translate-en.json
{
  "key-1"= "English message"
}

// component-name.translate-fr.json
{
  "key-1"= "Message français"
}

// Example
getTitle.$inject = ['$translate'];
function getTitle($translate) {
  return $translate('component-name.key-1');
}

```

See also [app.configuration.js](../../app/app.configuration.js).
