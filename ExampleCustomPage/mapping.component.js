module.exports = (function () { 
    'use strict';

    MonController.$inject = ['$scope'];

return {
    controller: MonController,
    template: require('./mapping.template.html'),
    bindings: {}
};

function MonController($scope) {
    return;
};

}());

