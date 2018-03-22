(function(mock, angular) {
  'use strict';

  describe('module login Login service', test);

  function test() {
    var $httpBackend,
      url = 'test',
      login = 'login',
      password = 'password',
      token = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' +
        'eyJsb2dpbiI6ImFkbWluIiwibmFtZSI6ImFkbWluIiwiYWRtaW4iOnRydWV9' +
        '.Bg85j9W88l_J1pQ_QG7XS9MnPuXK5kWaXfK1OL1Gt5k',
      loginJson = {login: login, password: password};

    function response() {
      return [
        200, // status
        {token: token} // body
      ];
    }

    beforeEach(initTest);

    it('should return true when connect with correct login', inject(returnTrueWhenCorrectLogin));
    it('should return false when connect with incorrect login', inject(returnFalseWhenIncorrectLogin));
    it('should save token', inject(saveToken));

    function initTest() {
      var fakeModule = angular.module('test.app.config', []);
      fakeModule.config(function (loginServiceProvider) {
        loginServiceProvider.setDefaultUrl(url);
      });
      mock.module(require('./login.module.js'), 'test.app.config');
      inject(function(_$httpBackend_) {
        $httpBackend = _$httpBackend_;
      });
    }

    function returnTrueWhenCorrectLogin(loginService) {
      // Given
      $httpBackend.expectPOST(url + 'auth/login', loginJson).respond(response);

      // When
      var promise = loginService.connect(login, password);

      // Then
      promise.then(function(result) {
          expect(result).toBe(true);
        }, function() {
          fail('cannot be reject');
        }
      );
      $httpBackend.flush();
    }

    function returnFalseWhenIncorrectLogin(loginService) {
      // Given
      var login = 'login', password = 'password';
      $httpBackend.expectPOST(url + 'auth/login', loginJson).respond(401);

      // When
      var promise = loginService.connect(login, password);

      // Then
      promise.then(function(result) {
          expect(result).toBe(false);
        }, function() {
          fail('cannot be reject');
        }
      );
      $httpBackend.flush();
    }

    function saveToken(loginService) {
      // Given
      $httpBackend.expectPOST(url + 'auth/login', loginJson).respond(response);
      loginService.connect(login, password);
      $httpBackend.flush();
      var callBack = jasmine.createSpy('callBack');

      // When
      loginService.getToken(callBack);

      // Then
      expect(callBack).toHaveBeenCalledWith(token);
    }
  }
}(window.angular.mock, window.angular));
