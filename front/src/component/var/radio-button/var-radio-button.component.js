module.exports = (function () {
  'use strict';

  VarRadioButtonController.$inject = ['entityModel', 'contextService', 'customService'];

  var ComponentController = require('../var-component.component.js');
  VarRadioButtonController.prototype = Object.create(ComponentController.prototype);
  VarRadioButtonController.prototype.constructor = VarRadioButtonController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./var-radio-button.template.html'),
    controller: VarRadioButtonController,
    styles: [require('./var-radio-button.less')],
    bindings: {
      classInput: '@',
      classLabel: '@',
      classOffset: '@',
      label: '@',
      name: '@',
      type: '@',
      ngModel: '=',
      isProtected: '<',
      mandatory: '<',
      placeholder: '@',
      tooltip: '@',
      entityName: '@',
      varName: '@'
    }
  };

  function VarRadioButtonController(entityModel, contextService, customService) {
    ComponentController.call(this, contextService, customService, 'var-radio-button');
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.choiceLabelToTranslate = choiceLabelToTranslate;
    $ctrl.choiceLabelToTranslateFromValue = choiceLabelToTranslateFromValue;

    // Implementations
    function onInit() {
      $ctrl.init();

      // array of options
      var entity = entityModel.entity($ctrl.entityName);
      var allowedValues = entity.allowedValues($ctrl.varName);
      // copy the array in order to not update allowedValues
      var options = [];
      allowedValues.forEach(function (allowedValue) {
        var option = {
          code: allowedValue.code,
          value: allowedValue.value
        };
        options.push(option);
      });
      // add a indifferent value in case of searchForm
      if ($ctrl.searchForm) {
        var optionInd = {
          code: 'common.search-indifferent',
          value: undefined
        };
        options.push(optionInd);
      }
      $ctrl.options = options;
    }

    /**
     * Saves this component's state (dirty, errors and touched).
     */
    function onDestroy() {
      $ctrl.saveState();
    }

    /**
     * Return the correct label to be translated by $translate from allowedValue's code
     */
    function choiceLabelToTranslate(code) {
      // special case for searchTemplate "indifferent" choice
      if (code === 'common.search-indifferent') {
        return code;
      }
      return $ctrl.entityName + '.' + $ctrl.varName + '-' + code;
    }

    /**
     * Return the correct label to be translated by $translate from allowedValue's value
     */
    function choiceLabelToTranslateFromValue(value) {
      for (var i = 0, len = $ctrl.options.length; i < len; i++) {
        if ($ctrl.options[i].value === value) {
          return choiceLabelToTranslate($ctrl.options[i].code);
        }
      }
      return value;
    }

  }
}());
