module.exports = (function() {
    'use strict';

    mappingRoute.$inject = ['$routeProvider'];
    return mappingRoute;

    function mappingRoute($routeProvider) {
        $routeProvider.when('/carte', {
            template: '<carte></carte>'
        })
    }
}());
