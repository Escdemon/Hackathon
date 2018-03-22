module.exports = (function() {
  'use strict';

  routes.$inject = ['$routeProvider'];
  function routes ($routeProvider) {
    $routeProvider
      .when('/login', { template: '<login></login>' })
      .when('/logout', {resolve: { redirect: logout }});
  }

  logout.$inject = ['$location', 'loginService', 'messageService'];
  function logout($location, loginService, messageService) {
    // resolve is diverted to do a new redirection.
    loginService.logOut();
    messageService.display({display:'login.logout-success', level:'success', duration: 2000});
    $location.path('/');
  }

  return routes;
}());
