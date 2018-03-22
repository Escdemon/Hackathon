module.exports = (function () {
  'use strict';

  return LoginProvider;

  function LoginProvider() {
    var defaultUrl = '',
      sessionToken = 'session.accessToken',
      sessionLogin = 'session.login',
      sessionSecurityFunctions = 'session.security-functions';

    this.$get = loginService;
    this.setDefaultUrl = setDefaultUrl;

    loginService.$inject = ['$http', '$rootScope', 'App'];

    function loginService($http, $rootScope, App) {
      var filteredSecurityFunctions = [];
      var isFilteredSecurityFunctions = false;

      return {
        isConnected: isConnected,
        connect: connect,
        logOut: logOut,
        getToken: getToken,
        canUseFunction: canUseFunction,
        filterSecurityFunction: filterSecurityFunction
      };

      /**
       * @returns {Boolean} true if token exist, false otherwise.
       */
      function isConnected() {
        var token = sessionStorage.getItem(sessionToken);
        $rootScope.userConnected = !!token;
        return ($rootScope.userConnected);
      }

      /**
       * @param {Function} callBack call with token in parameter if token exist.
       */
      function getToken(callBack) {
        var token = sessionStorage.getItem(sessionToken);
        if (token) {
          callBack(token);
        }
      }

      /**
       * @returns {Promise.<Boolean>} On connect success promise will resolved with true. <br/>
       * On connect fail promise will resolved with false.
       */
      function connect(login, password) {
        var params = {login: login, password: password};
        return $http
          .post(defaultUrl + 'auth/login', params)
          .then(onSuccessGet, logOut)
          .then(getSecurityFunction)
          .then(function (connected) {
            $rootScope.userConnected = connected;
            return connected;
          });

        function onSuccessGet(response) {
          var data = response.data;
          if (data && data.token) {
            sessionStorage.setItem(sessionLogin, data.user);
            sessionStorage.setItem(sessionToken, data.token);
          }
          return true;
        }
      }

      function getSecurityFunction(connected) {
        if (!connected) {
          return connected;
        }
        if (App.disableSecurity) {
          return true;
        }
        return $http
          .get(defaultUrl + 'auth/security-functions')
          .then(onSuccessGetSecurityFunction, logOut);

        function onSuccessGetSecurityFunction(response) {
          var data = response.data;
          sessionStorage.setItem(sessionSecurityFunctions, JSON.stringify(data));
          fireUpdatedSecurityFunction();
          return true;
        }
      }

      function logOut() {
        sessionStorage.clear();
        $rootScope.userConnected = false;
        return false;
      }

      /**
       * @param {string | Action} functionToTest action or menu item to test.
       * @return {boolean} true if function can be used.
       */
      function canUseFunction(functionToTest) {
        if (App.disableSecurity) {
          return true;
        }
        var securityFunctions = JSON.parse(sessionStorage.getItem(sessionSecurityFunctions));
        if ((!securityFunctions || securityFunctions.length === 0)) {
          // If back don't give any function then all function are allowed
          return true;
        }
        var functions = isFilteredSecurityFunctions ? filteredSecurityFunctions : securityFunctions;
        var filterFunction;
        if (typeof functionToTest === 'string') {
          filterFunction = function (securityFunction) {
            return securityFunction.menu === functionToTest ||
              securityFunction.menuOption === functionToTest;
          };
        } else {
          filterFunction = function (securityFunction) {
            return securityFunction.action === functionToTest.name.back &&
              securityFunction.entite === functionToTest.entity.name.back;
          };
        }
        for (var index = 0; index < functions.length; index++) {
          if (filterFunction(functions[index])) {
            return true;
          }
        }
        return false;
      }

      function filterSecurityFunction(fnFilter) {
        isFilteredSecurityFunctions = true;
        var securityFunctions = JSON.parse(sessionStorage.getItem(sessionSecurityFunctions));
        filteredSecurityFunctions.length = 0;
        for (var index = 0; securityFunctions.length < index; index++) {
          if (fnFilter(securityFunctions[index])) {
            filteredSecurityFunctions.push(securityFunctions[index]);
          }
        }
        fireUpdatedSecurityFunction();
      }

      function fireUpdatedSecurityFunction() {
        $rootScope.$broadcast('login.update-security-function');
      }
    }

    function setDefaultUrl(url) {
      defaultUrl = url;
    }
  }
}());
