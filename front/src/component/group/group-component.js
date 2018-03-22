module.exports = (function() {
  'use strict';

  var GroupController = function(contextService, customService, $translate, componentType) {
    this.contextService = contextService;
    this.customService = customService;
    this.$translate = $translate;
    this.componentType = componentType;
  };

  /**
   * Initializes this component.
   *
   * This method calls the custom methods to initialize the following properties :
   * - title
   * - tooltip
   * - isProtected
   * - visible
   */
  GroupController.prototype.onInit = function() {
    this.ctx = this.contextService.getCurrent();
    if (!this.ctx) {
      return;
    }
    this.id = this.ctx.getComponentId(this.name);
    this.formCtrl = this.varForm || this.searchForm;
    this.waitDataToExecute(this.doInit);
  };

  /**
   * Launches the function `execute` when the form's promise is resolved
   * or immediately if there is no form nor promise.
   * @param {Function} execute Function to execute.
   */
  GroupController.prototype.waitDataToExecute = function(execute) {
    /*
     * The initialization is deferred if this component is inside a form
     * which holds a promise (we are waiting for data to work with).
     */
    if (this.formCtrl && this.formCtrl.promiseData && this.formCtrl.promiseData.then) {
      var that = this;
      this.formCtrl.promiseData.then(function() {
        execute.call(that);
      });
    } else {
      execute.call(this);
    }
  };

  GroupController.prototype.doInit = function() {
    this.initialize('title', this.titleKey, this.createTranslate);
    this.initialize('tooltip', this.tooltipKey, this.createTranslate);

    if (!this.isProtected) {// If parent is protected, this template is protected.
      this.initialize('isProtected', false, this.createAssign);
    }
    this.initialize('visible', true, this.createAssign);
  };

  GroupController.prototype.saveState = function(state) {
    state = state || {};
    state.title = this.title;
    state.tooltip = this.tooltip;
    state.isProtected = this.isProtected;
    state.visible = this.visible;
    if (this.ctx) {
      this.ctx.saveComponentState(this.id, state);
    }
  };

  /**
   * Executes the custom method corresponding to the `varName` to initialize this component's property `varName`.
   *
   * @param {string} varName Name of the property to initialize.
   * @param {*} defaultValue Value if the custom method returns `undefined`.
   * @param {function} callbackBuilder Builder function called to create a new callback which is executed while the custom method's promise is resolved.
   */
  GroupController.prototype.initialize = function(varName, defaultValue, callbackBuilder) {
    var state = this.ctx.getComponentState(this.id);

    if (state[varName] !== undefined) {
      this[varName] = state[varName];

    } else {
      var callback = callbackBuilder(this, varName, defaultValue);
      var data = this.ctx.getData();
      var params = {templateName: this.name};
      this.customService.get(varName)(this.entityName, this.componentType, data, params).then(callback);
    }
  };

  /**
   * Creates a new function which makes an assignment.
   *
   * @param {object} obj Object to update (it is the controller).
   * @param {string} varName Name of the property to update.
   * @param {any} defaultValue Value if the custom method returns `undefined`.
   * @returns {function} A function with the following signature : (any) => void.
   */
  GroupController.prototype.createAssign = function(obj, varName, defaultValue) {
    return function(value) {
      obj[varName] = (value !== undefined) ? value : defaultValue;
    };
  };

  /**
   * Creates a new function which calls the service `$translate` to retrieve a label from a key.
   *
   * @param {object} obj Object to update (it is the controller).
   * @param {string} varName Name of the property to update.
   * @param {any} defaultValue Value if the custom method returns `undefined`.
   * @returns {function} A function with the following signature : (any) => void.
   */
  GroupController.prototype.createTranslate = function(obj, varName, defaultValue) {
    return function(value) {
      var key = (value !== undefined) ? value : defaultValue;
      // If the key is not found, the value is used as is.
      var translation = obj.$translate(key, undefined, undefined, value);
      translation.then(obj.createAssign(obj, varName, defaultValue));
    };
  };

  return GroupController;

}());
