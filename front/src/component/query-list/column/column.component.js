module.exports = (function(ng) {
  'use strict';

  ColumnController.$inject = ['customService'];

  return {
    bindings: {
      key: '@',
      name: '@',
      sortable: '<?',
      type: '@'
    },
    require: {
      listCtrl: '^queryList'
    },
    controller: ColumnController
  };

  function ColumnController(customService) {
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.changeSortCssClass = changeSortCssClass;
    $ctrl.clearSortCssClass = clearSortCssClass;

    function onInit() {
      var listCtrl = $ctrl.listCtrl;
      var entity = listCtrl.entity;
      var params = {
        columnKey: $ctrl.key,
        queryName: listCtrl.query
      };

      $ctrl.visible = customService.get('column-visible')(entity.name.front, 'query-list', {}, params);

      if ($ctrl.visible) {
        $ctrl.titleKey = customService.get('column-title')(entity.name.front, 'query-list', {}, params);
        /* More sortable columns than computed columns, so they are sortable while the attribute is not specified. */
        $ctrl.sortable = ng.isDefined($ctrl.sortable) ? $ctrl.sortable : true;
        /* String class by default. */
        $ctrl.cssClass = function(row) {
          var addingCss = customService.get('cell-css')(entity.name.front, 'query-list', row, params);
          if (addingCss) {
            addingCss += ' ';
          } else {
            addingCss = '';
          }
          return addingCss + (ng.isDefined($ctrl.type) ? $ctrl.type : 'string');
        };
      }
      listCtrl.addColumn($ctrl);
    }

    function onDestroy() {
      delete $ctrl.cssClass;
    }

    function changeSortCssClass(direction) {
      var cssClass;

      if ($ctrl.type === 'string') {
        cssClass = 'glyphicon-sort-by-alphabet';

      } else if ($ctrl.type === 'numeric') {
        cssClass = 'glyphicon-sort-by-order';

      } else {
        cssClass = 'glyphicon-sort-by-attributes';
      }
      $ctrl.sortCssClass = cssClass + (direction === 'desc' ? '-alt' : '');
    }

    function clearSortCssClass() {
      $ctrl.sortCssClass = '';
    }

  }

}(window.angular));
