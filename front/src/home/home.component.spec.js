module.exports = (function (angular, mock) {
  'use strict';

  describe('home component', homeTest);

  function homeTest() {
    var $compile,
      $rootScope,
      scope,
      $location,
      titleService,
      loginService;

    beforeEach(initTest);

    it('should set title', setTitle);
    it('should redirect if not connected', redirectIfNotConnected);
    it('should not redirect if connected', notRedirectIfConnected);

    function initTest() {
      mock.module(require('./home.module.js'), provide);
      inject(setElements);
      loginService.isConnected.and.returnValue(true);
    }

    function setElements(_$compile_, _$rootScope_, _$location_, _titleService_, _loginService_) {
      $compile = _$compile_;
      $rootScope = _$rootScope_;
      scope = $rootScope.$new();
      $location = _$location_;
      titleService = _titleService_;
      loginService = _loginService_;
    }

    function provide($provide) {
      $provide.decorator('titleService', delegateBySpy('set'));
      $provide.decorator('loginService', delegateBySpy('isConnected'));
      $provide.decorator('$location', delegateBySpy('path'));
    }

    function delegateBySpy(name) {
      return function ($delegate) {
        $delegate[name] = jasmine.createSpy(name);
        return $delegate;
      };
    }

    function setTitle() {
      var element = angular.element('<home></home>');
      $compile(element)(scope);
      scope.$apply();
      expect(titleService.set).toHaveBeenCalledWith('home.title');
    }

    function redirectIfNotConnected() {
      loginService.isConnected.and.returnValue(false);
      var element = angular.element('<home></home>');
      $compile(element)(scope);
      scope.$apply();
      expect($location.path).toHaveBeenCalledWith('/login');
    }

    function notRedirectIfConnected() {
      var element = angular.element('<home></home>');
      $compile(element)(scope);
      scope.$apply();
      expect($location.path).not.toHaveBeenCalled();
    }
  }
}(window.angular, window.angular.mock));
