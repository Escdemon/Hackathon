module.exports = (function() {
  'use strict';

  return utilsService;

  function utilsService() {
    return {
      camelize: camelize
    };

    /**
     * Transform a string to camelCase
     */
    function camelize(str) {
        return str.replace(/^([A-Z])|[\s-_](\w)/g, function(match, p1, p2, offset) {
            if (p2) return p2.toUpperCase();
            return p1.toLowerCase();        
        });
    }
  }
}());
