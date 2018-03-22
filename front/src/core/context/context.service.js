module.exports = (function () {
  'use strict';
  serviceFactory.$inject = ['$location', 'Context', '$route', 'customService', 'titleService', '$rootScope', 'entityModel', 'loginService'];
  const paramSearchName = 'c';

  return serviceFactory;

  function serviceFactory($location, Context, $route, customService, titleService, $rootScope, entityModel, loginService) {
    
    const sessionCurrentContext = 'session.current.context';

    /**
     * stack of context.
     * @type {Context}
     */
    var _currentContext = undefined;

    $rootScope.$on('$locationChangeSuccess', function (event, newUrl, oldUrl) {
      if ($location.path() === '/logout' || $location.path() === '/') {
        // LogOut or DefaultPage event, reset context and position
        setContext(undefined);
      }
    });

    return {
      getIdContext: getIdContext,
      getCurrent: getCurrent,
      initCurrent: initCurrent,
      replaceCurrent: replaceCurrent,
      createNext: createNext,
      setNext: setNext,
      goTo: goTo,
      goToPrevious: goToPrevious,
      goToNextInFlow: goToNextInFlow,
      reload: reload,
      loaded: loaded
    };

    /**
     * Replace to current context with a new one.
     * @param {Context} context context to save.
     */
    function setContext(ctx) {
      _currentContext = ctx;
      saveContext();
    }

    function saveContext() {
      if (_currentContext) {
        sessionStorage.setItem(sessionCurrentContext, JSON.stringify(_currentContext));
      } else {
        $location.search(paramSearchName, null);
        sessionStorage.removeItem(sessionCurrentContext);
      }
    }

    /**
     * Get a context by ID.
     * @param {Integer} id ID of the context
     */
    function getContext(id) {
      var ctx = _currentContext;
      while (ctx) {
        if (ctx.idContext === id) {
          return ctx;
        }
        ctx = ctx.previous;
      }
      return undefined;
    }

    function initCurrent(action, pks, begin) {
      var currentId = begin ? 0 : getIdContext();
      var context = getContext(currentId);
      if (undefined === context || !context.isSame(action, pks)) {
        var previous;
        var pksOfContext = pks;
        if (currentId > 0) {
          previous = _currentContext;
        } else {
          pksOfContext = [pks[0]];
        }
        context = new Context(action, pksOfContext, currentId, previous, getFlow(action, pks));
      }
      return context;
    }

    /**
     * Replaces the current context (the top one into the context stack) by a new context.
     * 
     * @param {Action} action action to execute.
     * @param {String[]} pks Array of serialized primary keys.
     * @returns {Context} The new context.
     */
    function replaceCurrent(action, pks) {
      // Create new context.
      var ctx = new Context(action, getNextPk(action, pks), _currentContext.idContext, _currentContext.previous, getFlow(action, pks));
      setContext(ctx);
      return ctx;
    }

    /**
     * Set next context into stack context into context id context.
     * @param {Context} context context to set into context stack.
     * @param {Boolean} replaceCurrent replace current context for flow.
     * @throws {Error} if context is falsy or context cannot be add at own id context into context stack.
     */
    function setNext(context, replaceCurrent) {
      if (!context 
        || context.idContext < 0 
        || (!replaceCurrent && (context.idContext !== _currentContext.idContext + 1))
        || (replaceCurrent && (context.idContext !== _currentContext.idContext))
      ) {
        throw new Error('Context need to be add into context stack', _currentContext, context);
      }
      if (!replaceCurrent) {
        if (_currentContext) {
          context.previous = _currentContext;
        } else {
          context.idContext = 0;
        }
      }
      setContext(context);
    }

    /**
     * Create next context form current context (Will NOT be added to the stack, only prepared).
     * 
     * @param {Action} action action of next context.
     * @param {String[]} pks
     * @param {Object} [options] options to execute action
     * @param {Link} [options.link] link form action to execute.
     * @return {Context|undefined} context if can be created
     */
    function createNext(action, pks, options) {
      if (_currentContext.isInFlow() && (action === _currentContext.action || !action)) {
        // Get next context in flow if exist
        return _currentContext.getNextStepContext(!!action);
      }
      return new Context(action, getNextPk(action, pks), _currentContext.idContext + 1, _currentContext, getFlow(action, pks), options);
    }

    /**
     * Get id into context stack.
     * @returns {Number} current id find into url.
     */
    function getIdContext() {
      return parseInt($location.search()[paramSearchName], 10) || 0;
    }

    function getFlow(action, pks) {
      if (!action.hasSingleInput()) {
        return;
      }
      return {
        pks: pks,
        executedPks: [],
        id: 0
      };
    }

    /**
     * @param {Action} action
     * @param {String[]} pks array of primary keys.
     * @return {String[]} array of primary keys.
     */
    function getNextPk(action, pks) {
      var nextPk = pks;
      if (action.hasMultipleInput()) {
        if (!Array.isArray(pks)) {
          nextPk = [pks];
        } else if (!pks.length) {
          nextPk = undefined;
        }
      } else {
        if (Array.isArray(pks)) {
          if (pks.length) {
            nextPk = [pks[0]];
          } else {
            nextPk = undefined;
          }
        }
      }
      return nextPk;
    }

    /**
     * Restore a Context stack from the given input string.
     * @param {String} ctxJson String representation of a Context object
     * @returns {Context}
     */
    function getContextFromJSON(ctxJson) {
      if (ctxJson.previous) {
        // Restore previous context first
        var previousCtx = getContextFromJSON(ctxJson.previous);
      }
      var entity = entityModel.entity(ctxJson.action.entity.name.front);
      var ctx = new Context(entity.getAction(ctxJson.action.name.front), ctxJson.pks, ctxJson.idContext, previousCtx, ctxJson.flow, ctxJson.options);
      ctx.title = ctxJson.title;
      ctx.titleTooltip = ctxJson.titleTooltip;
      return ctx;
    }

    /**
     * @param {Boolean} [noRedirect] do not redirect if current not exist.
     * @return {Context | undefined} current context.
     */
    function getCurrent(noRedirect) {
      if (!_currentContext) {
        if (!noRedirect) {
          try {
            // Try to restore context from sessionStrorage
            if (loginService.isConnected()) {
              // Only if user is still logged
              var url = $location.url();
              var contextStr = sessionStorage.getItem(sessionCurrentContext);
              if (url !== '/' && contextStr) {
                // Only if user didn't ask for the root url
                // And there is a saved context, restore true objects
                var contextObj = getContextFromJSON(JSON.parse(contextStr));
                setContext(contextObj);

                if (contextObj) {
                  // Successfully restored context and found an active one
                  $rootScope.$broadcast('context.restore-success');

                  // Check current action validity
                  if (contextObj.action.process === 'link') {
                    // Attach action cannot be restored, go back
                    goTo(contextObj.previous);
                  }

                  return contextObj;
                }
              }
            } else {
              // Purge sessionstorage
              sessionStorage.removeItem(sessionCurrentContext);
            }
          } catch (e) {
            console.error("Error during context restoration", e);
          }
          
          // Not able to restore contextes, reset everything that might be in memory
          setContext(undefined);
          console.info('No context found. Redirect to home page.');
          // And forward the user to the default page
          $location.path('/');
        }

        return undefined;
      }

      return _currentContext;
    }

    /**
     * Load context.
     * @param {Context} context context to go.
     */
    function goTo(context) {
      context.resetComponentIds();
      setContext(context);
      
      var nextPath = context.getPath();
      if ($location.url() !== nextPath) {
        $location.url(nextPath);
      } else {
        reload();
      }
    }

    /**
     * Go to previous context.
     */
    function goToPrevious() {
      goTo(_currentContext.previous);
    }

    /**
     * Reload current context
     */
    function reload() {
      // To force reload current data
      _currentContext.setData(undefined);
      _currentContext.clearCache();
      $route.reload();
    }

    /**
     *
     * @param {Boolean} isCurrentSkip if current is skip.
     */
    function goToNextInFlow(isCurrentSkip) {
      if (!_currentContext.isInFlow()) {
        throw new Error('Cannot go to next in flow because flow is not set');
      }
      var nextContext = _currentContext.getNextStepContext(isCurrentSkip);
      if (nextContext) {
        setNext(nextContext, true);
        goTo(nextContext);
      } else {
        goToPrevious();
      }
    }

    /**
     * Call when screen is loaded.
     */
    function loaded() {
      var customTooltipSetter = customService.get('titleTooltip');
      var customTitleSetter = customService.get('title');
      var entityName = _currentContext.action.entity.name.front;
      var params = {
        'action-name': _currentContext.action.name.front,
        action: _currentContext.action,
        inFlow: _currentContext.isInFlow(),
        context: _currentContext,
        idContext: _currentContext.idContext
      };
      var titlePromise, titleTooltip;
      if (_currentContext.promiseData) {
        titlePromise = _currentContext.promiseData.then(function (data) {
          return customTitleSetter(entityName, 'context', data, params);
        });
        titleTooltip = _currentContext.promiseData.then(function (data) {
          return customTooltipSetter(entityName, 'context', data, params);
        });
      } else {
        titlePromise = customTitleSetter(entityName, 'context', _currentContext.getData(), params);
        titleTooltip = customTooltipSetter(entityName, 'context', _currentContext.getData(), params);
      }
      titlePromise.then(function (title) {
        var val = title.toString();
        titleService.set(val);
        if (_currentContext.title !== val) {
          // Title changed
          _currentContext.title = val;  
          saveContext();
        }
      });
      titleTooltip.then(function (tooltip) {
        var val = tooltip.toString();
        if (_currentContext.titleTooltip !== val) {
          // tooltip changed
          _currentContext.titleTooltip = val;
          saveContext();
        }
      });
    }
  }
}());
