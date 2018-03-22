module.exports = (function () {
  'use strict';

  return function () {
    return {
      link: function (scope, element, attributes) {
        var cleanWatch = scope.$watch(attributes.autoFocus, function (value) {
          if (value === true) {
            element[0].focus();
            scope[attributes.autoFocus] = false;
          }
        });
        var cleanDestroy = scope.$on('$destroy', function() {
          cleanWatch();
          cleanDestroy();
        });
      }
    };
  };

}());
