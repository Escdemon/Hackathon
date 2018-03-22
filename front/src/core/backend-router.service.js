module.exports = (function () {
  'use strict';

  backendRouter.$inject = ['$location', '$q', 'Url', 'messageService'];

  return backendRouter;

  function backendRouter($location, $q, Url, messageService) {
    return {
      request: request,
      responseError: responseError,
      response: response
    };

    function request(config) {
      config.url = toUrl(config.url);
      return config;
    }

    /**
     * Parse response to separate messages from content
     */
    function response(response) {
      if (!response) {
        return;
      }
      var data = response.data;
      if (data) {
        if (data.messages) {
          displayMessages(data.messages);
        }
        if (data.downloadFilename) {
          downloadFile(data.downloadFilename);
        }
        if (data.hasOwnProperty('content')) {
          // keep only content for following process
          response.data = data.content;
        }
      }
      return response;
    }

    /**
     * Display messages received from back
     */
    function displayMessages(messages) {
      var msgs = Array.isArray(messages) ? messages : [messages];
      msgs.forEach(function (element) {
        var message = {
          display: element.message,
          level: element.severity,
          duration: 7000
        };
        messageService.display(message);
      });
    }

    /**
     * Download file received from back
     */
    function downloadFile(fileUuid) {
      var url = request({url: Url.backend}).url + 'file/dl/';   
      window.open(url + fileUuid);  
    }

    function toUrl(url) {
      var keysUrl = Object.keys(Url);
      for (var i = 0; i < keysUrl.length; i++) {
        var c = Url[keysUrl[i]];
        if (Url[c]) {
          url = url.replace(c, Url[c]);
        }
      }
      return url;
    }

    function responseError(rejection) {
      // Always start by displaying error messages
      if (rejection.data) {
        // Error response can contains multiples errors messages
        displayMessages(rejection.data);
      } else {
        // Generic errors : no messages sent from back, use default message
        var errorMessage = {
          parameters: {
            error: rejection.statusText,
            code: rejection.status
          },
          level: 'danger',
          duration: 7000
        };
        if (rejection.status === -1) {
          errorMessage.display = 'core.http-response-no-response';
        } else if (rejection.status === 404) {
          errorMessage.display = 'core.http-response-not-found';
        } else if (rejection.status === 500) {
          errorMessage.display = 'core.http-response-server-error';
        } else {
          errorMessage.display = 'core.http-response-general-error';
        }
        messageService.display(errorMessage);
      }

      // Redirect user on some error cases
      if (rejection.status === -1) {
        $location.path('/login');
      } else if ((rejection.status === 401 || rejection.status === 403) && $location.path() !== '/login') {
        // AccessRights error, redirect to login page
        $location.url('/login?refresh=true');
      }
      return $q.reject(rejection);
    }
  }
}());
