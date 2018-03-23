module.exports = (function (ng) {
  'use strict';

  factory.$inject = [
    'contextService',
    'customService',
    'restService',
    '$q',
    'messageService',
    '$location',
    'loginService'
  ];
  return factory;

  function factory(
    contextService,
    customService,
    restService,
    $q,
    messageService,
    $location,
    loginService
  ) {
    return Action;

    /**
     * Create new Action.
     * @param {Object} action action
     * @param {Object} action.name name of action.
     * @param {String} action.name.front front display of action.
     * @param {String} action.name.back back display of action.
     * @param {String} action.label key translate for display label.
     * @param {String} action.title key translate for display title.
     * @param {String} action.icon css class to display icon.
     * @param {String} action.input type of input.
     * @param {String} action.process type of process.
     * @param {String} action.persistence type of persistence.
     * @param {Object} action.subActions list of SubActions.
     * @param {Boolean} action.read-only if action is read only.
     * @param {String} action.io-flux type of io-flux of action.
     * @param {Entity} entity entity.
     * @constructor
     */
    function Action(action, entity) {
      var that = this;
      /**
       * @type {Entity} entity of action.
       */
      this.entity = entity;
      /**
       * @type {String} key translate for display label.
       */
      this.label = action.label;

      /**
       * @type {String} key translate for display title.
       */
      this.title = action.title;

      /**
       * @type {{front: String, back: String}} name of action.
       */
      this.name = action.name;

      /**
       * @type {{code:{front: String, back: String}, label}} list of SubActions.
       */
      this.subActions = action['sub-actions'];

      /**
       * @type {String} css class to display icon.
       */
      this.icon = action.icon;

      /**
       * @type {String} process.
       */
      this.process = action.process;

      /**
       * @type {String} persistence.
       */
      this.persistence = action.persistence;

      /**
       * @type {String} input.
       */
      this.input = action.input;

      /**
       * @type {String} Next action's identifier.
       */
      this.nextAction = action['next-action'];

      /**
       * @type {String} flux.
       */
      this.flux = action['io-flux'];

      /**
       * @type {Boolean} if action is read only.
       */
      this.readOnly = action['read-only'];

      this.isDisplayable = isDisplayable;
      this.execute = execute;
      this.cancel = cancel;
      this.redirectToPageAction = redirectToPageAction;

      /**
       * To know if the action is in edit mode (ie not in read-only).
       * @returns {Boolean} true if edit mode is active, false else.
       * @function
       */
      this.isEdit = isEdit;

      /**
       * To know if the action is create action.
       * @returns {Boolean} true if action is create, false else.
       * @function
       */
      this.isCreate = isPersistence('insert');

      /**
       * To know if the action is delete action.
       * @returns {Boolean} true if action is delete, false else.
       * @function
       */
      this.isDelete = isPersistence('delete');

      /**
       * To know if the action is update action.
       * @returns {Boolean} true if action is update, false else.
       * @function
       */
      this.isUpdate = isPersistence('update');

      /**
       * To know if the action performed without persistence.
       * @returns {Boolean} true if action is perform without persistence, false else.
       * @function
       */
      this.hasNoPersistence = isPersistence('none');

      /**
       * To know if the action need to have one or multiple input.
       * @returns {Boolean} true if action is input-one or input-multiple, false else.
       * @function
       */
      this.hasInput = hasInput;

      /**
       * To know if the action need to have one input.
       * @returns {Boolean} true if action is object-one, false else.
       * @function
       */
      this.hasSingleInput = hasInputEquals('object-one');

      /**
       * To know if the action need to have no input.
       * @returns {Boolean} true if action is none, false else.
       * @function
       */
      this.hasNoInput = hasInputEquals('none');

      /**
       * To know if the action need to have no input.
       * @returns {Boolean} true if action is none, false else.
       * @function
       */
      this.hasMultipleInput = hasInputEquals('object-multiple');

      /**
       * To know if the action need to have query input.
       * @returns {Boolean} true if action is query, false else.
       * @function
       */
      this.hasQueryInput = hasInputEquals('query');

      /**
       * To know if the action have link process.
       * @returns {Boolean} true if action have link process, false else.
       * @function
       */
      this.hasLinkProcess = hasProcessEquals('link');

      /**
       * Indicates whether this action has a custom process.
       * @returns {Boolean} true if action has a custom process; false otherwise.
       * @function
       */
      this.hasCustomProcess = hasProcessEquals('custom');

      /**
       * To know if action is detach.
       * @returns {Boolean} true if action is link and delete.
       */
      this.isDetach = isDetach;

      /**
       * To know if action is attach.
       * @returns {Boolean} true if action is link and create.
       */
      this.isAttach = isAttach;

      /**
       * To know if action need form for user interface.
       * @returns {Boolean} true if action need form for user interface.
       */
      this.isUIInput = isUI('input');
      /**
       * To know if action don't need form (display only) for user interface.
       * @returns {Boolean} true if action don't need form (display only) for user interface.
       */
      this.isUIDisplay = isUI('display');
      /**
       * To know if action has no user interface.
       * @returns {Boolean} true if action has no user interface.
       */
      this.isUINone = isUI('none');
      /**
       * Sub-Action currently executed
       */
      this.selectedSubAction = undefined;

      // Implementations

      /**
       * To know if action is displayable
       * @param {Action} currentAction current page action.
       * @param {Boolean} protect false if variable or template is editable, true else.
       * @param {Boolean} beanSelected true if a bean is selected.
       * @param {Boolean} mandatory true if component into perform action is mandatory.
       * @param {Boolean} withLink true if component need to display link action.
       * @returns {Boolean} true if action is displayable, false else.
       * @function
       */
      function isDisplayable(currentAction, protect, beanSelected, mandatory, withLink) {
        if (!loginService.canUseFunction(that)) {
          return false;
        }
        if (that.hasQueryInput()) {
          return false;
        }
        if (currentAction.hasLinkProcess() && that.isDetach()) {
          return false;
        }
        if (currentAction.hasQueryInput()) {
          return (withLink && that.hasLinkProcess()) || (!withLink && !that.hasLinkProcess());
        }
        if (currentAction.readOnly && (!that.readOnly || !that.hasNoPersistence())) {
          return false;
        }
        if (protect && (!that.readOnly || !that.hasNoPersistence())) {
          return false;
        }
        if (withLink && that.hasLinkProcess()) {
          if (that.isCreate()) {
            return true;
          }
          if (that.isDelete() && mandatory) {
            return false;
          }
        }
        return beanSelected || !that.hasInput();
      }

      /**
       * To redirect into action page.
       * For flux none action and detach call execute function.
       *
       * @param {String[]} pks array of id to perform action (can be empty).
       * @param {Object} [functions] function call on next action.
       * @param {Function} [functions.execute] call after execution of action.
       * @param {Function} [functions.afterExecute] call after execution of action.
       * @param {Function} [functions.afterBackLoad] call after back load process.
       * @param {Object} [options] options to execute action
       * @param {Link} [options.link] link form action to execute.
       * @param {String} [options.query] query to add to execute action.
       * @param {Boolean} [fromMenu] Indicates whether this function is called from the menu or not.
       * @param {Boolean} [nextAction] Indicates whether this function is called by launching a next action.
       */
      function redirectToPageAction(pks, functions, options, fromMenu, nextAction) {
        var params = {
          action: that,
          options: options,
          actionName: that.name.front
        };

        getMenuActionKeys(fromMenu, pks, params).then(function (customPks) {
          params.pks = customPks;
          var overrideAction = customService.get('override-action')(that.entity.name.front, 'action', {}, params);
          if (overrideAction){
            overrideAction.then(function (data) {
              if (data && data.action && data.action !== that) {
                var keys = data.pks || customPks;
                var opts = data.options || options;
                data.action.redirectToPageAction(keys, functions, opts, fromMenu, nextAction);
              } else {
                doRedirect(customPks, functions, options, fromMenu, nextAction);
              }
            });
          } else {
            console.error('The custom function override-action for action can\'t return undefined.');
          }
        });
      }

      /**
       * Performs the redirection.
       */
      function doRedirect(pks, functions, options, fromMenu, nextAction) {
        var ctx = fromMenu ? contextService.initCurrent(that, pks, fromMenu) : contextService.getCurrent();

        var currentAction = ctx.action;
        if (that.isUINone() || that.isDetach() || (that.isAttach() && currentAction.isAttach())) {
          execute(pks, ctx.action, functions, options);
          return;
        }

        if (fromMenu) {
          contextService.goTo(ctx);
          return;
        }

        var nextContext;
        if (nextAction) {
          nextContext = contextService.replaceCurrent(ctx.action, pks);
        } else {
          var opts = that.hasLinkProcess() ? options : undefined;
          nextContext = contextService.createNext(that, that.hasInput() ? pks : [], opts);
          contextService.setNext(nextContext);
        }
        nextContext.setFunctions(functions);
        contextService.goTo(nextContext);
      }

      /**
       * Retrieves the keys of the entities to work with if the current action is an action with input
       * and no key is provided.
       * @param {Boolean} [fromMenu] Indicates whether this function is called from the menu or not.
       * @param {String[]} [pks] Keys of the entities (can be empty).
       * @param {Object} [params] Parameters for the custom method.
       * @param {Action} [params.action] Action to perform.
       * @param {Object} [params.options] Action's options.
       */
      function getMenuActionKeys(fromMenu, pks, params) {
        if (fromMenu && that.hasInput() && (!pks || !pks.length)) {
          var menuAction = customService.get('menu-action')(that.entity.name.front, 'action', {}, params);
          if (menuAction){
            return menuAction.then(function (keys) {
              if (!keys || !keys.length) {
                var errorMessage = {
                  display: 'core.menu-action-no-element-error',
                  parameters: {
                    entity: that.entity.name.front
                  },
                  level: 'danger'
                };
                messageService.display(errorMessage);
                return undefined;
              }
              return keys;
            });
          } else {
        	  console.error('The custom function menu-action for action can\'t return undefined.');
          }
        }
        return $q.when(pks);
      }

      /**
       * To cancel current action and go to previous action page.
       * For flow can skip current pk, or all.
       *
       * @param {Boolean} skipOne skip one element in flow.
       */
      function cancel(skipOne) {
        var cancelAction = customService.get('cancel-action')
        (that.entity.name.front, 'action', contextService.getCurrent().getData(), {action: that});
        if (cancelAction){
          cancelAction.then(function (cancellation) {
            if (cancellation) {
              if (skipOne) {
                contextService.goToNextInFlow(true);
              } else {
                contextService.goToPrevious();
              }
            }
          });
        } else {
          console.error('The custom function cancel-action for action can\'t return undefined.');
        }
      }

      /**
       * To perform (execute action in back and go to previous page) or prepare action (go to page to execute action).
       * @param {String[]} ids array of id to perform action (can be empty).
       * @param {Action} [currentAction] current page action.
       * @param {Object} [functions] function call on next action.
       * @param {Function} [functions.execute] call after execution of action.
       * @param {Function} [functions.afterExecute] call after execution of action.
       * @param {Function} [functions.afterBackLoad] call after back load process.
       * @param {Object} [options] options to execute action
       * @param {Link} [options.link] link form action to execute.
       * @param {String} [options.query] query to add to execute action.
       */
      function execute(ids, currentAction, functions, options) {
        var currentContext = contextService.getCurrent();
        var params = {
          action: currentAction,
          pks: ids,
          options: options
        };
        var validateAction = customService.get('validate-action')
        (that.entity.name.front, 'action', currentContext ? currentContext.getData() : {}, params);
        if (validateAction){
          validateAction.then(function (validation) {
            if (validation) {
              doExecute(ids, currentAction, functions, options, currentContext);
            }
          });
        } else {
          console.error('The custom function validate-action for action can\'t return undefined.');
        }
      }

      /**
       * Perform the execution.
       */
      function doExecute(ids, currentAction, functions, options, currentContext) {
        var nextContext = $q.when(undefined);
        var fnToExecute;

        // Les actions de type read ne font pas appel au back
        if (that.hasNoPersistence() && !that.hasCustomProcess()) {
          fnToExecute = executeRead;
          // Il faut donc traiter les actions custom comme des update (soumission du resultat au back)
        } else if (that.isUpdate() || that.hasCustomProcess()) {
          fnToExecute = executeUpdate;
        } else if (that.isCreate()) {
          fnToExecute = executeCreate;
        } else if (that.isDelete()) {
          fnToExecute = executeDelete;
        }

        var overrideExecuteFn = currentContext.getFunction('execute');
        if (overrideExecuteFn) {
          var result = overrideExecuteFn(fnToExecute, that, currentContext, ids, currentAction, options);
          if (ng.isDefined(result) && (typeof result.then === 'function')) {
            nextContext = result;
          }
        } else {
          nextContext = fnToExecute(currentContext, ids, currentAction, options);
        }

        if (ng.isDefined(nextContext) && (typeof nextContext.then === 'function')) {
          nextContext.then(function (nextContext) {
            goToNextAction(currentAction, currentContext.pks, options, currentContext).then(function (stop) {
              if (stop) {
                return;
              }
              // Reload when action has no IHM to display after execute and for detach (not in select tab)
              var needReload = that.isUINone() || (that.isDetach() && !currentAction.isDetach());
              if (nextContext) {
                // Need to go to other context (to stack)
                contextService.setNext(nextContext);
                nextContext.setFunctions(functions);
              } else if (needReload) {
                // Reload current context.
                nextContext = currentContext;
              } else {
                nextContext = currentContext.getNextStepContext();
                if (nextContext) {
                  // Current action is into flow then go to next id into flow.
                  contextService.setNext(nextContext, true);
                } else {
                  // No other context, no flow, then go to previous.
                  nextContext = currentContext.previous;
                  if (undefined === nextContext) {
                    $location.path('/?');
                    return;
                  }
                }
              }
              contextService.goTo(nextContext);
            });
          });
        }
      }

      /**
       * Redirects to the next action page if it exists.
       * @param {Action} [currentAction] Current action.
       * @param {String[]} [pks] Entities' identifiers.
       * @param {Object} [options] Current action's options.
       * @param {Context} [context] Current context.
       * @returns {Promise} A promise which returns true while a redirection is performed; false otherwise.
       */
      function goToNextAction(currentAction, pks, options, context) {
        var params = {
          action: currentAction,
          actionName: currentAction.name.front,
          pks: pks,
          options: options
        };
        var entityData = context.getData();
        var nextActionFunction = customService.get('next-action');
        var nextActionObject = nextActionFunction(that.entity.name.front, 'action', entityData, params);
        if (nextActionObject){
          return nextActionObject.then(function (data) {
            if (data) {
              var keys;
              // Previous action was an INPUT_NONE, but now we may have an input
              if (currentAction.hasNoInput() && data.action.hasInput()) {
                var entityModel = currentAction.entity;
                keys = [];
                // The PK is extracted for the next action if it is complete.
                if (entityModel.isPrimaryKeyFull(entityData)) {
                  var pk = entityModel.getStringPrimaryKey(entityModel.extractPrimaryKey(entityData));
                  keys.push(pk);
                }
              } else {
                keys = pks;
              }
              context.action = data.action;
              var nextFunctions = data.functions || {};
              var nextOptions = data.options || {};
              // Calls redirectToPageAction to call the custom methods (menuAction, overrideAction).
              context.action.redirectToPageAction(keys, nextFunctions, nextOptions, false, true);
              return true;
            }
            return false;
          });
        } else {
          console.error('The custom function next-action for action can\'t return undefined.');
        }
      }

      /**
       * Execute create action.
       * Launch rest query, call the previous context callback with data result and delete current context.
       *
       * @param {Context} context current context.
       * @param {String[]} pks primary keys.
       * @param {Action} currentAction action of current view.
       * @param {Object} [options] options to execute action
       * @param {Link} [options.link] link form action to execute.
       * @param {String} [options.query] query to add to execute action.
       * @return {Promise} promise of context to go (undefined if need to go back).
       */
      function executeCreate(context, pks, currentAction, options) {
        if (that.hasLinkProcess()) {
          var currentContext = contextService.getCurrent();
          var link;
          var data =  dataToSend(currentContext.previous.getData());
          if (options) {
            link = options.link;
          }
          link = link || currentContext.options.link;
          return restService.link(that, pks, link, data)
            .then(
              callAfterExecute(context),
              errorExecute('Cannot link', arguments)
            );
        } else {
          return restService.create(that, context.getData())
            .then(
              callAfterExecute(context),
              errorExecute('Cannot create', arguments)
            );
        }
      }

      /**
       * Execute create read.
       * If current action and this are same when go to previous.
       * If current action and this are not same when go to next action.
       */
      function executeRead() {
        return $q.when(undefined);
      }

      /**
       *
       * @param {Context} context
       * @param {String[]} pks
       * @param {Action} currentAction
       * @return {Promise}
       */
      function executeUpdate(context, pks, currentAction) {
        var data = dataToSend(context.getData());
        if (that.hasMultipleInput()) {
          return restService.multipleSave(pks, that, data)
            .then(
              callAfterExecute(context),
              errorExecute('Cannot update', arguments)
            );
        } else if (that.hasSingleInput()) {
          if (pks.length === 1) {
            return restService.save(pks[0], that, data)
              .then(
                callAfterExecute(context),
                errorExecute('Cannot update', arguments)
              );
          }
          console.error('Cannot update multiple pks', that, context, pks, currentAction);
          throw new Error('Cannot update multiple pks');
        } else {
          // Action without selection
          return restService.noInputUpdate(that)
            .then(
              callAfterExecute(context),
              errorExecute('Cannot update', arguments)
            );
        }
      }

      /**
       *
       * @param {Context} context
       * @param {String[]} pks
       * @param {Action} currentAction
       * @param {Object} [options] options to execute action
       * @param {Link} [options.link] link form action to execute.
       * @param {String} [options.query] query to add to execute action.
       * @return {Promise}
       */
      function executeDelete(context, pks, currentAction, options) {
        if (that.hasLinkProcess()) {
          var currentContext = contextService.getCurrent();
          var link;
          if (options) {
            link = options.link;
          }
          return restService.link(that, pks, link, dataToSend(currentContext.getData()))
            .then(
              callAfterExecute(context),
              errorExecute('Cannot delete link', arguments)
            );
        } else {
          if (currentAction !== that && !that.isUINone()) {
            return $q.when(contextService.createNext(that, pks));
          } else {
            if (that.hasMultipleInput()) {
              if (pks && pks.length) {
                return restService.deleteMultiple(pks, that, dataToSend(context.getData())).then(
                  callAfterExecute(context),
                  errorExecute('Cannot delete', arguments)
                );
              } else {
                console.error('Cannot delete without pks', that, context, pks, currentAction);
                throw new Error('Cannot delete without pks');
              }
            }
            if (that.hasSingleInput()) {
              if (pks && pks.length === 1) {
                return restService.delete(pks[0], that, dataToSend(context.getData()))
                  .then(
                    callAfterExecute(context),
                    errorExecute('Cannot delete', arguments)
                  );
              } else {
                console.error('Cannot delete multiple pks', that, context, pks, currentAction);
                throw new Error('Cannot delete multiple pks');
              }
            }
          }
        }
        console.error('Not implemented', that, context, pks, currentAction);
        throw new Error('Not implemented');
      }

      function isEdit() {
        return !action['read-only'];
      }

      /**
       * Get function to say persistence
       * @param {String} persistence value persistence to test
       * @returns {Function} function to know if action with persistence
       */
      function isPersistence(persistence) {
        return function () {
          return action.persistence === persistence;
        };
      }

      function hasInput() {
        return that.hasSingleInput() || that.hasMultipleInput();
      }

      /**
       * Get function to say input
       * @param {String} input value input to test
       * @returns {Function} function to know if action with input
       */
      function hasInputEquals(input) {
        return function () {
          return action.input === input;
        };
      }

      /**
       * Get function to say process
       * @param {String} process value process to test
       * @returns {Function} function to know if action with process
       */
      function hasProcessEquals(process) {
        return function () {
          return action.process === process;
        };
      }

      function isDetach() {
        return that.hasLinkProcess() && that.isDelete();
      }

      function isAttach() {
        return that.hasLinkProcess() && that.isCreate();
      }

      function callAfterExecute(context) {
        return function (response) {
          if (!context.action.isDelete() && !that.isUINone()) {
            // Refresh entity with persisted one
            context.setData(response.data);
            if (response.data && response.data.primaryKey) {
              context.pks[0] = response.data.primaryKey;
            }
          }

          var callback = context.getFunction('afterExecute');
          if (callback) {
            return callback(response.data);
          }
        };
      }

      function errorExecute(message, params) {
        return function (error) {
          console.error(message, that, params, error);
          throw new Error(message);
        };
      }

      /**
       *
       * @param {String} typeFlux name of type flux (none, display, input)
       * @return {isFluxType} a function who return true or false.
       */
      function isUI(typeFlux) {
        /**
         * @name isFluxType
         * @function
         * @return {Boolean} true or false.
         */
        return function isFluxType() {
          return typeFlux === action['io-flux'];
        };
      }

      /**
       * Don't send data to back when action is without UI.
       * @param {Object} data
       * @return {Object}
       */
      function dataToSend (data) {
        if (!that.isUINone()) {
          return data;
        }
      }
    }
  }
}(window.angular));
