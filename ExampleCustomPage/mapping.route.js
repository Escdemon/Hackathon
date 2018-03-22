module.exports = (function() {
    'use strict';

    mappingRoute.$inject = ['$routeProvider'];
    return mappingRoute;

    function mappingRoute($routeProvider) {
        $routeProvider.when('/mapping', {
            template: '<mapping></mapping>'
        })
    }
}());
