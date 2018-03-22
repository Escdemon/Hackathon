module.exports = (function(ng) {
  'use strict';

  GlobalSearchController.$inject = ['$scope', 'customService', '$element'];

  return {
    template: require('./global-search.template.html'),
    styles: [require('./global-search.less')],
    transclude: {
      'gs-search-tpl': 'div',
      'actions': '?actionsList'
    },
    require: {
      queryList: '^'
    },
    bindings: {
      criteria: '<',
      rowCount: '<',
      rowsByPage: '<',
      currentPage: '<',
      totalCount: '<',
      onChange: '<',
      entityName: '<',
      hasSearchTpl: '<?'
    },
    controller: GlobalSearchController
  };

  function GlobalSearchController($scope, customService, $element) {
    var $ctrl = this;
    var cleanWatchCriteria;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.displaySearchPnl = false;

    function onInit() {
      $ctrl.currentPage = $ctrl.currentPage || 1;
      $ctrl.displaySearchPnl = false;
      $ctrl.searchCriteria = $ctrl.criteria;
      $ctrl.reset = reset;
      $ctrl.toggleSearchPnl = toggleSearchPnl;
      $ctrl.hideSearchPnl = hideSearchPnl;
      $ctrl.search = search;
      $ctrl.live = customService.get('live-search')(
        $ctrl.entityName,
        'global-search',
        {},
        {}
      );

      if ($ctrl.live) {
        cleanWatchCriteria = $scope.$watch('$ctrl.searchCriteria', function(
          newValue,
          oldValue
        ) {
          if (newValue !== oldValue && ng.isFunction($ctrl.onChange)) {
            $ctrl.search();
          }
        });
      }
    }

    function onDestroy() {
      if (cleanWatchCriteria) {
        cleanWatchCriteria();
      }
    }

    function search() {
      $ctrl.onChange($ctrl.searchCriteria);
    }

    function reset() {
      $ctrl.searchCriteria = '';
    }

    function toggleSearchPnl() {
      $ctrl.displaySearchPnl = !$ctrl.displaySearchPnl;
      if ($ctrl.displaySearchPnl) {
        reset();
        disableDirtyState();
      }
    }

    function hideSearchPnl() {
      if ($ctrl.displaySearchPnl) {
        $ctrl.displaySearchPnl = false;
      }
    }

    /**
     * Disables dirty state management onto this component's children.
     *  - It retrieves the chidren which owns a ngModel.
     *  - Then, it changes the function $setDirty onto the ngModelController by a no-operation function.
     *
     * Children will always be seen as pristine and they will not update the parent form's dirty state.
     */
    function disableDirtyState() {
      var ngModels = $element[0].querySelectorAll('[data-ng-model]');
      ngModels.forEach(function(child) {
        var ngElement = ng.element(child);
        var ngModelController = ngElement.data().$ngModelController;
        ngModelController.$setDirty = ng.noop;
      });
    }
  }
}(window.angular));
