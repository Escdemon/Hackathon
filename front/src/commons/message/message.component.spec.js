(function(angular, mock) {
  'use strict';

  describe('commons module message component', messageTest);

  function messageTest() {
    describe('controller', controllerTest);
    describe('component', componentTest);
  }

  function componentTest() {
    var $compile,
      $rootScope,
      scope,
      messageService;

    beforeEach(initTest);

    it('should add listener to message service', addListenerOnCompile);
    it('should listen add event and add new panel', addPanelOnAddEvent);
    it('should listen add event and add new panel danger', addXPanelOnAddEvent('danger'));
    it('should listen add event and add new panel warning', addXPanelOnAddEvent('warning'));
    it('should listen add event and add new panel info', addXPanelOnAddEvent('info'));
    it('should listen add event and add new panel success', addXPanelOnAddEvent('success'));
    it('should listen remove event and remove panel added', removePanelOnRemove);

    function initTest() {
      mock.module(require('../commons.module.js'), provide);
      inject(setElements);

      function provide ($provide) {
        $provide.decorator('messageService', delegate);
        function delegate($delegate) {
          $delegate.addListener = jasmine.createSpy('addListener');
          return $delegate;
        }
      }
      function setElements(_$compile_, _$rootScope_, _messageService_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        scope = $rootScope.$new();
        messageService = _messageService_;
      }
    }

    function addListenerOnCompile() {
      var element = angular.element('<message></message>');
      $compile(element)(scope);
      scope.$apply();
      expect(messageService.addListener).toHaveBeenCalled();
    }

    function addPanelOnAddEvent() {
      var listener;
      messageService.addListener.and.callFake(function(givenListener) {
        listener = givenListener;
      });
      var element = angular.element('<message></message>');
      $compile(element)(scope);
      scope.$apply();
      expect(element.children().length).toBe(0);
      // Fire event
      listener.add({message:'message'});
      scope.$apply();
      expect(element.children().length).toBe(1);
      // Fire event
      listener.add({message:'message'});
      scope.$apply();
      expect(element.children().length).toBe(2);
    }

    function addXPanelOnAddEvent(type) {
      return function() {
        var listener;
        messageService.addListener.and.callFake(function(givenListener) {
          listener = givenListener;
        });
        var element = angular.element('<message></message>');
        $compile(element)(scope);
        scope.$apply();
        expect(element.find('div').length).toBe(0);
        // Fire event
        listener.add({message:'message', level: type});
        scope.$apply();
        expect(element.children().length).toBe(1);
        expect(element.children().eq(0).attr('data-type')).toBe(type);
      };
    }

    function removePanelOnRemove() {
      var listener;
      messageService.addListener.and.callFake(function(givenListener) {
        listener = givenListener;
      });
      var element = angular.element('<message></message>');
      $compile(element)(scope);
      scope.$apply();
      expect(element.children().length).toBe(0);
      // Fire add event
      listener.add({message:'message', level: 'success', id: 0});
      scope.$apply();
      expect(element.children().length).toBe(1);
      listener.remove(0);
      scope.$apply();
      expect(element.children().length).toBe(0);
    }
  }

  function controllerTest() {
    var messageController,
      messageService;

    beforeEach(initTest);

    it('should do nothing when message service call remove function on listener with unknow',
      doNothingOnRemoveUnknowPanelFromListner);
    it('should remove message when call remove function',
      removePanel);

    function initTest() {
      messageService = {};
      var MessageController = require('./message.component.js').controller;
      messageController = new MessageController(messageService);
      messageService.addListener = jasmine.createSpy('addListener');
    }

    function doNothingOnRemoveUnknowPanelFromListner() {
      var removeFunction;
      messageService.addListener = function(object) {
        removeFunction = object.remove;
      };
      messageController.$onInit();
      expect(messageController.messages.length).toBe(0);
      removeFunction(1);
      expect(messageController.messages.length).toBe(0);
    }

    function removePanel() {
      messageController.$onInit();
      messageController.messages = [{id:1}, {id:10}];
      expect(messageController.messages.length).toBe(2);
      messageController.removeMessage(1);
      expect(messageController.messages.length).toBe(1);
      expect(messageController.messages[0]).toEqual({id:10});
    }
  }
}(window.angular, window.angular.mock));
