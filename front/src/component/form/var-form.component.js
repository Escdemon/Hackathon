module.exports = (function (ng) {
  'use strict';

  VarFormController.$inject = [
    'contextService',
    'entityModel',
    '$routeParams',
    'loadService',
    '$window',
    '$translate',
    '$uibModal',
    '$rootScope',
    '$location',
    '$scope',
    '$document',
    'customService',
    'messageService'
  ];

  return {
    transclude: true,
    template: require('./var-form.template.html'),
    bindings: {
      actionName: '@',
      entityName: '@'
    },
    controller: VarFormController
  };

  /**
   *
   * @param {contextService} contextService
   * @param {entityModel} entityModel
   * @param {$routeParams} $routeParams service angular.
   * @param {loadService} loadService
   * @param {$window} $window service angular.
   * @param {$translate} $translate service angular-translate.
   * @param {$uibModal} $uibModal service ui-boostrap.
   * @param {$rootScope} $rootScope service angular.
   * @param {$location} $location service angular.
   * @param {$scope} $scope service angular.
   * @param {$document} $document service angular.
   * @param {customService} customService custom service.
   * @param {messageService} messageService message service.
   * @constructor
   */
  function VarFormController(contextService,
                             entityModel,
                             $routeParams,
                             loadService,
                             $window,
                             $translate,
                             $uibModal,
                             $rootScope,
                             $location,
                             $scope,
                             $document,
                             customService,
                             messageService) {
    /**
     * Definition of a button.
     * @typedef {Object} FormButton
     * @property {function} execute - function call onclick button.
     * @property {String} label - translate key to display on button.
     * @property {String} css - indicate names of css class.
     * @property {boolean} [requireValidForm] - indicate whether onclick function is present.
     */
    var $ctrl = this;
    var unregisteredLocationChangeStart;
    var unregisteredOnUpdate;
    $ctrl.defaultSubAction = undefined;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.validate = validate;
    $ctrl.cancel = cancel;
    $ctrl.isDirty = isDirty;
    var isDirtyPopupDisplayed = false;

    function validate(subAction) {
      if (!$ctrl.form.$valid) {
        messageService.display({
          display: $translate.instant('var-form.invalid-form').toString(),
          level: 'danger',
          duration: 1500
        });
        return;
      }

      if (unregisteredLocationChangeStart) {
        unregisteredLocationChangeStart();
      }
      if (subAction) {
        $ctrl.action.selectedSubAction = subAction;
      }
      $ctrl.promiseData.then(function (data) {
        var currentContext = contextService.getCurrent();
        if (Array.isArray(data)) {
          // In case of query list.
          var pks = currentContext.selectedRows.reduce(function (cumulativePks, row) {
            cumulativePks.push(row.primaryKey);
            return cumulativePks;
          }, []);
          $ctrl.action.execute(pks, $ctrl.action);

        } else if ($ctrl.action.hasMultipleInput()) {
          $ctrl.action.execute(currentContext.pks, $ctrl.action);
        } else {
          var originalData = currentContext.getData(true) || data;
          $ctrl.action.execute([originalData.primaryKey], $ctrl.action);
        }
      }, function () {
        unregisteredLocationChangeStart = $rootScope.$on('$locationChangeStart', locationChangeStartHander);
      });
    }

    function cancel(skipOne) {
      checkDirtyness(function () {
        $ctrl.action.cancel(skipOne);
      });
    }

    function onInit() {
      $ctrl.entity = entityModel.entity($ctrl.entityName);
      $ctrl.action = $ctrl.entity.getAction($ctrl.actionName);
      var pks =
        $routeParams.pks === undefined ? [] : Array.isArray($routeParams.pks) ? $routeParams.pks : [$routeParams.pks];
      $ctrl.ctx = contextService.getCurrent();
      if (!$ctrl.ctx) {
        return;
      }
      $ctrl.flow = $ctrl.ctx.getFlow();
      $ctrl.id = $ctrl.ctx.getComponentId('form');
      $ctrl.edit = !$ctrl.action.readOnly;
      $ctrl.promiseData = loadService.loadData($ctrl.action, pks);
      // Adding listeners to handle the form's dirtyness.
      $window.addEventListener('beforeunload', onBeforeUnload);
      $document.on("keydown", onKeydown);
      unregisteredLocationChangeStart = $rootScope.$on('$locationChangeStart', locationChangeStartHander);

      unregisteredOnUpdate = $scope.$on('link-updated-' + $ctrl.id, function (event, eventData) {
        $ctrl.promiseData.then(function(data) {
          $scope.$broadcast('link-changed-' + $ctrl.id, {data: data, eventData: eventData});
        });
      });
      $ctrl.promiseData.then(function(data) {
        customService.get('entity-post-load')($ctrl.entityName, 'var-form', data, customParam());
        contextService.loaded();
        if ($ctrl.action) {
          manageButton($ctrl.action, data);
        }
      });
    }

    /**
     * Add buttons to display into this form.
     * @param {Action} action Current action to determine which buttons to display.
     * @param {Object} data Edited entity.
     */
    function manageButton(action, data) {
      if (action.hasQueryInput()) {
        return;
      }
      var buttons = [];
      if (action.subActions) {
        var isSubActionVisible = customService.get('sub-action-visible');
        action.subActions.forEach(function(subAction) {
          if (isSubActionVisible($ctrl.entityName, 'var-form', data, customParam(subAction.code.front))) {
            buttons.push(subActionSubmit(subAction));
            if (ng.isUndefined($ctrl.defaultSubAction)) {
              $ctrl.defaultSubAction = subAction;
            }
          }
        });
      } else if ((!action.hasNoPersistence() || action.hasCustomProcess()) && !action.hasLinkProcess()) {
        // Les actions de read n'ont pas besoin de bouton "Valider" (sauf si elles sont custom => besoin d'envoyer le resultat au back)
        buttons.push(defaultSubmit());
      }
      var currentContext = contextService.getCurrent();
      if (currentContext.idContext !== 0) {
        buttons.push(defaultBack());
      }
      if (currentContext.isInFlow()) {
        var flow = currentContext.getFlow();
        if (flow.id < flow.pks.length - 1) {
          buttons.push(defaultStop());
        }
      }
      $ctrl.buttons = buttons;
    }

    /**
     * Definition of a sub-action button.
     * @return FormButton
     */
    function subActionSubmit(subAction) {
      return {
        execute: $ctrl.validate,
        subAction: subAction,
        label: subAction.label,
        css: 'btn btn-primary btn-toy ' + subAction.code.front,
        requireValidForm: true
      };
    }

    /**
     * Definition of default submit button.
     * @return FormButton
     */
    function defaultSubmit() {
      return {
        execute: $ctrl.validate,
        label: null,
        css: 'btn btn-primary btn-round-toy btn-round-lg-toy icon-toy-check',
        requireValidForm: true
      };
    }

    /**
     * Definition of default back button.
     * @return FormButton
     */
    function defaultBack() {
      return {
        execute: function () {
          $ctrl.cancel(false);
        },
        label: null,
        css: 'btn btn-primary btn-round-toy btn-round-lg-toy icon-toy-close'
      };
    }

    /**
     * Definition of default stop button.
     * @return FormButton
     */
    function defaultStop() {
      return {
        execute: function () {
          $ctrl.cancel(true);
        },
        label: 'var-form.skip',
        css: 'btn btn-primary btn-toy'
      };
    }

    function onDestroy() {
      if ($ctrl.promiseData) {
        $ctrl.promiseData.then(function(data) {
          customService.get('entity-unload')($ctrl.entityName, 'var-form', data, customParam());
        });
      }
      // Removing listeners to handle the form's dirtyness.
      if (unregisteredLocationChangeStart) {
        unregisteredLocationChangeStart();
      }
      if (unregisteredOnUpdate) {
        unregisteredOnUpdate();
      }
      $window.removeEventListener('beforeunload', onBeforeUnload);
      $document.off("keydown");
    }

    /**
     * Displays a confirm dialog if the form is dirty.
     *
     * @param {Event} event Before unload DOM event.
     * @returns {String} A message to prevent exiting, undefined otherwise.
     */
    function onBeforeUnload(event) {
      if ($ctrl.form && $ctrl.form.$dirty) {
        var msg = $translate.instant('var-form.modal-dirty-text').toString();
        event.returnValue = msg;
        return msg;
      }
      return undefined;
    }

    /**
     * Checks dirtyness on location change.
     *
     * @param {Event} event Location change event.
     */
    function locationChangeStartHander(event) {
      var url = $location.url();
      var newCtx = contextService.getCurrent() || {idContext: 0};
      // if idContext is greater, the new page is next into the current flow.
      if ($ctrl.isDirty() && newCtx.idContext <= $ctrl.ctx.idContext) {
        checkDirtyness(function () {
          // User accepts to cancel its updates, let's redirect.
          $location.url(url);
        });
        // event is prevented as the modal is asynchronous.
        event.preventDefault();
      }
    }
    
    /**
     * Function for the keydown event, behavior depends on the key :
     *   - Enter : validate the form if all conditions are met
     *   - Escape : cancel the form
     * 
     * @params {KeyboardEvent} event the keyboard event
     */
    function onKeydown(event) {
      if (isDirtyPopupDisplayed) {
        // Dirty popup is displayed, do not handle key strokes
        return event;
      }

      var handled = false;
      if (event.key === "Enter" && !event.shiftKey
          && event.target.tagName !== "TEXTAREA" && event.target.tagName !== "BUTTON" && event.target.tagName !== "A") {
        /** 
         * Condition to validate the form:
         * - it's the Enter key
         * - it's not the shift + enter key
         * - the event doesn't come from a <textarea/> (because the enter key is for a newLine) 
         *   or from a <button/> (to not validate the form twice) 
         *   or from a link <a/> (because it might validate and open the link)
         */
        validate($ctrl.defaultSubAction);
        handled = true;
      } else if (event.key === "Escape") {
        /** 
         * Condition to validate the form:
         * - it's the Escape key
         */
        cancel(false);
        handled = true;
      }

      if (handled) {
        // Event trapped, cancel default behaviour
        event.stopPropagation();
        event.preventDefault();
      }
    }

    /**
     * Displays a confirm dialog if the form is dirty.
     *
     * @param {Function} callback Function to execute if the form is not dirty or the form is dirty and the user accepts to cancel its updates.
     */
    function checkDirtyness(callback) {
      if ($ctrl.isDirty()) {
        var modalInstance = $uibModal.open({
          ariaLabelledBy: 'modal-title',
          ariaDescribedBy: 'modal-body',
          template: require('./dirty-dialog.template.html')
        });
        isDirtyPopupDisplayed = true;
        modalInstance.closed.then(function() {
          isDirtyPopupDisplayed = false;
        });
        modalInstance.result.then(function () {
          if (unregisteredLocationChangeStart) {
            unregisteredLocationChangeStart();
          }
          callback();
        });
      } else {
        callback();
      }
    }

    /**
     * Indicates whether this form is dirty or not.
     *
     * @returns {Boolean} true if form is dirty; false otherwise.
     */
    function isDirty() {
      return $ctrl.form && $ctrl.form.$dirty;
    }

    function customParam(subAction) {
      return {
        'action-name': $ctrl.actionName,
        'sub-action-name' : subAction
      };
    }
  }
}(window.angular));
