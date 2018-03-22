module.exports = (function () {
  'use strict';

  SearchFormController.$inject = ['$scope', '$document', 'entityModel', 'customService'];

  return {
    transclude: true,
    require: {
      globalSearch: '^^',
      queryList: '^^'
    },
    template: require('./search-form.template.html'),
    bindings: {
      actionName: '@',
      entityName: '@'
    },
    controller: SearchFormController
  };

  /**
   * @param {$scope} $scope service.
   * @param {$document} $document service angular.
   * @param {entityModel} entityModel
   * @param {customService} customService
   * @constructor
   */
  function SearchFormController($scope, $document, entityModel, customService) {
    var $ctrl = this;
    var unregisterWatcher;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;

    $ctrl.reset = reset;
    $ctrl.launchSearch = launchSearch;

    function onInit() {
      $ctrl.edit = true;
      $ctrl.entity = entityModel.entity($ctrl.entityName);
      $ctrl.action = $ctrl.entity.getAction($ctrl.actionName);
      var paramsDataCustom = {
        action: $ctrl.action.name.front
      };
      $ctrl.promiseData = customService.get('load-data')
      ($ctrl.entityName, 'search-form', $ctrl.queryList.criteriaData, paramsDataCustom);
      $ctrl.promiseData.then(
        function(data) {
          $ctrl.data = data;
          if (!$ctrl.queryList.restored) {
            launchSearch();
          }
        }
      );
      unregisterWatcher = $scope.$watch(getDisplaySearchPnl, function(newValue, oldValue) {
        if (!newValue && oldValue) {
          clear();
        }
      });
      $document.on("keydown", onKeydown);
    }

    function getDisplaySearchPnl() {
      return $ctrl.globalSearch.displaySearchPnl;
    }

    function onDestroy() {
      if (unregisterWatcher) {
        unregisterWatcher();
      }
      $document.off("keydown");
      delete $ctrl.entity;
      delete $ctrl.action;
      delete $ctrl.data;
    }

    function clear() {
      Object.keys($ctrl.data).forEach(function(key) {
        delete $ctrl.data[key];
      });
    }

    function reset() {
      clear();
      launchSearch();
      $scope.$broadcast('search-form.reset');
    }

    function launchSearch() {
      $ctrl.queryList.search('', $ctrl.data);
    }
    
    /**
     * Function for the keydown event, behavior depends on the key :
     *   - Enter : launch the search
     *   - Escape : reset the form
     * 
     * @params {KeyboardEvent} event the keyboard event
     */
    function onKeydown(event) {
      var handled = false;
      if (event.key === "Enter" && !event.shiftKey && event.target.tagName !== "TEXTAREA"
          && event.target.tagName !== "BUTTON" && event.target.tagName !== "A") {
        // Enter key pressed
        /** 
         * Condition to validate the form:
         * - it's the Enter key
         * - it's not the shift + enter key
         * - the event doesn't come from a <textarea/> (because the enter key is for a newLine) 
         * or from a <button/> (to not validate the form twice) 
         * or from a link <a/> (because it might validate and open the link)
         */
        launchSearch();
        handled = true;
      } else if (event.key === "Escape") {
        // Escape key pressed
        reset();
        handled = true;
      }

      if (handled) {
        event.stopPropagation();
        event.preventDefault();
      }
    }
  }
}(window.angular));
