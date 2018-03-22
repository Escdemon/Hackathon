module.exports = (function (ng) {
  'use strict';

  require('./simple-link.less');

  SimpleLinkController.$inject = [
    'restService',
    'customService',
    'entityModel',
    'contextService',
    '$q',
    '$timeout',
    '$scope'];

  return {
    controller: SimpleLinkController,
    transclude: true,
    require: {
      innerTemplate: '?^^innerTemplate',
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./simple-link.template.html'),
    bindings: {
      // Css
      classInput: '@',
      classOffset: '@?',
      classLabel: '@?',

      label: '@?',
      tooltip: '@?',
      placeholder: '@?',

      isProtected: '<',
      mandatory: '<',

      prefix: '@',
      dataSrc: '<',
      actions: '<',
      entityName: '@',
      linkName: '@',
      quickSearch: '<',
      queryName: '@',
      searchQueryName: '@'
    }
  };

  function SimpleLinkController(restService,
                                customService,
                                entityModel,
                                contextService,
                                $q,
                                $timeout,
                                $scope) {
    var $ctrl = this,
      context = contextService.getCurrent(),
      originalForeignKey,
      firstLoad = true,
      onLinkChanged,
      onDataChanged;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.executeAction = executeAction;
    $ctrl.setDirty = setDirty;
    $ctrl.getQuickElement = getQuickElement;
    $ctrl.onSelectQuickSearch = onSelectQuickSearch;
    $ctrl.clearData = clearData;
    $ctrl.lostFocus = lostFocus;

    /**
     * Load component after set bindings.
     */
    function onInit() {
      $ctrl.formCtrl = $ctrl.innerTemplate || $ctrl.varForm || $ctrl.searchForm;
      // Var is never mandatory if it is in a searchTemplate
      if ($ctrl.searchForm) {
        $ctrl.mandatory = false;
      }
      context.resetFunction('afterExecute');
      $ctrl.id = context.getComponentId($ctrl.linkName);
      $ctrl.entity = entityModel.entity($ctrl.entityName);
      $ctrl.link = $ctrl.entity.getLink($ctrl.linkName);
      $ctrl.linkEntity = $ctrl.link.dstEntity;
      $ctrl.action = context.action;
      $ctrl.readOnly = $ctrl.action.readOnly;
      /**
       * Listen if the link may have changed
       */
      onLinkChanged = $scope.$on('link-changed-' + $ctrl.formCtrl.id, function (event, data) {
        if (!data.eventData) {
          return;
        }
        var columnsFKName = Object.keys($ctrl.link.fk);
        var loadNeeded = false;
        // If update is one of column of FK reload is needed
        for (var i = 0; i < columnsFKName.length; i++) {
          if (data.eventData.names.indexOf(columnsFKName[i]) !== -1) {
            loadNeeded = true;
            break;
          }
        }
        if (loadNeeded) {
          load();
        }
      });
      onDataChanged = $scope.$on('data-changed-' + $ctrl.formCtrl.id, function (event, eventData) {
        load(eventData.data);
      });
      $ctrl.formCtrl.promiseData.then(function (data) {
        load(data);
      });
    }

    function onDestroy() {
      if (onLinkChanged) {
        onLinkChanged();
      }
      if (onDataChanged) {
        onDataChanged();
      }
    }

    /**
     * Load component data.
     */
    function load(forceToLoad) {
      forceToLoad = forceToLoad || $ctrl.dataSrc;
      return loadLinkedEntity(forceToLoad)
        .then(function (data) {
          $ctrl.data = data;
          if (forceToLoad) {
            originalForeignKey = Object.keys($ctrl.link.fk).reduce(function (originalForeignKey, element) {
              originalForeignKey.push(forceToLoad[element]);
              return originalForeignKey;
            }, []);
            var formCtrl;
            // Form name is not same between inner template or root form.
            if ($ctrl.innerTemplate) {
              var topForm = $ctrl.innerTemplate.searchForm || $ctrl.innerTemplate.varForm;
              formCtrl = topForm.form['form-' + $ctrl.innerTemplate.id];
            } else {
              formCtrl = $ctrl.formCtrl ? $ctrl.formCtrl.form : null;
            }
            var inputForm = formCtrl ? formCtrl[$ctrl.id] : null;
            if (inputForm && firstLoad) {
              if (!ng.equals(originalForeignKey, $ctrl.foreignKey)) {
                inputForm.$setDirty();
              } else {
                inputForm.$setPristine();
              }
            }
          }
          loadActions(data);
          custom(data)
            .then(function () {
              $ctrl.focus = false;
            })
            .finally(updateSearchMode);

          firstLoad = false;
        });
    }

    function getParams() {
      return {
        linkName: $ctrl.link.name.front,
        actionName: context.action.name.front,
        prefix: $ctrl.prefix,
        isProtected: $ctrl.isProtected,
        mandatory: $ctrl.mandatory,
        label: $ctrl.label,
        tooltip: $ctrl.tooltip,
        placeholder: $ctrl.placeholder
      };
    }

    /**
     * Call all custom function.
     * @param {Object} entity entity loaded
     */
    function custom(entity) {
      var params = getParams();
      var linkEntityName = $ctrl.linkEntity.name.front;
      var promises = [];
      /*
       * array to define all custom functions to execute with :
       * - name = function's name
       * - fn = function to define where the result of th custom function will be saved/used
       * - reload = boolean to define if the custom function is needed on a page's reload
       * - correctParams = boolean to define if all needed parameters (for the custom function execution) are correct 
       */
      var customs = [
        {name: 'label', fn: applyLabel, reload: false},
        {name: 'tooltip', fn: applyTooltip, reload: false},
        {name: 'placeholder', fn: applyPlaceholder, reload: false},
        {name: 'protected', fn: applyProtected, reload: false},
        {name: 'hidden', fn: applyHidden, reload: false},
        {name: 'mandatory', fn: applyMandatory, reload: false}
      ];
      customs.forEach(function (custom) {
        if (firstLoad || custom.reload) {
          var customPromise = customService.get(custom.name)(linkEntityName, 'link', entity, params);
          pushPromise(customPromise, custom.fn, custom.name);
        }
      });

      // 'value' represent the displayed text for the link
      // If no entity is selected, nothing should be displayed
      var customPromise = entity ? customService.get('value')(linkEntityName, 'link', entity, params) : $q.when('');
      pushPromise(customPromise, applyValue(entity), 'value');

      return $q.all(promises);

      function pushPromise(promise, fn, name) {
        if (promise){
          promises.push(
            promise.then(fn)
          );
        } else {
          console.error('The custom function ' + name + ' for link can\'t return undefined.');
        }
      }
    }

    function applyLabel(label) {
      $ctrl.labelDisplay = label;
    }

    function applyTooltip(tooltip) {
      $ctrl.tooltipDisplay = tooltip;
    }

    function applyPlaceholder(placeholder) {
      $ctrl.placeholderDisplay = placeholder;
    }

    function applyValue(entity) {
      return function (value) {
        $ctrl.bindValue = {
          display: value,
          data: entity
        };
      };
    }

    function applyProtected(isProtected) {
      $ctrl.isProtected = !$ctrl.quickSearch || isProtected || ($ctrl.action.isUIDisplay() && !$ctrl.searchForm);
    }

    function applyHidden(hidden) {
      $ctrl.hidden = hidden;
    }

    function applyMandatory(mandatory) {
      $ctrl.mandatory = mandatory;

      if ($ctrl.mandatory) {
        $ctrl.classLabel += ' mandatory';
      }
    }

    function loadActions(entity) {
      $ctrl.displayActions = $ctrl.actions.reduce(function (actions, strAction) {
        var action = $ctrl.linkEntity.getAction(strAction);
        if (action.isDisplayable(context.action, $ctrl.isProtected, entity, $ctrl.mandatory, true)) {
          if (action.hasInput()) {
            $ctrl.defaultAction = $ctrl.defaultAction || action;
          }
          if (action.isAttach()) {
            $ctrl.defaultButton = action;
          } else {
            actions.push(action);
          }
        }
        return actions;
      }, []);
    }

    /**
     * Load link when data on form is loaded.
     * @param {Object} entityForm entity from form.
     */
    function loadLinkedEntity(entityForm) {
      if (!entityForm || entityForm === {}) {
        return $q.when();
      }
      $ctrl.foreignKey = [];
      var foreignKeySet = false;
      Object.keys($ctrl.link.fk).forEach(function (element) {
        $ctrl.foreignKey.push(entityForm[element]);
        if (entityForm[element] !== undefined && entityForm[element] !== null) {
          foreignKeySet = true;
        }
      });
      if (!foreignKeySet) {
        return $q.when();
      }
      var cache = context.getCache($ctrl.linkEntity, $ctrl.foreignKey);
      if (cache) {
        return cache;
      } else if ($ctrl.foreignKey && $ctrl.foreignKey.length) {
        // Load from rest
        var action = context.action;
        var dataPromise = restService.backRef(
          $ctrl.linkEntity.name.front,
          $ctrl.linkEntity.getStringPrimaryKey($ctrl.foreignKey),
          action.name.back,
          $ctrl.link.name.back,
          action.entity.name.back
        )
          .then(function (result) {
            return result.data;
          });
        context.setCache($ctrl.linkEntity, $ctrl.foreignKey, dataPromise);
        return dataPromise;
      }
    }

    /**
     * Execute given action.
     * @param {Action} action to perform.
     */
    function executeAction(action) {
      if (action.isDetach()) {
        // Delete all information from link.
        reset();
      } else {
        action.redirectToPageAction(
          [$ctrl.linkEntity.getStringPrimaryKey($ctrl.foreignKey)],
          {
            afterExecute: saveEntityFormNext,
            execute: execute
          },
          {
            link: $ctrl.link,
            query: $ctrl.searchQueryName,
            inputOne: true // true if this action as an unique input
          });
      }
    }

    function execute(realExecute, actionToExecute, currentContext, ids, currentAction, functions, options) {
      if (!actionToExecute.isAttach()) {
        return realExecute(currentContext, ids, currentAction, functions, options);
      } else {
        saveEntityFormNext($ctrl.linkEntity.getPrimaryKeyFromString(ids[0]));
      }
    }

    function saveEntityFormNext(entity) {
      if (!entity) {
        return;
      }
      var contextData = $ctrl.searchForm ? $ctrl.dataSrc : context.getData();
      $ctrl.foreignKey = [];
      Object.keys($ctrl.link.fk).forEach(function (element) {
        contextData[element] = entity[$ctrl.link.fk[element]];
        $ctrl.foreignKey.push(contextData[element]);
      });
    }

    function resetData(data) {
      Object.keys($ctrl.link.fk).forEach(function (element) {
        delete data[element];
      });
    }

    function reset() {
      if ($ctrl.searchForm) {
        resetData($ctrl.dataSrc);
      } else {
        $ctrl.formCtrl.promiseData.then(resetData);
      }
      $ctrl.bindValue = {};
      delete $ctrl.defaultAction;
      delete $ctrl.defaultButton;
      return load();
    }

    function setDirty() {
      $ctrl.formCtrl.form[$ctrl.id + '-hidden'].$dirty = true;
    }

    /**
     * Get quick element
     * @param {String} filter filter to launch query
     * @return {Promise}
     */
    function getQuickElement(filter) {
      return restService.query($ctrl.linkEntity, $ctrl.searchQueryName, {search: filter}).then(function (result) {
        var formatedResult = [];
        var params = ng.extend({}, getParams(), {resultList: true});
        result.data.results.forEach(function (result) {
          var getValue = customService.get('value')($ctrl.linkEntity.name.front, 'link', result, params);
          if (getValue){
            getValue.then(function (value) {
              formatedResult.push({
                data: result,
                display: value
              });
            });
          } else {
            console.error('The custom function value for link can\'t return undefined.');
          }
        });
        return formatedResult;
      });
    }

    /**
     * Change data into form + load real entity to display.
     */
    function onSelectQuickSearch() {
      // launch load before change data into form to not load multi-time same data.
      var toLoad = {};
      $ctrl.link.setLinkedEntity($ctrl.linkEntity.getPrimaryKeyFromString($ctrl.bindValue.data.primaryKey), toLoad);
      load(ng.extend({}, $ctrl.dataSrc, toLoad)).then(function () {
        ng.extend($ctrl.dataSrc, toLoad);
        change();
      });
    }

    function clearData() {
      reset().then(function () {
        change();
        updateSearchMode();
        $timeout(function () {
          $ctrl.focus = true;
        });
      });
    }

    function lostFocus() {
      updateSearchMode();
      $ctrl.focus = false;
      setDirty();
    }

    function updateSearchMode() {
      var unmodifiable = $ctrl.readOnly || $ctrl.isProtected;
      var dataSet = $ctrl.bindValue && $ctrl.bindValue.data;
      $ctrl.searchMode = $ctrl.quickSearch && !unmodifiable && !dataSet;
    }

    function change() {
      var toEmit = {
        idParent: $ctrl.formCtrl.id,
        idComponent: $ctrl.id,
        names: Object.keys($ctrl.link.fk),
        values: $ctrl.foreignKey
      };
      /**
       * Indicate to others links : this link have changed
       */
      $scope.$emit('link-updated-' + $ctrl.formCtrl.id, toEmit);
    }
  }
}(window.angular));
