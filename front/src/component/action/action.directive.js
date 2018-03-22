module.exports = (function(ng) {
  'use strict';

  return directive;

  function directive() {

    ActionController.$inject = ['contextService', 'customService'];

    return {
      require: {
        actionsCtrl: '?^actions'
      },
      restrict: 'E',
      replace: true,
      template: require('./action.template.html'),
      bindToController: true,
      controllerAs: '$ctrl',
      scope: {
        action: '<',
        rows: '<',
        minimal: '<?',
        execute: '<'
      },
      controller: ActionController
    };

    function ActionController(contextService, customService) {
      var $ctrl = this;
      $ctrl.$onInit = onInit;

      $ctrl.isEnabled = isEnabled;

      function onInit() {
        var css, icon, label, tooltip;
        if ($ctrl.actionsCtrl) {
          css = $ctrl.actionsCtrl.addingCssAction;
          icon = $ctrl.actionsCtrl.withoutIcon ? '' : $ctrl.action.icon;
          label = $ctrl.actionsCtrl.withoutLabel ? '' : $ctrl.action.label;
          tooltip = $ctrl.actionsCtrl.withoutLabel ? $ctrl.action.label : $ctrl.action.title;
        } else {
          icon = $ctrl.action.icon;
          label = $ctrl.action.label;
          tooltip = $ctrl.action.title;
        }
        if ($ctrl.minimal) {
          icon = $ctrl.action.icon;
          tooltip = tooltip || label;
          label = null;
        }
        css = css || 'btn-default btn-round-toy';
        css +=  ' process-' + $ctrl.action.process;
        css +=  ' persistence-' + $ctrl.action.persistence;
        css +=  ' input-' + $ctrl.action.input;
        $ctrl.addingClass +=  ' flux-' + $ctrl.action.flux;

        var currentAction = contextService.getCurrent().action;
        var currentEntity = currentAction.entity.name.front;
        var params = {
          currentActionName: currentAction.name.front,
          executeAction: $ctrl.action.name.front,
          executeActionEntity: $ctrl.action.entity.name.front,
          minimal: $ctrl.minimal
        };
        $ctrl.addingClass = customService.get('css')
          (currentEntity, 'action', $ctrl.rows, ng.extend({css: css}, params));
        $ctrl.icon = customService.get('icon')
          (currentEntity, 'action', $ctrl.rows, ng.extend({icon: icon}, params));
        $ctrl.label = customService.get('label')
          (currentEntity, 'action', $ctrl.rows, ng.extend({label: label}, params));
        $ctrl.tooltip = customService.get('tooltip')
          (currentEntity, 'action', $ctrl.rows, ng.extend({tooltip: tooltip}, params));
      }

      /**
       * To know if the action button is enabled depending on the type of action and the inputs of this action.
       * @returns {Boolean} true if the action button is enabled.
       */
      function isEnabled() {
        if ($ctrl.action.hasInput()) {
          $ctrl.ctx = contextService.getCurrent();
          if ($ctrl.action.isAttach() && !$ctrl.action.hasCustomProcess() && $ctrl.ctx.options.inputOne) { // inputOne indicate if the action is expecting an unique input
        	  																							   // "true" if it's a simple link else "undefined"
              return $ctrl.rows.length == 1;
          }
          return $ctrl.rows.length > 0;
        }
        return true;
      }
    }
  }
}(window.angular));
