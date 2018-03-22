module.exports = (function () {
  'use strict';

  require('./bread-crumb.less');

  BreadCrumbController.$inject = ['contextService', '$rootScope', 'breadCrumb'];

  return {
    controller: BreadCrumbController,
    template: require('./bread-crumb.template.html')
  };

  /**
   * @param {contextService} contextService context service.
   * @param {$rootScope} $rootScope root scope.
   * @param {breadCrumb} breadCrumb constante of bread crumb.
   * @constructor
   */
  function BreadCrumbController(contextService, $rootScope, breadCrumb) {
    var $ctrl = this;
    var onRouteChangeSuccess;
    var onContextLoadSuccess;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.goto = goTo;
    $ctrl.const = breadCrumb;
    $ctrl.range = range;

    function onInit() {
      loadContextInformation();
      onRouteChangeSuccess = $rootScope.$on('$routeChangeSuccess', loadContextInformation);
      onContextLoadSuccess = $rootScope.$on('context.restore-success', loadContextInformation);
    }

    function onDestroy() {
      if (onRouteChangeSuccess) {
        onRouteChangeSuccess();
      }
      if (onContextLoadSuccess) {
        onContextLoadSuccess();
      }
    }

    function loadContextInformation() {
      $ctrl.current = contextService.getCurrent(true);
      $ctrl.contexts = [];
      var ctx = $ctrl.current;
      while (ctx) {
        $ctrl.contexts.unshift(ctx);
        ctx = ctx.previous;
      }
    }

    function goTo(ctx) {
      contextService.goTo(ctx);
    }

    function range(min, max, step) {
      step = step || 1;
      var input = [];
      for (var i = min; i <= max; i += step) {
        input.push(i);
      }
      return input;
    }
  }

}());
