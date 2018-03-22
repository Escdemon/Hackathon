module.exports = (function () {
  'use strict';

  VarStringLongController.$inject = ['$timeout', 'contextService', 'customService'];

  var ComponentController = require('../var-component.component.js');
  VarStringLongController.prototype = Object.create(ComponentController.prototype);
  VarStringLongController.prototype.constructor = VarStringLongController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./string-long.template.html'),
    controller: VarStringLongController,
    bindings: {
      classInput: '@',
      classLabel: '@',
      classOffset: '@',
      label: '@',
      name: '@',
      ngModel: '=',
      isProtected: '<',
      mandatory: '<',
      placeholder: '@',
      tooltip: '@',
      maximumlength: '<',
      entityName: '@',
      varName: '@'
    }
  };

  function VarStringLongController($timeout, contextService, customService) {
    ComponentController.call(this, contextService, customService, 'string-long');
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.$postLink = postLink;

    function onInit() {
      $ctrl.init();
      $ctrl.initialize('placeholder', $ctrl.placeholder);
      $ctrl.maxlength = $ctrl.maximumlength;
    }

    /**
     * Saves this component's state (dirty, errors and touched).
     */
    function onDestroy() {
      $ctrl.saveState();
    }

    /**
     * Restores this component's state (dirty, errors and touched).
     */
    function postLink() {
      $timeout(function () {
        var m = $ctrl.formCtrl.form[$ctrl.id];
        if (m === undefined) {
          return;
        }
        var state = $ctrl.ctx.getComponentState($ctrl.id);

        if (state.$dirty) {
          m.$setDirty();
        }
        if (state.$touched) {
          m.$setTouched();
        }
        if (state.$error) {
          Object.keys(state.$error).forEach(function (errorKey) {
            m.$setValidity(errorKey, false);
          });
        }
      }, 100);
    }
  }

}());
