module.exports = (function(angular) {
    'use strict';

    return angular
        .module('mapping', [
            'app.core',
            require('../commons/commons.module.js'),
            'ui.bootstrap'
        ])
        .component('mapping', require('./mapping.component.js'))
        .config(require('./mapping.route.js'))
        .name;

}(window.angular));