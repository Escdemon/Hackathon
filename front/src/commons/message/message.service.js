module.exports = (function() {
  'use strict';

  message.$inject = ['$rootScope', '$timeout'];
  return message;

  function message ($rootScope, $timeout) {
    var count = 0,
      listeners = [],
      levels = ['success', 'info', 'warning', 'danger'];
    return {
      display: display,
      addListener: addListener
    };

    /**
     * Fire an event to display message.
     *
     * @param {Object} message object message to display
     * @param {String} message.display key message to display
     * @param {String} [message.parameters] message parameters to display
     * @param {String} [message.level] success, info, warning, danger
     * @param {Number} [message.duration] duration of display in millisecond
     */
    function display(message) {
      if (!$rootScope.messages) {
        $rootScope.messages = [];
      }
      if (message.duration) {
        $timeout(removeMessage, message.duration, true, count);
      }
      var level = levels.indexOf(message.level) !== -1 ? message.level : 'success',
        messageObject = {content: message.display, level: level, id: count};
      if (message.parameters) {
        messageObject.param = message.parameters;
      }
      $rootScope.messages[count] = messageObject;
      fireAddEvent(messageObject);

      count++;

      // To remove message in messages array
      function removeMessage(id) {
        delete $rootScope.messages[id];
        fireRemoveEvent(id);
      }

      // To launch add function on listener with message
      function fireAddEvent(message) {
        listeners.forEach(function(listener) {
          if (listener.add) {
            listener.add(message);
          }
        });
      }

      // To launch remove function on listener with id message
      function fireRemoveEvent(id) {
        listeners.forEach(function(listener) {
          if (listener.remove) {
            listener.remove(id);
          }
        });
      }
    }

    /**
     * Add listener to listen add and remove message event.
     *
     * @param {Object} listener object to contain two functions for add and remove event
     * @param {Function} listener.add function call with message object when added message
     * @param {Function} listener.remove function call with id when removed message
     */
    function addListener(listener) {
      listeners.push(listener);
    }
  }
}());
