(function (ng, mock) {
  'use strict';

  describe('Rest service', test);

  function test() {
    var $httpBackend,
      url = 'BackendUrl',
      response = [
        200, // status
        {somme: 'response'} // body
      ];

    beforeEach(initTest);

    it('should launch query without params', inject(shouldLaunchQueryWithoutParams));

    /**
     * Implementations
     */
    function responseFn() {
      return response;
    }

    function initTest() {
      var fakeModuleName = 'test.app.config';
      var fakeModule = ng.module(fakeModuleName, []);
      mock.module(require('./core.module.js'), fakeModuleName);
      fakeModule.config(function (restServiceProvider) {
        restServiceProvider.setBackendUrl(url);
      });
      inject(setElements);
      function setElements(_$httpBackend_) {
        $httpBackend = _$httpBackend_;
      }
    }

    function shouldLaunchQueryWithoutParams(restService) {
      // Given
      var queryName = 'query';
      var entityName = 'entity';
      var entity = {
        name: {front: entityName}
      };
      var timestamp = 1234;
      spyOn(Date.prototype, 'getTime').and.returnValue(timestamp);
      $httpBackend.expectGET(url + entityName + '/query/' + queryName + '?r-r=' + timestamp).respond(responseFn);

      // When
      var promise = restService.query(entity, queryName);

      // Then
      promise.then(function (result) {
          expect(result.data).toEqual(response[1]);
          expect(result.status).toBe(response[0]);
        }, function () {
          fail('cannot be reject');
        }
      );
      $httpBackend.flush();
    }
  }
}(window.angular, window.angular.mock));
