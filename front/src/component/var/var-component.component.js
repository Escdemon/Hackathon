module.exports = (function() {
  'use strict';

  /*
   * Generic component which holds common behaviors (state management, custom methods).
   */
  var VarComponentController = function(contextService, customService, componentType) {
    this.contextService = contextService;
    this.customService = customService;
    this.componentType = componentType;
  };

  /**
   * Initializes this component.
   *
   * This method calls the custom methods to initialize the following properties :
   * - label
   * - tooltip
   * - isMandatory
   * - isProtected
   * - isReadOnly
   * - visible
   */
  VarComponentController.prototype.init = function() {
    this.formCtrl = this.varForm || this.searchForm;
    this.ctx = this.contextService.getCurrent();
    this.id = this.ctx.getComponentId(this.name);
    this.showLabel = this.classLabel !== undefined;
    this.isReadOnly = this.ctx.action.readOnly;
    var that = this;

    if (this.formCtrl.promiseData && this.formCtrl.promiseData.then) {
      this.formCtrl.promiseData.then(customInit(that));
    } else {
      customInit(that)();
    }
  };

  function customInit(that) {
    return function () {
      that.initialize('label', that.label);
      that.initialize('tooltip', that.tooltip);

      if (that.searchForm) {// never mandatory if it is in a searchTemplate
        that.isMandatory = false;
      } else if (that.mandatory) {
        that.isMandatory = that.mandatory;
      } else {
        that.initialize('mandatory', false, 'isMandatory');
      }

      if (!that.isProtected) {// If parent is protected, this template is protected.
        if (that.isReadOnly) {
          that.isProtected = true;
        } else {
          that.initialize('isProtected', false);
        }
      } 
      if (!that.isReadOnly) {
        that.initialize('isReadOnly', false);
      }
      that.initialize('visible', true);
    };
  }

  VarComponentController.prototype.saveState = function(state) {
    state = state || {};

    if (!this.isProtected) {
      var m = this.formCtrl.form[this.id];

      if (m !== undefined) {
        state.$dirty = m.$dirty;
        state.$error = m.$error;
        state.$touched = m.$touched;
      }
    }
    state.label = this.label;
    state.tooltip = this.tooltip;
    state.isMandatory = this.isMandatory;
    state.isProtected = this.isProtected;
    state.isReadOnly = this.isReadOnly;
    state.visible = this.visible;
    this.ctx.saveComponentState(this.id, state);
  };

  /**
   * Executes the custom method corresponding to the `methodName` to initialize this component's property `propertyName`.
   *
   * @param {string} methodName Name of the custom method to call.
   * @param {any} defaultValue Value if the custom method returns `undefined` or a Promise which resolves to `undefined`.
   * @param {string} propertyName Name of the property to initialize. It may be `undefined`, in this case the `methodName` is used.
   */
  VarComponentController.prototype.initialize = function(methodName, defaultValue, propertyName) {
    var state = this.ctx.getComponentState(this.id);
    var varName = propertyName || methodName;

    if (state[varName] !== undefined) {
      this[varName] = state[varName];

    } else {
      var $ctrl = this;
      var data = this.ctx.getData();
      var params = this.getParams();
      var value = this.customService.get(methodName)($ctrl.entityName, this.componentType, data, params);

      if (typeof value.then === 'function') {
        value.then(function(result) {
          $ctrl[varName] = result !== undefined ? result : defaultValue;
        });
      } else {
        $ctrl[varName] = value !== undefined ? value : defaultValue;
      }
    }
  };

  VarComponentController.prototype.getParams = function() {
    return {
      id: this.id,
      name: this.name,
      entityName: this.entityName,
      varName: this.varName,
      labelKey: this.label,
      tooltipKey: this.tooltip,
      placeholderKey: this.placeholder
    };
  };

  return VarComponentController;

}());
