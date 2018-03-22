module.exports = (function () {
  'use strict';

  simpleLinkConfiguration.$inject = ['customServiceProvider'];

  return simpleLinkConfiguration;

  function simpleLinkConfiguration(customServiceProvider) {
    customServiceProvider.setNewFunction('value', 'link', defaultValue);
    customServiceProvider.setNewFunction('label', 'link', defaultTranslate('label'));
    customServiceProvider.setNewFunction('tooltip', 'link', defaultTranslate('tooltip'));
    customServiceProvider.setNewFunction('placeholder', 'link', defaultTranslate('placeholder'));
    customServiceProvider.setNewFunction('protected', 'link', defaultReturnValue('isProtected'));
    customServiceProvider.setNewFunction('hidden', 'link', defaultReturnFalse);
    customServiceProvider.setNewFunction('mandatory', 'link', defaultReturnValue('mandatory'));

    defaultValue.$inject = ['$q'];
    function defaultValue($q) {
      return function (entityName, bean, params) {
        if (bean) {
          return $q.when(bean[params.prefix + '_internalCaption'] || bean.internalCaption || bean.primaryKey);
        }
        return $q.when('');
      };
    }

    function defaultTranslate(variable) {
      defaultVar.$inject = ['$translate'];
      return defaultVar;
      function defaultVar($translate) {
        return function (entityName, entity, param) {
          return $translate(param[variable], entity);
        };
      }
    }

    function defaultReturnValue(name) {
      defaultToReturn.$inject = ['$q'];
      return defaultToReturn;
      function defaultToReturn($q) {
        return function (entityName, bean, param) {
          return $q.when(param[name]);
        };
      }
    }

    defaultReturnFalse.$inject = ['$q'];
    function defaultReturnFalse($q) {
      return function() {
        return $q.when(false);
      };
    }
  }
}());
