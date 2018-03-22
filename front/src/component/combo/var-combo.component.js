module.exports = (function() {
  'use strict';

  VarComboController.$inject = ['$translate', '$timeout', 'customService', 'entityModel', 'contextService'];

  var ComponentController = require('../var/var-component.component.js');
  VarComboController.prototype = Object.create(ComponentController.prototype);
  VarComboController.prototype.constructor = VarComboController;
  var comboType = 'var-combo';

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./combo.template.html'),
    controller: VarComboController,
    bindings: {
      ngModel: '=',
      classInput: '@',
      classLabel: '@',
      classOffset: '@',
      name: '@',
      placeholder: '@',
      entityName: '@',
      varName: '@',
      label: '@',
      tooltip: '@',
      mandatory: '<',
      isProtected: '<'
    }
  };

  function VarComboController($translate, $timeout, customService, entityModel, contextService) {
    ComponentController.call(this, contextService, customService, comboType);
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.$postLink = postLink;
    $ctrl.getOption = getOption;

    /**
     * Initializes this component.
     */
    function onInit() {
      $ctrl.formCtrl = $ctrl.varForm || $ctrl.searchForm;
      $ctrl.init();

      if ($ctrl.formCtrl.promiseData && $ctrl.formCtrl.promiseData.then) {
        $ctrl.formCtrl.promiseData.then(initOptions);
      } else {
        initOptions();
      }
    }

    /**
     * Initializes the available options.
     */
    function initOptions() {
      var options = [];

      // add a indifferent value in case of searchForm
      if ($ctrl.searchForm) {
        var optionInd = {
          value: undefined,
          selected: 'selected'
        };
        if ($ctrl.placeholder) {
          optionInd.label = '-- ' + $ctrl.placeholder + ' --';
        } else {
          optionInd.label = '';
        }
        options.push(optionInd);
      }

      var data = $ctrl.ctx.getData();
      var params = $ctrl.getParams();
      var getOptions = $ctrl.customService.get('options');
      var result = getOptions($ctrl.entityName, comboType, data, params);
      result.then(function(opts) {
        $ctrl.opts = options.concat(opts);
      });
    }

    /**
     * Saves this component's state into the current context.
     */
    function onDestroy() {
      $ctrl.saveState();
    }

    /**
     * Restores this component's state (dirty, errors and touched).
     */
    function postLink() {
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
    }

    /**
     * Retrieves an option object {value, label} corresponding to the given value.
     * 
     * @param {object} value Value.
     */
    function getOption(value) {
      var selectedOpts = $ctrl.opts.filter(function(opt) {
        return opt.value === value;
      });
      return selectedOpts.length === 1 ? selectedOpts[0] : undefined;
    }
  }
}());
