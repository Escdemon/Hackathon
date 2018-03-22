module.exports = (function () {
  'use strict';

  Controller.$inject = [
    '$scope', 'contextService', 'customService'
  ];

  return {
    require: {
      innerTemplateCtrl: '?^^innerTemplate',
      formCtrl: '?^^varForm',
      searchCtrl: '?^^searchForm'
    },
    controller: Controller,
    template: require('./localisation.template.html'),
    bindings: {
      isProtected: '<',
      name: '@'
    }
  };

  function Controller($scope, contextService, customService) {
    var $ctrl = this;
    var onDestroyer;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;

    $ctrl.showButton = false;

    // Implementation

    function onInit() {
      var ctx = contextService.getCurrent();
      if (!ctx) {
        return;
      }
      if ($ctrl.name) {
        $ctrl.id = ctx.getComponentId($ctrl.name);
      }
      var parentComponent = $ctrl.innerTemplateCtrl || $ctrl.searchCtrl ||  $ctrl.formCtrl;
      var promiseData = parentComponent.promiseData;
      promiseData.then(saveIntoData);
      onDestroyer = $scope.$on('data-changed-' + parentComponent.id, function (event, data) {
        parentComponent.promiseData.then(saveIntoData);
      });

      $ctrl.action = ctx.action;
      $ctrl.entity = $ctrl.action.entity;
      $ctrl.actions = {};
      $ctrl.show = {};
      $ctrl.addingClass = {};
      $ctrl.mandatory = {};
      $ctrl.protect = {};
      var template = [
        {
          'type': 'simple-link',
          'name': 'localisation-r-balise',
          'mandatory': true
        }
      ];

      template.forEach(function (template) {
        var params = {template: template, action: $ctrl.action};
        var getVisible = customService.get('visible')('localisation', 'template', $ctrl.data, params);
        if (getVisible){
          getVisible.then(function(visible) {
            $ctrl.show[template.name] = visible;
          });
        } else {
          console.error('The custom function visible for template can\'t return undefined.');
        }
        var getCss = customService.get('css')('localisation', 'template', $ctrl.data, params);
        if (getCss){
          getCss.then(function(css) {
            $ctrl.addingClass[template.name] = css;
          });
        } else {
          console.error('The custom function css for template can\'t return undefined.');
        }
        var getMandatory = customService.get('mandatory')('localisation', 'template', $ctrl.data, params);
        if (getMandatory){
          getMandatory.then(function(mandatory) {
            $ctrl.mandatory[template.name] = mandatory;
          });
        } else {
          console.error('The custom function mandatory for template can\'t return undefined.');
        }
        if ($ctrl.isProtected) {
          $ctrl.protect[template.name] = true;
        } else {
          var getProtected = customService.get('isProtected')('localisation', 'template', $ctrl.data, {
            template: template, action: $ctrl.action
          });
          if (getProtected){
            getProtected.then(function(isProtected) {
              $ctrl.protect[template.name] = isProtected;
            });
          } else {
            console.error('The custom function isProtected for template can\'t return undefined.');
          }
        }
      });
      $ctrl['key-elements'] = {
        'localisation-r-balise': {
          'baliseId': 'id'
        }
      };

    }
    function onDestroy() {
      if (onDestroyer) {
        onDestroyer();
      }
    }

    function saveIntoData(data) {
      $ctrl.data = data;
    }
  }
}());
