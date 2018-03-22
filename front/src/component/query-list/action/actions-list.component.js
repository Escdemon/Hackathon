module.exports = (function() {
  'use strict';

  ActionListController.$inject = ['contextService'];

  const GROUP_CREATE  = 0, 
        GROUP_LINK    = 1, 
        GROUP_MODIFY  = 2,
        GROUP_CONSULT = 3,
        GROUP_DELETE  = 4,
        GROUP_EXPORT  = 5;

  return {
    require: {
      queryListCtrl: '^queryList'
    },
    template: require('./actions-list.template.html'),
    styles: [require('./actions-list.less')],
    bindings: {
      actions: '<'
    },
    controller: ActionListController
  };

  /**
   * Creates a new controller.
   * @param {contextService} contextService
   */
  function ActionListController(contextService) {
    var $ctrl = this;
    $ctrl.$onInit = onInit;

    function onInit() {
      var groups = [
        {
          css: 'create',
          cssGroup: '',
          cssAction: 'btn-primary btn-toy',
          actions: [],
          moreIcon: 'fa fa-plus',
          moreClass: 'btn-round-toy btn-round-lg-toy'
        },
        {
          css: 'link',
          cssAction: 'btn-default btn-toy',
          actions: [],
          moreIcon: 'fa fa-link',
          moreClass: 'btn-round-toy btn-round-lg-toy'
        },
        {
          css: 'modify',
          cssAction: 'btn-success btn-toy',
          actions: [],
          moreIcon: 'fa fa-pencil',
          moreClass: 'btn-round-toy btn-round-lg-toy'
        },
        {
          css: 'consult',
          cssAction: 'btn-default btn-toy',
          actions: [],
          moreIcon: 'fa fa-eye',
          moreClass: 'btn-round-toy btn-round-lg-toy'
        },
        {
          css: 'delete',
          cssAction: 'btn-danger btn-toy',
          actions: [],
          moreIcon: 'fa fa-trash-o',
          moreClass: 'btn-round-toy btn-round-lg-toy'
        },
        {
          css: 'export',
          cssAction: 'btn-default btn-toy',
          actions: [],
          moreIcon: 'fa fa-upload',
          moreClass: 'btn-round-toy btn-round-lg-toy'
        }
      ];
      $ctrl.groups = $ctrl.actions.reduce(function (groups, strAction) {
        var action = $ctrl.queryListCtrl.entity.getAction(strAction);
        if ($ctrl.displayAction && $ctrl.displayAction.inGroup) {
          if (!$ctrl.displayAction.inGroup(action)) {
            return groups;
          }
        } else if (!action.isDisplayable(
            contextService.getCurrent().action,
            $ctrl.queryListCtrl.isProtected,
            true,
            false,
            $ctrl.queryListCtrl.linkName
         )
        ) {
          return groups;
        }
        if (action.hasNoInput() && action.priority > 99) {
          groups[GROUP_EXPORT].actions.push(action);
        } else if (action.hasNoInput() && action.isCreate()) {
          groups[GROUP_CREATE].actions.push(action);
        } else if (action.hasNoPersistence()) {
          groups[GROUP_CONSULT].actions.push(action);
        } else if (action.hasLinkProcess()) {
          groups[GROUP_LINK].actions.push(action);
        } else if (action.isDelete()) {
          groups[GROUP_DELETE].actions.push(action);
        } else {
          groups[GROUP_MODIFY].actions.push(action);
        }
        return groups;
      }, groups);
    }
  }
}());
