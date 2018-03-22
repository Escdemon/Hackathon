module.exports = (function() {
  'use strict';

  GroupFieldsetController.$inject = ['contextService', 'customService', '$translate'];

  var GroupController = require('../group-component.js');
  GroupFieldsetController.prototype = Object.create(GroupController.prototype);
  GroupFieldsetController.prototype.constructor = GroupFieldsetController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./fieldset.template.html'),
    styles: [require('./fieldset.less')],
    controller: GroupFieldsetController,
    bindings: {
      name: '@',
      entityName: '@',
      isProtected: '=',// to update the variable into the parent controller, so children will be updated.
      visible: '<',
      collapsable: '<',
      collapsed: '<',
      titleKey: '@?',
      tooltipKey: '@?'
    },
    transclude: true
  };

  function GroupFieldsetController(contextService, customService, $translate) {
    GroupController.call(this, contextService, customService, $translate, 'group-fieldset');
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.toggle = toggle;

    /**
     * initializes this component.
     */
    function onInit() {
      GroupController.prototype.onInit.call($ctrl); // title, tooltip, isProtected, visible

      if ($ctrl.collapsable) {
        $ctrl.waitDataToExecute(function() {
          $ctrl.initialize('collapsed', true, $ctrl.createAssign);
        });
      }
    }

    function onDestroy() {
      $ctrl.saveState({
        collapsed: $ctrl.collapsed
      });
    }

    /**
     * Expand or collapse this fieldset.
     */
    function toggle() {
      $ctrl.collapsed = !$ctrl.collapsed;
    }

  }
}());
