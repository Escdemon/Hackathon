module.exports = (function () {
  'use strict';

  configuration.$inject = ['customServiceProvider'];

  return configuration;

  function configuration(customServiceProvider) {
    customServiceProvider.setNewFunction('visible', 'template', defaultVisible);
    customServiceProvider.setNewFunction('css', 'template', defaultCss);
    customServiceProvider.setNewFunction('mandatory', 'template', defaultMandatory);
    customServiceProvider.setNewFunction('isProtected', 'template', defaultReturnFalse);

    defaultVisible.$inject = ['$q', 'Action'];
    function defaultVisible($q, Action) {
      /**
       * Return a promise with boolean to show (with ng-if) the template.
       * By default is false only for template simple-list-link in create action.
       */
      return function (entityName, bean, params) {
        if (params.template && params.template.type === 'simple-list-link') {
          return $q.when(!new Action(params.action).isCreate());
        }
        return $q.when(true);
      };
    }

    defaultCss.$inject = ['$q'];
    function defaultCss($q) {
      /**
       * Return a promise with class css to display on template.
       * By default is undefined to not add class.
       */
      return function () {
        return $q.when(undefined);
      };
    }

    defaultMandatory.$inject = ['$q'];
    function defaultMandatory($q) {
      /**
       * Return a promise with the parameter mandatory for a template.
       * By default it is set as defined on the template definition.
       */
      return function (entityName, bean, params) {
        /* the mandatory parameter is useful only for variable template */
        if (params.template.type !== 'template') {
          return $q.when(params.template.mandatory);
        } else {
          return $q.when(false);
        }
      };
    }

    defaultReturnFalse.$inject = ['$q'];
    function defaultReturnFalse($q) {
      return function() {
        return $q.when(false);
      };
    }

  }
}());
