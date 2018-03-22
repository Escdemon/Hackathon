module.exports = (function () { 
    'use strict';

    MonController.$inject = ['$scope'];

return {
    controller: MonController,
    template: require('./carte.template.html'),
    bindings: {}
};

function MonController($scope) {
    return;
};

}());

