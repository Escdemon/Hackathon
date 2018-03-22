module.exports = (function () {
  'use strict';

  translateRouter.$inject = ['$injector'];

  return translateRouter;

  function translateRouter($injector) {
    return {
      request: request,
      response: response
    };

    // Add header for language and location
    function request(config) {
      // To break circular dependency
      var $translate = $injector.get('$translate');
      if (!config.headers) {
        config.headers = {};
      }

      config.headers['Accept-Language'] = $translate.use();
      return config;
    }

    /**
     * Parse response to set location
     */
    function response(response) {
      var contentLanguage;
      if (response && response.headers) {
        contentLanguage = response.headers('Content-Language');
      }
      if (contentLanguage) {
        // To break circular dependency
        var $translate = $injector.get('$translate');
        $translate.use(contentLanguage);
      }
      return response;
    }
  }
}());
