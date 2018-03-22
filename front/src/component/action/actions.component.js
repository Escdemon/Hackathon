module.exports = (function (ng) {
  'use strict';

  ActionsController.$inject = ['entityModel', 'contextService', 'customService', '$q'];

  require('./actions.less');

  return {
    controller: ActionsController,
    template: require('./actions.template.html'),
    bindings: {
      parentId: '<',
      actions: '<?',
      actionNames: '<?',
      entityName: '@',
      addingCssAction: '<?',
      css: '<?',
      cssGroup: '<?',
      linkName: '<?',
      queryName: '<?',
      rows: '<?',
      row: '<?',
      withoutIcon: '<?',
      withoutLabel: '<?',
      maxDisplay: '<?',
      isProtected: '<?',
      minimal: '<?',
      moreIcon: '<?',
      moreClass: '<?'
    }
  };

  function ActionsController(entityModel, contextService, customService, $q) {
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.additionalActions = [];

    function onInit() {
      $ctrl.moreIcon = $ctrl.moreIcon || 'glyphicon glyphicon-option-vertical';
      $ctrl.moreClass = $ctrl.moreClass || 'btn-round-toy';
      if (!$ctrl.rows && $ctrl.row) {
        $ctrl.rows = [$ctrl.row];
      }
      var params = {queryName: $ctrl.queryName, linkName: $ctrl.linkName, maxDisplay: $ctrl.maxDisplay};
      $ctrl.maxDisplay = customService.get('max-display')($ctrl.entityName, 'actions', $ctrl.rows, params);
      $ctrl.perform = perform;
      $ctrl.css = $ctrl.css || '';
      if ($ctrl.actions) {
        if ($ctrl.actions.length > 1) {
          $ctrl.additionalActions =
            $ctrl.actions.slice(Math.min($ctrl.maxDisplay, $ctrl.actions.length), $ctrl.actions.length);
          $ctrl.actions = $ctrl.actions.slice(0, Math.min($ctrl.maxDisplay, $ctrl.actions.length));
        }
      } else {
        var entity = entityModel.entity($ctrl.entityName);
        $ctrl.actions = [];
        $ctrl.actionNames.forEach(function (actionName) {
          var action = entity.getAction(actionName);
          if (action.isDisplayable(
              contextService.getCurrent().action,
              $ctrl.isProtected,
              true,
              contextService.getCurrent().action.hasLinkProcess()
            )
          ) {
            if (ng.isUndefined($ctrl.maxDisplay) || $ctrl.actions.length < $ctrl.maxDisplay) {
              $ctrl.actions.push(action);
            } else {
              $ctrl.additionalActions.push(action);
            }
          }
        });
        if ($ctrl.additionalActions.length === 1 && 0 !== $ctrl.maxDisplay) {
          $ctrl.actions.push($ctrl.additionalActions[0]);
          $ctrl.additionalActions = null;
        }
      }
      // Prevent propagation on click
      $ctrl.stopEvent = function($event) {
          $event.stopPropagation();
        };
    }

    function perform(action) {
      // Action input sans lignes selectionées => ne rien faire
      if (!action.isAttach() && action.hasInput() && !$ctrl.rows.length) {
        return ;
      }
      var currentContext = contextService.getCurrent();
      var pks = $ctrl.rows.reduce(function (pks, row) {
        if (row && row.primaryKey) {
          pks.push(row.primaryKey);
        }
        return pks;
      }, []);
      var functions = {};
      var options = {};
      if ($ctrl.linkName) {
        var currentData = currentContext.getData();
        var link = action.entity.getLink($ctrl.linkName);
        if (!link.associative) {
          var entity = link.srcEntity;
          functions.afterBackLoad = function () {
            var nextContext = contextService.getCurrent();
            var dataIntoNextAction = nextContext.getData();
            link.setLinkedEntity(currentData, dataIntoNextAction);
            nextContext.setCache(entity, entity.extractPrimaryKey(currentData), $q.when(currentData));
          };
        }
        options.link = link;
        options.query = $ctrl.queryName;
      }
      action.redirectToPageAction(pks, functions, options);
    }
  }
}(window.angular));
