module.exports = (function() {
  'use strict';

  LineController.$inject = ['contextService', 'customService', '$translate'];

  var GroupController = require('../group-component.js');
  LineController.prototype = Object.create(GroupController.prototype);
  LineController.prototype.constructor = LineController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./line.template.html'),
    styles: [require('./line.less')],
    controller: LineController,
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

  function LineController(contextService, customService, $translate) {
    GroupController.call(this, contextService, customService, $translate, 'group-line');
    this.$onInit = GroupController.prototype.onInit;
    this.$onDestroy = onDestroy;
    var $ctrl = this;

    function onDestroy() {
      $ctrl.saveState();
    }
  }
}());
