module.exports = (function () {
  'use strict';

  var toReturn = {
    controller: HomeController,
    template: require('./home.template.html')
  };

  HomeController.$inject = ['titleService', 'loginService', '$location'];

  return toReturn;

  function HomeController(titleService, loginService, $location) {
    if (!loginService.isConnected()) {
      $location.path('/login');
    }
    titleService.set('home.title');
  }
}());
