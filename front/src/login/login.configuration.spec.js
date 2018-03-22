(function(mock) {
  'use strict';

  describe('login module login configuration', test);

  function test() {
    var httpProvider,
      loginRouter;

    beforeEach(initTest);

    it('should have the loginRouter as an interceptor', haveLoginRouterAsAnInterceptor);

    function initTest() {
      mock.module(require('./login.module.js'), setHttpProvider);
      inject(setLoginRouter);

      function setHttpProvider($httpProvider) {
        httpProvider = $httpProvider;
      }

      function setLoginRouter(_loginRouter_) {
        loginRouter = _loginRouter_;
      }

    }

    function haveLoginRouterAsAnInterceptor() {
      expect(loginRouter).toBeDefined();
      expect(httpProvider.interceptors).toContain('loginRouter');
    }
  }
}(window.angular.mock));
