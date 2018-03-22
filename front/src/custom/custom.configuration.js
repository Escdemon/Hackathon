module.exports = (function() {
    'use strict';

    actionsConfiguration.$inject = ['customServiceProvider'];

    return actionsConfiguration;

    function actionsConfiguration(customServiceProvider) {
        customServiceProvider.setImplementation('menu-action')('balise', 'action', getOperateur, { });

        function getOperateur($q) {
            return function () {
                return $q.when(['id:::L1']);
            };
          }
          getOperateur.$inject = ['$q'];
    }
} ());