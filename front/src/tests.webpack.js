require('angular');
require('angular-mocks/angular-mocks');
require('angular-animate');
require('angular-ui-bootstrap');
require('angular-resource');
require('angular-route');
require('angular-sanitize');
require('angular-translate');
require('cmelo-angular-sticky');
require('ng-file-upload');
require('./app/app.module.js');

var testsContext = require.context('.', true, /\.spec\.js$/);
testsContext.keys().forEach(testsContext);
