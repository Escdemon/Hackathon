module.exports = (function () {
  'use strict';

  VarLabelController.$inject = ['contextService'];

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./var-label.template.html'),
    controller: VarLabelController,
    styles: [require('./var-label.less')],
    bindings: {
      classLabel: '@',
      classOffset: '@',
      label: '@',
      name: '@',
      nameParent: '<',
      isProtected: '<',
      mandatory: '<'
    }
  };

  function VarLabelController(contextService) {
    var $ctrl = this;
    $ctrl.editForm = editForm;
    $ctrl.displayingLabel = displayingLabel;
    $ctrl.$onInit = init;

    // Implementations

    function init() {
      $ctrl.displayLabel = !!$ctrl.label;
      $ctrl.type = $ctrl.type || 'text';
      // init attribute "for"
      if ($ctrl.name) {
        $ctrl.tplName = $ctrl.name;
        $ctrl.id = $ctrl.tplName + '-label';
      }
      $ctrl.disableInput = $ctrl.isProtected;
    }

    function editForm() {
      return !contextService.getCurrent().action.readonly;
    }

    function displayingLabel() {
      return !!$ctrl.label;
    }
  }
}());
