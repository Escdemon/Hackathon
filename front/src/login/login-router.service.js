(function() {
  'use strict';

  loginRouter.$inject = ['$injector'];
  function loginRouter($injector) {
    var toReturn = {
      request: request
    };
    return toReturn;

    // Add header for authorization if not set
    function request(config) {
      // To break circular dependency
      var loginService = $injector.get('loginService');
      if (!config.headers) {
        config.headers = {};
      }

      if (!config.headers.Authorization) {
        loginService.getToken(function(token) {
          config.headers.Authorization = 'Bearer ' + token;
        });
      }
      return config;
    }
  }

  module.exports = loginRouter;
}());
