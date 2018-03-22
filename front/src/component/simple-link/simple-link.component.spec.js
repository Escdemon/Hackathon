(function (mock) {
  'use strict';

  describe('simple-link component', simpleLink);

  function simpleLink() {
    var $compile,
      $rootScope,
      scope,
      restService,
      customService,
      entityModel,
      contextService,
      link,
      entity,
      destEntity,
      action,
      $httpBackend;

    it('should load entity', loadEntity);

    beforeEach(initTest);

    function initTest() {
      mock.module(require('./simple-link.module.js'));
      restService = {
        backRef: jasmine.createSpy('backRef')
      };
      restService.backRef.and.returnValue({
        then: function () {
          return {
            then: jasmine.createSpy('then')
          };
        }
      });
      customService = {
        get: jasmine.createSpy('get')
      };
      customService.get.and.returnValue(function () {
        return {
          then: function () {
          }
        };
      });
      action = {name: {}};
      contextService = {
        getCurrent: jasmine.createSpy('getCurrent')
      };
      contextService.getCurrent.and.returnValue({
        resetFunction: jasmine.createSpy('resetFunction'),
        getComponentId: jasmine.createSpy('getComponentId'),
        getCache: jasmine.createSpy('getCache'),
        action: action
      });
      destEntity = {
        getStringPrimaryKey: jasmine.createSpy('getStringPrimaryKey'),
        name: {front: ''}
      };
      link = {
        dstEntity: destEntity,
        name: {front: ''},
        fk: {'fk': 'pk'}
      };
      entity = {
        getAction: jasmine.createSpy('getAction'),
        getLink: jasmine.createSpy('getLink'),
        name: {front: ''}
      };
      entity.getAction.and.returnValue(action);
      entity.getLink.and.returnValue(link);
      entityModel = {
        entity: jasmine.createSpy('entity')
      };
      entityModel.entity.and.returnValue(entity);
      mock.module(function ($provide) {
        $provide.value('restService', restService);
        $provide.value('customService', customService);
        $provide.value('entityModel', entityModel);
        $provide.value('contextService', contextService);
      });
      inject(setElements);
    }

    function setElements(_$compile_, _$rootScope_, _$httpBackend_) {
      $compile = _$compile_;
      $rootScope = _$rootScope_;
      scope = $rootScope.$new();
      $httpBackend = _$httpBackend_;
    }

    /**
     * should load entity when component load.
     */
    function loadEntity() {
      $compile('<var-form><simple-link ' +
        'data-prefix="prefix" ' +
        'data-data-src="{fk:\'fk-value\'}" ' +
        'data-actions="[]" ' +
        'data-class-input="input-class" ' +
        'data-class-offset="offset-class" ' +
        'data-is-protected="false" ' +
        'data-is-protected="false" ' +
        'data-entity-name="entity" ' +
        'data-link-name="link" ' +
        'data-quick-search="false" ' +
        'data-query-name="query" ' +
        'data-search-query-name="search-query" ' +
        'data-mandatory="false" ' +
        'data-placeholder="placeholder">' +
        '</simple-link></var-form>')
      (scope);

      scope.$digest();

      expect(restService.backRef).toHaveBeenCalled();
    }
  }
}(window.angular.mock));

