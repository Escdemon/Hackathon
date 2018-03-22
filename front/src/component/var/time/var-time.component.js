module.exports = (function() {
  'use strict';

  VarTimeController.$inject = ['$translate', '$scope', '$timeout', 'contextService', 'customService'];

  var ComponentController = require('../var-component.component.js');
  VarTimeController.prototype = Object.create(ComponentController.prototype);
  VarTimeController.prototype.constructor = VarTimeController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./var-time.template.html'),
    controller: VarTimeController,
    styles: [require('./var-time.less')],
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
      entityName: '@',
      varName: '@'
    }
  };

  function VarTimeController($translate, $scope, $timeout, contextService, customService) {
    ComponentController.call(this, contextService, customService, 'var-time');
    var $ctrl = this;
    var ngModelFormat;

    this.$onInit = function() {
      $ctrl.init();
    };

    /**
     * Saves this component's state (dirty, errors and touched).
     */
    this.$onDestroy = function() {
      $ctrl.saveState();
      if (ngModelFormat) {
        ngModelFormat();
      }
    };

    /**
     * Restores this component's state (dirty, errors and touched).
     */
    this.$postLink = function() {
      $timeout(function() {
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
          Object.keys(state.$error).forEach(function(errorKey) {
            m.$setValidity(errorKey, false);
          });
        }
      }, 100);
    };

    /*
     * Updates the value of $ctrl.time to current timestamp
     */
    this.updateNow = function() {
      if ($ctrl.ngModel !== undefined) {
        $ctrl.ngModel = new Date();
      }
    };
  }
}());
