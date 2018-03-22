module.exports = (function() {
  'use strict';

  DefaultController.$inject = ['contextService', 'customService', '$translate'];

  var GroupController = require('../group-component.js');
  DefaultController.prototype = Object.create(GroupController.prototype);
  DefaultController.prototype.constructor = DefaultController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./default.template.html'),
    styles: [require('./default.less')],
    controller: DefaultController,
    bindings: {
      name: '@',
      entityName: '@',
      isProtected: '=',// to update the variable into the parent controller, so children will be updated.
      visible: '<',
      titleKey: '@?',
      tooltipKey: '@?'
    },
    transclude: true
  };

  function DefaultController(contextService, customService, $translate) {
    GroupController.call(this, contextService, customService, $translate, 'group-default');
    this.$onInit = GroupController.prototype.onInit;
    this.$onDestroy = onDestroy;
    var $ctrl = this;

    function onDestroy() {
      $ctrl.saveState();
    }
  }
}());
