(function(mock) {
  'use strict';

  describe('login module login router', test);

  function test() {
    var getToken;

    beforeEach(initTest);

    it('should add header Authorization when loginService give token without authorization header',
      inject(addHeaderAuthorizationWithoutAuthorizationHeader));
    it('shouldn\'t add header Authorization when loginService give token with authorization header',
      inject(doNothingWithAuthorizationHeader));
    it('shouldn\'t add header Authorization when loginService not give token without authorization header',
      inject(doNothingWithoutGivenToken));

    function initTest() {
      mock.module(require('./login.module.js'), provide);
      function provide ($provide) {
        $provide.decorator('loginService', delegate);
        function delegate($delegate) {
          $delegate.getToken = jasmine
            .createSpy('getToken')
            .and.callFake(function(callBack) {getToken(callBack);});
          return $delegate;
        }
      }
    }

    function addHeaderAuthorizationWithoutAuthorizationHeader(loginRouter) {
      // Given
      var config = {};
      var token = 'token';
      getToken = function(callBack) {
        callBack(token);
      };

      // When
      loginRouter.request(config);

      // Then
      expect(config.headers.Authorization).toBe('Bearer ' + token);
    }

    function doNothingWithAuthorizationHeader(loginRouter) {
      // Given
      var config = {headers: {Authorization: 'test'}};
      var token = 'token';
      getToken = function(callBack) {
        callBack(token);
      };

      // When
      loginRouter.request(config);

      // Then
      expect(config.headers.Authorization).toBe('test');
    }

    function doNothingWithoutGivenToken(loginRouter) {
      // Given
      var config = {};
      var token = 'token';
      getToken = function() {};

      // When
      loginRouter.request(config);

      // Then
      expect(config.headers).not.toBe({Authorization:'Bearer ' + token});
    }
  }
}(window.angular.mock));
