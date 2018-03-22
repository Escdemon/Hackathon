(function() {
  'use strict';

  describe('title service', test);

  function test() {
    var titleService,
      $rootScope;
    beforeEach(initTest);

    it('should set $rootScope.title when set function is called', setRootscoopWhenSetCalled);

    function initTest() {
      $rootScope = {};
      titleService = require('./title.service.js')($rootScope);
    }

    function setRootscoopWhenSetCalled() {
      var title = 'title';
      expect($rootScope.title).toBe(undefined);
      titleService.set(title);
      expect($rootScope.title).toBe(title);
    }
  }
}());
