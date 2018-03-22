module.exports = (function () {
  'use strict';

  InnerTemplateController.$inject = ['restService', 'contextService', 'entityModel', '$scope'];

  require('./inner-template.less');

  return {
    controller: InnerTemplateController,
    require: {
      varForm: '?^^varForm',
      innerTemplate: '?^^innerTemplate'
    },
    template: require('./inner-template.template.html'),
    bindings: {
      linkName: '@',
      entityName: '@',
      classOffset: '@?',
      classInput: '@?'
    },
    transclude: true
  };

  function InnerTemplateController(restService, contextService, entityModel, $scope) {
    var $ctrl = this;
    var onlinkUpdate, onLinkChanged, onDataChanged;
    var context = contextService.getCurrent();
    if (!context) {
      return;
    }

    $ctrl.$onInit = init;
    $ctrl.$onDestroy = destroy;

    // Implementation

    function init() {
      $ctrl.id = context.getComponentId($ctrl.linkName);
      $ctrl.entity = entityModel.entity($ctrl.entityName);
      $ctrl.link = $ctrl.entity.getLink($ctrl.linkName);
      $ctrl.linkEntity = $ctrl.link.dstEntity;

      if ($ctrl.innerTemplate) {
        $ctrl.parentId = $ctrl.innerTemplate.id;
      } else {
        $ctrl.parentId = $ctrl.varForm.id;
      }

      onLinkChanged = $scope.$on('link-changed-' + $ctrl.parentId, function (event, data) {
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
          loadAfterEvent();
        }
      });

      onDataChanged = $scope.$on('data-changed-' + $ctrl.parentId, loadAfterEvent);

      function loadAfterEvent() {
        /**
         * Broadcast a message to all children to give theirs information entity have changed.
         * In order to update other inner-template
         */
        load().then(function (dataLoaded) {
          $scope.$broadcast('data-changed-' + $ctrl.id, {
            data: dataLoaded,
            eventData: {
              idParent: $ctrl.parentId,
              idComponent: $ctrl.id
            }
          });
        });
      }

      /**
       * When a children emit a change on to foreign key, send at all children the new linked entity
       */
      onlinkUpdate = $scope.$on('link-updated-' + $ctrl.id, function (event, eventData) {
        $scope.$broadcast('link-changed-' + $ctrl.id, {
          data: $ctrl.data,
          eventData: eventData
        });
      });

      load();
    }

    function load() {
      var parentForm = $ctrl.innerTemplate || $ctrl.varForm;
      var superDataPromise = parentForm.promiseData;
      var action = context.action;
      return ($ctrl.promiseData = superDataPromise.then(function (superData) {
        var foreignKey = getForeignKey(superData);
        if (!foreignKey) {
          $ctrl.data = {};
          return {};
        }
        var dataPromise = context.getCache($ctrl.linkEntity, foreignKey);
        if (!dataPromise) {
          dataPromise = restService.backRef(
            $ctrl.linkEntity.name.front,
            $ctrl.linkEntity.getStringPrimaryKey(foreignKey),
            action.name.back,
            $ctrl.link.name.back,
            action.entity.name.back
          ).then(function (response) {
            return response.data;
          });
          context.setCache($ctrl.linkEntity, foreignKey, dataPromise);
        }
        dataPromise.then(function (data) {
          $ctrl.data = data;
        });
        return dataPromise;
      }));
    }

    function getForeignKey(entityForm) {
      var foreignKeyNull = false;
      var foreignKey = [];
      Object.keys($ctrl.link.fk).forEach(function (element) {
        foreignKey.push(entityForm[element]);
        if (entityForm[element] === undefined || entityForm[element] === null) {
          foreignKeyNull = true;
        }
      });
      if (foreignKeyNull) {
        return null;
      }
      return foreignKey;
    }

    function destroy() {
      if (onlinkUpdate) {
        onlinkUpdate();
      }
      if (onDataChanged) {
        onDataChanged();
      }
      if (onLinkChanged) {
        onLinkChanged();
      }
    }
  }
}());
