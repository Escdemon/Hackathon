module.exports = (function (ng) {
  'use strict';
  return factory;
  function factory() {
    return Context;
    /**
     * @typedef {Object} ContextOptions
     * @property {Link} [link] link.
     * @property {String} [query} query name.
     */
    /**
     *
     * @param {Action} action action execute or to execute.
     * @param {String[]} pks list of primary keys onto execute action.
     * @param {Number} idContext idContext into context stack.
     * @param {Context} [previous] previous context.
     * @param {Object} [flow] object represent flow execution.
     * @param {String[]} flow.pks all ids who can be execute in flow.
     * @param {String[]} [flow.executedPks] all ids executed in flow.
     * @param {Number} [flow.id] id of current step into flow.
     * @param {ContextOptions} [options] options to execute action.
     * @param {Link} [options.link] link form action to execute.
     * @param {String} [options.query] query to add to execute action.
     *
     * @constructor
     */
    function Context(action, pks, idContext, previous, flow, options) {
      var paramSearchName = 'c';
      var that = this;
      var cache = {};
      /**
       * Functions to override default behaviour (used for ex in simple links)
       * @type {Object}
       */
      var functions;
      var data = {origin: undefined, dirty: undefined};
      var component = {
        ids: {},
        states: {}
      };
      /**
       * Action.
       * @type {Action}
       */
      this.action = action;
      /**
       * Array of entities primary key.
       * @type {String[]}
       */
      this.pks = pks || [];
      /**
       * Previous context.
       * @type {Context}
       */
      this.previous = previous;
      /**
       * Id Context into stack of context.
       * @type {Number}
       */
      this.idContext = idContext;
      /**
       * object represent flow execution.
       * @type {Object}
       */
      this.flow = flow;
      /**
       * options to execute action
       * @type {ContextOptions}
       */
      this.options = options || {};
      /**
       * @type {Object[]}
       */
      this.selectedRows = [];
      /**
       * @type {String} Current page title (used in breadcrumb)
       */
      this.title = "";
      /**
       * @type {String} Current page tooltip (used in breadcrumb)
       */
      this.titleTooltip = "";
      /**
       * @type {String} Custom page
       */
      this.customPath = "";

      this.getNextStepContext = getNextStepContext;
      this.isSame = isSame;
      this.getCache = getCache;
      this.setCache = setCache;
      this.clearCache = clearCache;
      this.isInFlow = isInFlow;
      this.getFlow = getFlow;
      this.getPath = getPath;
      this.resetFunction = resetFunction;
      this.setFunctions = setFunctions;
      this.getFunction = getFunction;
      this.setData = setData;
      this.getData = getData;
      this.saveComponentState = saveComponentState;
      this.getComponentState = getComponentState;
      this.getComponentId = getComponentId;
      this.resetComponentIds = resetComponentIds;
      this.destroy = destroy;
      // Implementations

      /**
       * To get next step context.
       * @param {Boolean} [isCurrentSkip] if current step has been skip.
       * @returns {Context|undefined} return next step context or undefined if no next step exist.
       * @function
       */
      function getNextStepContext(isCurrentSkip) {
        if (!this.flow || !this.flow.pks) {
          return;
        }
        this.flow.executedPks = this.flow.executedPks || [];
        this.flow.id = this.flow.id || 0;
        var currentPk = this.flow.pks[this.flow.id];
        var nextFlow =  {
          pks: this.flow.pks,
          id: this.flow.id + 1,
          executedPks: this.flow.executedPks.slice()
        };
        if (!isCurrentSkip) {
          nextFlow.executedPks.push(currentPk);
        }
        if (nextFlow.id === nextFlow.pks.length) {
          return;
        }
        return new Context(action, [nextFlow.pks[nextFlow.id]], idContext, previous, nextFlow);
      }

      /**
       * @return {Boolean} true if context is part of flow action (same action execute on many primary key).
       * @function
       */
      function isInFlow() {
        return !!this.flow && !!this.flow.pks;
      }

      /**
       * @typedef {Object} Flow
       * @property {String[]} pks all primary key to execute
       * @property {String[]} [executedPks] effective execute primary key
       * @property {Number} id id of primary to execute
       */
      /**
       * @return {Flow}
       * @function
       */
      function getFlow() {
        return this.flow;
      }

      /**
       * @param {Action} actionToTest action to test.
       * @param {String[]} pksToTest list of primary key.
       * @return {Boolean} true if context have same action and ids.
       * @function
       */
      function isSame(actionToTest, pksToTest) {
        return that.action === actionToTest && arraysEqual(that.pks, pksToTest ? pksToTest : []);
      }

      /**
       * @param {Entity} entity entity definition to save into cache
       * @param {String[]} pks primary key of cache
       * @return {Object|undefined} cache or nothing
       * @function
       */
      function getCache(entity, pks) {
        var entityName = entity.name.front;
        var cacheEntity = cache[entityName];
        if (cacheEntity) {
          return cacheEntity[pks.join()];
        }
      }

      /**
       * @param {Entity} entity entity definition to save into cache
       * @param {String[]} pks primary key of cache
       * @param {Object} value entity.
       * @param {Object} [value.loaded] original value of entity.
       * @param {Object} value.form value of entity modify by form.
       * @function
       */
      function setCache(entity, pks, value) {
        cache[entity.name.front] = cache[entity.name.front] || [];
        cache[entity.name.front][pks.join()] = value;
      }

      function clearCache() {
        cache.length = 0;
      }

      /**
       * Get route of this context.
       * @return {string} route of this context.
       */
      function getPath() {
        if (that.customPath) {
          return encodeURI(that.customPath);
        }

        var queryParam = false;
        var action = that.action;
        var pks = that.pks;
        var path = '/' + action.entity.name.front + '/' + action.name.front;
        if (pks && action.hasSingleInput()) {
          path += '/';
          if (Array.isArray(pks)) {
            path += pks[0];
          } else {
            path += pks;
          }
        }
        if (action.hasLinkProcess() && that.options.link) {
          if (action.entity === that.options.link.dstEntity) {
            path += '/' + that.options.link.srcEntity.name.front;
          } else {
            path += '/' + that.options.link.dstEntity.name.front;
          }
          path += '/' + that.options.link.name.front;
          // Query is not necessary anymore as the action's template contains the right query.
          // path += '/' + that.options.query;
        }
        if (pks && action.hasMultipleInput()) {
          if (Array.isArray(pks)) {
            path += '?pks=' + pks.join('&pks=');
          } else {
            path += '?pks=' + pks;
          }
          queryParam = true;
        }
        if (idContext !== undefined) {
          path += queryParam ? '&' : '?';
          path += paramSearchName + '=' + idContext;
        }
        return encodeURI(path);
      }

      /**
       * Set function call by next context when action is perform.
       * @param {Object} fns
       * @param {Function} [fns.execute] call in place of action execute.
       * @param {Function} [fns.afterExecute]
       * @param {Function} [fns.afterBackLoad]
       */
      function setFunctions(fns) {
        functions = fns;
      }

      function getFunction(name) {
        if (!functions) {
          return;
        }
        return functions[name];
      }

      /**
       * Reset call back.
       */
      function resetFunction(name) {
        if (functions) {
          delete functions[name];
        }
      }

      function setData(dataToSave) {
        data.origin = ng.extend({}, dataToSave);
        data.dirty = dataToSave;
      }

      function getData(original) {
        if (original) {
          return data.origin;
        }
        return data.dirty;
      }

      /**
       * Saves the state of a component into this context.
       *
       * @param {string} componentId Component's identifier.
       * @param {object} state Component's state (visibility, protectability, etc).
       */
      function saveComponentState(componentId, state) {
        if (!componentId || !state) {
          return;
        }
        if (ng.isDefined(component.states[componentId])) {
          ng.extend(component.states[componentId], state);
        } else {
          component.states[componentId] = state;
        }
      }

      /**
       * Retrieves a component state for the given id.
       *
       * @param {string} componentId Component's identifier.
       * @returns The component's state or an empty object to avoid any error.
       */
      function getComponentState(componentId) {
        return component.states[componentId] || {};
      }

      /**
       * Returns an unique identifier from the given name.
       *
       * @param {string} name Component's name.
       * @returns {string} An unique identifier.
       */
      function getComponentId(name) {

        if (!component.ids[name]) {// may be undefined or equals to 0.
          component.ids[name] = 1;
          return name;

        } else {
          return name + '-' + component.ids[name]++;
        }
      }

      /**
       * Resets the dictionary of components' identifier.
       */
      function resetComponentIds() {
        component.ids = {};
      }

      function destroy() {
        component = undefined;
        that.action = undefined;
        that.nextContext = undefined;
        that.previous = undefined;
        that.selectedRows = undefined;
      }
    }
  }

  function arraysEqual(a, b) {
    if (a === b) {
      return true;
    }
    if (a === null || b === null) {
      return false;
    }
    if (a.length !== b.length) {
      return false;
    }
    for (var i = 0; i < a.length; ++i) {
      if (a[i] !== b[i]) {
        return false;
      }
    }
    return true;
  }
}(window.angular));
