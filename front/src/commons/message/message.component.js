module.exports = (function() {
  'use strict';

  require('./message.less');

  Controller.$inject = ['messageService'];

  return {
    controller: Controller,
    template: require('./message.template.html')
  };

  function Controller(messageService) {
    var $ctrl = this;
    $ctrl.messages = [];
    $ctrl.removeMessage = removeMessage;
    $ctrl.$onInit = function() {
      $ctrl.messages = [];
      messageService.addListener({
        add: addMessage,
        remove: removeMessage
      });
    };

    function addMessage(message) {
      $ctrl.messages.push(message);
    }

    function removeMessage(id) {
      for (var i =  $ctrl.messages.length - 1; i >= 0; i--) {
        if ($ctrl.messages[i].id === id) {
          $ctrl.messages.splice(i, 1);
          break;
        }
      }
    }
  }

}());
