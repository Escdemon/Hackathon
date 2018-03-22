module.exports = (function (ng) {
  'use strict';

  QueryListController.$inject = ['$element', '$timeout', 'contextService', 'customService', 'entityModel', 'restService'];

  return queryListDirective;

  function queryListDirective() {
    return {
      restrict: 'E',
      controllerAs: '$ctrl',
      bindToController: true,
      require: {
        varFormCtrl: '?^^varForm',
        innerTemplate: '?^^innerTemplate'
      },
      template: queryListTemplate,
      styles: [require('./query-list.less')],
      scope: {
        label: '@',
        entityName: '@',
        actionName: '@',
        linkName: '@',
        queryName: '@',
        searchQueryName: '@?',
        globalSearch: '<?',
        isProtected: '<?',
        selectedRows: '<?',
        hasSearchTpl: '<?'
      },
      controller: QueryListController,
      compile: queryListCompile
    };
  }

  /**
   * Creates a new controller.
   *
   * @param {$element} $element DOM element wrapped into a jqLite object.
   * @param {$timeout} $timeout Service angular for timeout.
   * @param {contextService} contextService Service to manage the application context.
   * @param {customService} customService Service to register custom methods.
   * @param {entityModel} entityModel Service to manage entity.
   * @param {restService} restService Service to call REST services.
   */
  function QueryListController($element, $timeout, contextService, customService, entityModel, restService) {
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;

    $ctrl.rows = [];
    $ctrl.rowsByPage = 50;
    $ctrl.allSelected = false;
    $ctrl.columns = [];
    $ctrl.isLoading = true;

    $ctrl.search = search;
    $ctrl.sortBy = sortBy;

    $ctrl.toggleRow = toggleRow;
    $ctrl.toggleAllRows = toggleAllRows;

    $ctrl.addColumn = addColumn;

    function onInit() {
      $ctrl.globalSearch = ng.isDefined($ctrl.globalSearch) ? $ctrl.globalSearch : true;
      $ctrl.entity = entityModel.entity($ctrl.entityName);
      $ctrl.action = $ctrl.entity.getAction($ctrl.actionName);
      $ctrl.firstLoaded = false;

      $ctrl.ctx = contextService.getCurrent();
      if (!$ctrl.ctx) {
        return;
      }
      $ctrl.id = $ctrl.ctx.getComponentId($ctrl.actionName || $ctrl.queryName || $ctrl.linkName);
      restoreState();
      manageDisplay();
    }

    function manageDisplay() {
      var currentAction = $ctrl.ctx.action;
      $ctrl.display = !$ctrl.varFormCtrl || !currentAction.isCreate() || currentAction.hasLinkProcess();
      if ($ctrl.display) {
        $ctrl.readonly = currentAction.readOnly || customService.get('list-readonly')
          ($ctrl.entity.name.front, 'query-list', {}, {queryName: $ctrl.query});
        $timeout(function () {
          if (!$ctrl.firstLoaded) {
            // Search if search-form not launch a search.
            search();
          }
          disableDirtyState();
          contextService.loaded();
        });
      }
    }

    /**
     * Saves this component's state (criteria, current page, selected rows and sort).
     */
    function onDestroy() {
      if ($ctrl.ctx) {
        var state = {
          criteria: $ctrl.criteria,
          criteriaData: $ctrl.criteriaData,
          currentPage: $ctrl.currentPage,
          selectedRows: $ctrl.selectedRows,
          sort: $ctrl.sort,
          // RowCount needs to be saved to be able to restore uib-pagination
          totalRowCount: $ctrl.totalRowCount
        };
        $ctrl.ctx.saveComponentState($ctrl.id, state);
      }
      delete $ctrl.columns;
      delete $ctrl.rows;
    }

    /**
     * Restores this component's state (criteria, current page, selected rows and sort).
     */
    function restoreState() {
      var state = $ctrl.ctx.getComponentState($ctrl.id);
      if (ng.isDefined(state.currentPage)) {
        $ctrl.criteria = state.criteria;
        $ctrl.currentPage = state.currentPage || 1;
        $ctrl.selectedRows = state.selectedRows || [];
        $ctrl.sort = state.sort;
        $ctrl.criteriaData = state.criteriaData || {};
        $ctrl.totalRowCount = state.totalRowCount;
        $ctrl.restored = true;
      } else {
        // No saved state, init defaults
        $ctrl.criteria = '';
        $ctrl.currentPage = 1;
        $ctrl.selectedRows = [];
        $ctrl.criteriaData = {};
        $ctrl.sort = undefined;
      }
    }

    function search(criteria, entity) {
      $ctrl.firstLoaded = true;
      $ctrl.criteria = criteria !== undefined ? criteria : $ctrl.criteria;
      entity = entity !== undefined ? entity : $ctrl.criteriaData;
      var options = createQueryOptions($ctrl.criteria);
      if ($ctrl.action) {
        restService.list($ctrl.action, entity, options).then(extractData);
      } else {
        var ctrlForm = $ctrl.innerTemplate || $ctrl.varFormCtrl;
        ctrlForm.promiseData.then(function (data) {
          if ($ctrl.linkName) {
            var link = $ctrl.entity.getLink($ctrl.linkName);
            restService.listLink(contextService.getCurrent().action, $ctrl.queryName, link, data.primaryKey, options)
              .then(extractData);
          } else {
            restService.query($ctrl.entity, $ctrl.queryName, options).then(extractData);
          }
        });
      }
    }

    function getData(data) {
      $ctrl.totalRowCount = data.resultSetCount;

      if (data.results.length > $ctrl.rowsByPage) {
        $ctrl.rows = data.results.slice(0, $ctrl.rowsByPage);
      } else {
        $ctrl.rows = data.results;
      }
      // Component's state restored, let's select some rows.
      if ($ctrl.selectedRows.length && $ctrl.rows.length) {
        var selectedRows = ng.copy($ctrl.selectedRows);
        $ctrl.selectedRows = [];

        selectedRows.forEach(function (r) {
          $ctrl.rows.forEach(function (row) {
            if (row.primaryKey === r.primaryKey) {
              $ctrl.toggleRow(row);
            }
          });
        });
      }
      $ctrl.isLoading = false;
    }

    function extractData(data) {
      getData(data.data);
    }

    function createQueryOptions() {
      var options = {
        'search': $ctrl.criteria,
        'start-index': ($ctrl.currentPage - 1) * $ctrl.rowsByPage,
        'length': $ctrl.rowsByPage
      };

      if ($ctrl.sort && $ctrl.sort.column && $ctrl.sort.direction) {
        options['order-by'] = $ctrl.sort.column;
        options['order-direction'] = $ctrl.sort.direction;
      }
      return options;
    }

    function sortBy(col) {
      if (!col.sortable) {
        return;
      }

      var columnName = col.name;
      var oldColumnName = $ctrl.sort ? $ctrl.sort.column : undefined;

      if (oldColumnName !== undefined) {
        var oldColumn = $ctrl.columns.find(function (column) {
          return column !== undefined && column.name === oldColumnName;
        });
        oldColumn.clearSortCssClass();
      }

      if (oldColumnName !== columnName) {
        // First sort onto this column, order is ascending.
        $ctrl.sort = {
          column: columnName,
          direction: 'asc'
        };
        col.changeSortCssClass($ctrl.sort.direction);

      } else if ($ctrl.sort.direction === 'desc') {
        // Restores the default query order (asc / desc / default).
        $ctrl.sort = undefined;
        col.clearSortCssClass();

      } else {
        // Sort was ascending, now it is descending.
        $ctrl.sort.direction = 'desc';
        col.changeSortCssClass($ctrl.sort.direction);
      }

      $ctrl.search($ctrl.criteria);
    }

    function toggleRow(row) {
      row.selected = !row.selected;

      if (row.selected) {
        $ctrl.selectedRows.push(row);
      } else {
        var index = $ctrl.selectedRows.indexOf(row);
        if (index > -1) {
          $ctrl.selectedRows.splice(index, 1);
        }
      }
      $ctrl.allSelected = $ctrl.selectedRows.length === $ctrl.rows.length;
      fireSelectionChanged();
    }

    function toggleAllRows(selected) {
      $ctrl.allSelected = (selected !== undefined) ? selected : !$ctrl.allSelected;
      $ctrl.selectedRows.splice(0, $ctrl.selectedRows.length);

      $ctrl.rows.forEach(function (row) {
        row.selected = $ctrl.allSelected;

        if ($ctrl.allSelected) {
          $ctrl.selectedRows.push(row);
        }
      });
      fireSelectionChanged();
    }

    function fireSelectionChanged() {
      contextService.getCurrent().selectedRows = $ctrl.selectedRows;
    }

    /**
     * Adds the column to display.
     */
    function addColumn(column) {
      $ctrl.columns.push(column);

      // Component's state restored, let's display the sort feedback.
      if ($ctrl.sort && $ctrl.sort.column === column.name) {
        column.changeSortCssClass($ctrl.sort.direction);
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

  function queryListTemplate(element) {
    // Save given element
    var customRowTemplates = [];
    var renders = element.find('renderer');
    for (var i = 0; i < renders.length; i++) {
      // Extract now html for IE.
      customRowTemplates.push(renders[i].innerHTML);
    }
    element.data('custom-row-template', customRowTemplates);
    element.data('column', element.find('column'));
    element.data('actions-list', element.find('actions-list'));
    element.data('search-form', ng.element(element.find('search-form')[0]));
    element.data('search-tpl', ng.element(element.data('search-form').children()[0]));
    return require('./query-list.template.html');
  }

  /**
   *
   * @param {Object} element element given by angular.
   */
  function queryListCompile(element) {
    // Add row
    var globalSearch = ng.element(element.find('global-search')[0]);
    globalSearch.append(element.data('actions-list'));
    var divGs = ng.element(globalSearch.find('div')[0]);
    divGs.append(element.data('search-form'));
    element.data('search-form').append(element.data('search-tpl'));

    var templateRows = element.data('custom-row-template');
    var tableElement = ng.element(element.find('table')[0]);
    var tBodyElement = ng.element(tableElement.find('tbody')[0]);
    var tr = ng.element(tBodyElement.find('tr')[0]);
    var i = 0;
    for (; i < templateRows.length; i++) {
      var tdElement = ng.element(document.createElement('td'));
      tdElement.attr('data-ng-click', '::$ctrl.toggleRow(row)');
      tdElement.attr('data-ng-class', '::$ctrl.columns[' + i + '].cssClass(row)');
      tdElement.attr('data-ng-if', '::$ctrl.columns[' + i + '].visible');
      tdElement.html(templateRows[i]);
      tr.append(tdElement);
    }

    var divElements = element.find('div');
    var intoAppendColumns;
    for (i = 0; i < divElements.length && !intoAppendColumns; i++) {
      var divElement = ng.element(divElements[i]);
      if (!intoAppendColumns && divElement.hasClass('table-responsive')) {
        intoAppendColumns = divElement;
      }
    }

    var columns = element.data('column');
    for (i = 0; i < columns.length; i++) {
      intoAppendColumns.append(ng.element(columns[i]));
    }

    // Clean
    element.data('custom-row-template', null);
    element.data('column', null);
    element.data('actions-list', null);
    element.data('search-tpl', null);
  }
}(window.angular));
