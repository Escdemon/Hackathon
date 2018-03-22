module.exports = (function () {
  'use strict';

  Controller.$inject = [
    '$scope', 'contextService', 'customService'
  ];

  return {
    require: {
      innerTemplateCtrl: '?^^innerTemplate',
      formCtrl: '?^^varForm',
      searchCtrl: '?^^searchForm'
    },
    controller: Controller,
    template: require('./balise.template.html'),
    bindings: {
      isProtected: '<',
      name: '@'
    }
  };

  function Controller($scope, contextService, customService) {
    var $ctrl = this;
    var onDestroyer;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;

    $ctrl.showButton = false;

    // Implementation

    function onInit() {
      var ctx = contextService.getCurrent();
      if (!ctx) {
        return;
      }
      if ($ctrl.name) {
        $ctrl.id = ctx.getComponentId($ctrl.name);
      }
      var parentComponent = $ctrl.innerTemplateCtrl || $ctrl.searchCtrl ||  $ctrl.formCtrl;
      var promiseData = parentComponent.promiseData;
      promiseData.then(saveIntoData);
      onDestroyer = $scope.$on('data-changed-' + parentComponent.id, function (event, data) {
        parentComponent.promiseData.then(saveIntoData);
      });

      $ctrl.action = ctx.action;
      $ctrl.entity = $ctrl.action.entity;
      $ctrl.actions = {};
      $ctrl.show = {};
      $ctrl.addingClass = {};
      $ctrl.mandatory = {};
      $ctrl.protect = {};
      $ctrl['key-elements'] = {};

    }
    function onDestroy() {
      if (onDestroyer) {
        onDestroyer();
      }
    }

    function saveIntoData(data) {
      $ctrl.data = data;
    }
  }
}());
