(function (mock, ng) {
  'use strict';

  describe('var-form component', varForm);

  function varForm() {
    var $compile,
      $rootScope,
      scope,
      contextService,
      entityModel,
      loadService,
      entity,
      context,
      action;

    it('should not display validate button when current action have a link process',
      notDisplayValidateWithActionLinkProcess);

    beforeEach(initTest);

    function initTest() {
      mock.module(require('./var-form.module.js'));
      context = {
        getFlow: jasmine.createSpy('getFlow'),
        getData: jasmine.createSpy('getData'),
        isInFlow: jasmine.createSpy('isInFlow')
      };
      contextService = {
        getCurrent: jasmine.createSpy('getCurrent'),
        initCurrent: jasmine.createSpy('initCurrent'),
        loaded: jasmine.createSpy('loaded')
      };
      contextService.getCurrent.and.returnValue(context);
      contextService.initCurrent.and.returnValue(context);
      entityModel = {
        entity: jasmine.createSpy('entity')
      };
      action = {
        readOnly: false,
        hasLinkProcess: jasmine.createSpy('hasLinkProcess'),
        hasSingleInput: jasmine.createSpy('hasSingleInput'),
        hasMultipleInput: jasmine.createSpy('hasMultipleInput'),
        hasNoPersistence: jasmine.createSpy('hasNoPersistence'),
        hasQueryInput: jasmine.createSpy('hasQueryInput'),
        hasNoInput: jasmine.createSpy('hasNoInput')
      };
      entity = {
        getAction: jasmine.createSpy('getAction')
      };
      entity.getAction.and.returnValue(action);
      entityModel.entity.and.returnValue(entity);

      loadService = {loadData: jasmine.createSpy('loadData')};

      mock.module(function ($provide) {
        $provide.value('contextService', contextService);
        $provide.value('entityModel', entityModel);
        $provide.value('loadService', loadService);
      });
      inject(setElements);
    }

    function setElements(_$compile_, _$rootScope_) {
      $compile = _$compile_;
      $rootScope = _$rootScope_;
      scope = $rootScope.$new();
    }

    /**
     * should load entity when component load.
     */
    function notDisplayValidateWithActionLinkProcess() {
      action.hasLinkProcess.and.returnValue(true);
      var element = $compile('<var-form></var-form>')(scope);
      scope.$digest();
      var buttons = element.find('button');
      for (var indexButton = 0; buttons.length > indexButton; indexButton++) {
        if (ng.element(buttons[indexButton]).hasClass('icon-toy-check')) {
          fail('Validate button not permit');
        }
      }
    }
  }
}(window.angular.mock, window.angular));

