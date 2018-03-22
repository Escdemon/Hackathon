module.exports = (function () {
  'use strict';

  runBreadCrumbFunction.$inject = ['$templateCache'];

  return runBreadCrumbFunction;

  function runBreadCrumbFunction($templateCache) {
    $templateCache.put('bread-crumb/tooltip-hidden.template.html', require('./bread-crumb-hidden.template.html'));
  }
}());
