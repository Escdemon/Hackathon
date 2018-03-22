module.exports = (function() {
  'use strict';

  VarInputController.$inject = ['$timeout', 'contextService', 'customService', 'entityModel'];

  var ComponentController = require('../var-component.component.js');
  VarInputController.prototype = Object.create(ComponentController.prototype);
  VarInputController.prototype.constructor = VarInputController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./var-input.template.html'),
    controller: VarInputController,
    styles: [require('./var-input.less')],
    bindings: {
      classInput: '@',
      classLabel: '@',
      classOffset: '@',
      label: '@?',
      name: '@',
      type: '@',
      ngModel: '=',
      isProtected: '<',
      mandatory: '<',
      placeholder: '@?',
      tooltip: '@?',
      maximumlength: '<',
      decimal: '@',
      entityName: '@',
      varName: '@',
      isAmount: '<'
    }
  };

  function VarInputController($timeout, contextService, customService, entityModel) {
    ComponentController.call(this, contextService, customService, 'var-input');
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.$postLink = postLink;
    $ctrl.getOption = getOption;

    function onInit() {
      $ctrl.init();
      $ctrl.initialize('placeholder', $ctrl.placeholder);

      // Numeric specifications
      if ($ctrl.type === 'number') {
        // define step attribute's value
        var step = 1;
        var decimalPatternHuman = '';
        if (parseInt($ctrl.decimal)) {
          if ($ctrl.decimal > 0) {
            var i = 0;
            while (i < $ctrl.decimal) {
              step = step / 10;
              i = i + 1;
              decimalPatternHuman = decimalPatternHuman + 'X';
            }
          }
          $ctrl.step = step;
        }

        // convert maximumlength to min and max
        var maxIntPart = 0;
        var maxIntPartPatternHuman = '';
        if (parseInt($ctrl.maximumlength)) {
          maxIntPart = $ctrl.maximumlength;
          if (parseInt($ctrl.decimal)) {
            maxIntPart = parseInt($ctrl.maximumlength) - parseInt($ctrl.decimal);
          }
          var j = 0;
          var max = 1;
          while (j < maxIntPart) {
            max = max * 10;
            j = j + 1;
            maxIntPartPatternHuman = maxIntPartPatternHuman + 'X';
          }
          max = max - step;
          var min = -1 * max;
          $ctrl.max = max;
          $ctrl.min = min;
          // delete maxlength validator for type number
          $ctrl.maxlength = undefined;
        }

        // define the correct pattern
        var nbPattern = '';
        if (maxIntPart > 0) {
          nbPattern = nbPattern + '(\-)?[0-9]+';
        } else {
          nbPattern = nbPattern + '(\-)?0';
        }
        if (step < 1) {
          nbPattern = nbPattern + '((\.|,)[0-9]{1,' + $ctrl.decimal + '})?';
        }
        $ctrl.nbPattern = nbPattern;

        // build the pattern for humans
        var nbPatternHuman = '';
        if (maxIntPartPatternHuman.length > 0) {
          nbPatternHuman = nbPatternHuman + maxIntPartPatternHuman;
        } else {
          nbPatternHuman = nbPatternHuman + '0';
        }
        if (decimalPatternHuman.length > 0) {
          nbPatternHuman = nbPatternHuman + '.' + decimalPatternHuman;
        }
        $ctrl.nbPatternHuman = nbPatternHuman;

        if ($ctrl.isAmount) {
          $ctrl.amountOptions = getAmountOptions();
        }
      } else if ($ctrl.type === 'checkbox') {
        // for checkbox type
        initOptions();
      } else {
        // for all type but number and checkbox
        $ctrl.maxlength = $ctrl.maximumlength;
      }
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

    
    /**
     * Initializes the available options.
     */
    function initOptions() {
      // get allowed values' labels translation
      var entity = entityModel.entity($ctrl.entityName);
      var translatedAllowedValues = entity.translatedAllowedValues($ctrl.varName);
      $ctrl.opts = translatedAllowedValues;
    }

    /**
     * Retrieves the label corresponding to the given value
     * 
     * @param {object} value Value.
     * @returns {String} label if found ; {object} value if not
     */
    function getOption(value) {
      if ($ctrl.opts && $ctrl.opts.length > 0) {
        var selectedOpts = $ctrl.opts.filter(function(opt) {
          return opt.value === value;
        });
        return selectedOpts.length === 1 ? selectedOpts[0].label : value;
      } else {
        return value;
      }
    }

    function getAmountOptions() {
      var customAmountOptions = customService.get('amount-option');
      return customAmountOptions($ctrl.entityName, 'var-input', $ctrl.ngModel, {name: $ctrl.varName})
    }
  }
}());
