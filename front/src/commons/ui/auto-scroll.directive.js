module.exports = (function (doc) {
  'use strict';

  AutoScrollController.$inject = ['$scope', '$timeout'];
  return autoScrollDirective;

  function autoScrollDirective() {
    return {
      restrict: 'A',
      controllerAs: '$ctrl',
      controller: AutoScrollController,
      scope: {
        selector: '@autoScroll'
      }
    };
  }

  function AutoScrollController($scope, $timeout) {
    var $ctrl = this;
    var locationChangeStart, locationChangeSuccess;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.selector = $scope.selector || '[data-ng-view]';
    // Think about saving the scroll position into the current context.
    $ctrl.scrollPos = {};

    function onInit() {
      locationChangeStart = $scope.$on('$locationChangeStart', function (event, newUrl, oldUrl) {
        if (oldUrl) {
          var mainSection = getMainSection();
          if (!mainSection) {
            return;
          }
          $ctrl.scrollPos[oldUrl] = {
            x: mainSection.scrollLeft,
            y: mainSection.scrollTop
          };
        }
      });

      locationChangeSuccess = $scope.$on('$locationChangeSuccess', function (event, newUrl) {
        $timeout(function () {
          var scrollPos = $ctrl.scrollPos[newUrl] || {x: 0, y: 0};
          var mainSection = getMainSection();
          if (!mainSection) {
            return;
          }
          mainSection.scrollLeft = scrollPos.x;
          mainSection.scrollTop = scrollPos.y;
        }, 200);
        locationChangeSuccess();
      });
    }

    function onDestroy() {
      if (locationChangeStart) {
        locationChangeStart();
      }
    }

    /**
     * @returns The main section of this application.
     */
    function getMainSection() {
      return doc.querySelector($ctrl.selector);
    }
  }

})(window.document);
