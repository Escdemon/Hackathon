module.exports = (function() {
  'use strict';

  VarDateController.$inject = ['$timeout', 'contextService', 'customService'];

  var ComponentController = require('../var-component.component.js');
  VarDateController.prototype = Object.create(ComponentController.prototype);
  VarDateController.prototype.constructor = VarDateController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./var-date.template.html'),
    controller: VarDateController,
    styles: [require('./var-date.less')],
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

  function VarDateController($timeout, contextService, customService) {
    ComponentController.call(this, contextService, customService, 'var-date');
    var $ctrl = this;

    this.$onInit = function() {
      $ctrl.init();
    };

    /**
     * Saves this component's state (dirty, errors and touched).
     */
    this.$onDestroy = function() {
      $ctrl.saveState();
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
    // UI BOOTSTRAP COMPONENT

    // component option
    $ctrl.dateOptions = {
	    maxDate: new Date(2050, 12, 31),
	    minDate: new Date(1970, 1, 1),
	    startingDay: 1
	  };    

    // popup
    $ctrl.popup = {
    	opened: false
    };
    $ctrl.open = function() {
    	$ctrl.popup.opened = true;
    };

  }
}());
