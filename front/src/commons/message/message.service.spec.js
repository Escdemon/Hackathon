(function(mock) {
  'use strict';

  describe('commons module message service', test);

  function test() {

    beforeEach(initTest);

    it('should fire add event at all listener',
      inject(fireAddEventAtAllListener));
    it('should fire add event at all listener without type',
      inject(fireAddEventAtAllListenerWithoutType));
    it('should fire add event at all listener without wrong level type',
      inject(fireAffEventAllListenerWithoutWrongLevelType));
    it('should fire remove event at all listener',
      inject(fireRemoveEventAtAllListener));

    function initTest() {
      mock.module(require('../commons.module.js'));
    }

    function fireAddEventAtAllListener(messageService) {
      var listener1 = {add: function() {}},
        listener2 = {add: function() {}},
        messageObject = {display: 'foo', level: 'danger'},
        messageResult = {content: 'foo', level: 'danger', id: 0};
      spyOn(listener1, 'add');
      spyOn(listener2, 'add');
      messageService.addListener(listener1);
      messageService.addListener(listener2);
      messageService.display(messageObject);
      expect(listener1.add).toHaveBeenCalledWith(messageResult);
      expect(listener2.add).toHaveBeenCalledWith(messageResult);
    }

    function fireAddEventAtAllListenerWithoutType(messageService) {
      var listener1 = {add: function() {}},
        listener2 = {add: function() {}},
        messageObject = {display: 'foo'},
        messageResult = {content: 'foo', level: 'success', id: 0};
      spyOn(listener1, 'add');
      spyOn(listener2, 'add');
      messageService.addListener(listener1);
      messageService.addListener(listener2);
      messageService.display(messageObject);
      expect(listener1.add).toHaveBeenCalledWith(messageResult);
      expect(listener2.add).toHaveBeenCalledWith(messageResult);
    }

    function fireAffEventAllListenerWithoutWrongLevelType(messageService) {
      var listener1 = {add: function() {}},
        listener2 = {add: function() {}},
        messageObject = {display: 'foo', level: 'foo'},
        messageResult = {content: 'foo', level: 'success', id: 0};
      spyOn(listener1, 'add');
      spyOn(listener2, 'add');
      messageService.addListener(listener1);
      messageService.addListener(listener2);
      messageService.display(messageObject);
      expect(listener1.add).toHaveBeenCalledWith(messageResult);
      expect(listener2.add).toHaveBeenCalledWith(messageResult);
    }

    function fireRemoveEventAtAllListener(messageService, $timeout) {
      var listener1 = {remove: function() {}},
        listener2 = {remove: function() {}},
        messageObject = {duration: 20};
      spyOn(listener1, 'remove');
      spyOn(listener2, 'remove');
      messageService.addListener(listener1);
      messageService.addListener(listener2);
      messageService.display(messageObject);

      $timeout.flush(21);

      expect(listener1.remove).toHaveBeenCalledWith(0);
      expect(listener2.remove).toHaveBeenCalledWith(0);

      $timeout.verifyNoPendingTasks();
    }
  }
}(window.angular.mock));
