module.exports = (function() {
  'use strict';

  VarDatetimeController.$inject = ['$filter', '$scope', '$timeout', '$translate',
    'uibDateParser', 'contextService', 'customService'];

  var ComponentController = require('../var-component.component.js');
  VarDatetimeController.prototype = Object.create(ComponentController.prototype);
  VarDatetimeController.prototype.constructor = VarDatetimeController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./var-datetime.template.html'),
    controller: VarDatetimeController,
    styles: [require('./var-datetime.less')],
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

  function VarDatetimeController($filter, $scope, $timeout, $translate,
    uibDateParser, contextService, customService) {

    ComponentController.call(this, contextService, customService, 'var-datetime');
    var $ctrl = this;
    var ngModelFormat;

    this.$onInit = function() {
      $ctrl.init();
      // watch ngModel to format datetime on load
      ngModelFormat = $scope.$watch('$ctrl.ngModel', function(oldValue, newValue) {
        if (oldValue !== newValue) {
          $ctrl.updateInput();
        }
      });
      $ctrl.updateInput();
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
     * Updates the value of $ctrl.datetime (value displayed in the input) from $ctrl.ngModel
     */
    this.updateInput = function() {
      if ($ctrl.ngModel) {
        $translate('common.timestamp-format').then(function(translation) {
          $ctrl.datetime = $filter('date')($ctrl.ngModel, translation.toString());
        });
      }
    };

    /*
     * Updates the value of $ctrl.ngModel from $ctrl.datetime (value set in the input)
     */
    this.updateDatetimepicker = function() {
      if ($ctrl.datetime) {
        $translate('common.timestamp-format').then(function(translation) {
          var date = uibDateParser.parse($ctrl.datetime, translation.toString());
          if (date) {
            $ctrl.ngModel = date;
            // remove error flag
            $ctrl.varForm.form[$ctrl.id].$setValidity('datetime', true);
          } else {
            $ctrl.ngModel = undefined;
            // raise error flag
            $ctrl.varForm.form[$ctrl.id].$setValidity('datetime', false);
          }
        });
      }
    };

    /*
     * Closes the picker
     */
    this.closePicker = function() {
      var component = document.getElementById($ctrl.id).parentElement.parentElement;
      if (component.className.indexOf('open') !== -1) {
        component.classList.remove('open');
      }
    };
  }
}());
