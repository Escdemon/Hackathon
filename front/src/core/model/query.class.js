module.exports = (function () {
  'use strict';

  queryFactory.$inject = [];
  return queryFactory;

  function queryFactory() {
    return Query;

    /**
     * Create Query.
     * @param {Object} query query.
     * @param {String} query.name.front name of query
     * @param {String} query.name.back name of query(back view)
     * @param {Entity} entity principal query of query.
     * @constructor
     */
    function Query(query, entity) {
      /**
       * Principal entity of query.
       * @type {Entity}
       */
      this.entity = entity;
      /**
       * Name of query.
       * @type {{front: String, back: String}}
       */
      this.name = query.name;
    }
  }
}());
