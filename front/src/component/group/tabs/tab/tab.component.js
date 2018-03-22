module.exports = (function() {
  'use strict';

  TabController.$inject = ['contextService', 'customService', '$translate', '$scope', '$timeout'];

  var GroupController = require('../../group-component.js');
  TabController.prototype = Object.create(GroupController.prototype);
  TabController.prototype.constructor = TabController;

  return {
    template: require('./tab.template.html'),
    controller: TabController,
    bindings: {
      name: '@',
      titleKey: '@?',
      tooltipKey: '@?'
    },
    require: {
      tabsCtrl: '^^groupTabs'
    },
    transclude: true
  };

  function TabController(contextService, customService, $translate, $scope, $timeout) {
    GroupController.call(this, contextService, customService, $translate, 'tab');
    var $ctrl = this;
    $ctrl.$onInit = init;
    /**
     * To indicate if tab is selected or not.
     * Broadcast a event tab-selected or tab-unselected.
     * @param {boolean} selected
     */
    $ctrl.isSelected = isSelected;

    // Implementations

    function isSelected(selected) {
      $ctrl.selected = selected;
      $timeout(function() {
        $scope.$broadcast(selected ? 'tab-selected' : 'tab-unselected');
      });
    }

    function init() {
      $ctrl.entityName = $ctrl.tabsCtrl.entityName;
      $ctrl.ctx = $ctrl.contextService.getCurrent();
      $ctrl.id = $ctrl.ctx.getComponentId($ctrl.name);
      $ctrl.initialize('title', $ctrl.titleKey, $ctrl.createTranslate);
      $ctrl.initialize('tooltip', $ctrl.tooltipKey, $ctrl.createTranslate);
      $ctrl.initialize('visible', true, $ctrl.createAssign);
      $ctrl.tabsCtrl.addTab($ctrl);
    }

  }

}());
