module.exports = (function() {
  'use strict';

  var toReturn = {
    controller: Controller,
    template: require('./login.template.html')
  };

  Controller.$inject = ['$location', 'loginService', 'messageService', 'titleService'];
  function Controller($location, loginService, messageService, titleService) {
    var $ctrl = this;
    $ctrl.connect = connect;
    $ctrl.failed = false;
    $ctrl.inProgress = false;

    titleService.set('login.title-page');
    if ($location.search() && $location.search().refresh) {
      loginService.logOut();
    } else if (loginService.isConnected()) {
      $location.path('/');
    }

    function connect() {
      $ctrl.failed = false;
      $ctrl.inProgress = true;
      loginService
        .connect($ctrl.login, $ctrl.password)
        .then(connectResult);

      function connectResult (isSuccess) {
        $ctrl.inProgress = false;
        $ctrl.failed = !isSuccess;
        if (isSuccess) {
          $location.path('/');
          messageService.display({display:'login.connect-success', level:'success', duration: 2000});
        } else {
          $ctrl.password = '';
        }
      }
    }
  }

  return toReturn;
}());
