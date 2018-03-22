module.exports = (function(angular) {
    'use strict';

    return angular
        .module('carte', [
            'app.core',
            'ui.bootstrap'
        ])
        .component('carte', require('./carte.component.js'))
        .config(require('./carte.route.js'))
        .name;

}(window.angular));