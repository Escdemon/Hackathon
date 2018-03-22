module.exports = (function(document) {
  'use strict';

  UpdateTitleDirective.$inject = ['$rootScope', '$translate'];
  return UpdateTitleDirective;

  function UpdateTitleDirective($rootScope, $translate) {
    return {
      link: function(scope) {
        var cleanWatch = $rootScope.$watch('title', update);
        var cleanDestroy = scope.$on('$destroy', function() {
          cleanWatch();
          cleanDestroy();
        });
      }
    };

    function update(title) {
      if (typeof title === 'string') {
        $translate(title).then(function(label) {
          document.querySelector('title').innerText = label.toString();
        });
      } else if (typeof title === 'object') {
        document.querySelector('title').innerText = title.toString();
      }
    }

  }

}(window.document));
