module.exports = (function () {
  'use strict';

  linkFactory.$inject = ['entityModel'];
  return linkFactory;

  function linkFactory(entityModel) {
    return Link;
    /**
     * Create new link.
     *
     * @param {Object} link link.
     * @param {Object} link.name name of link.
     * @param {String} link.name.front front display of link.
     * @param {String} link.name.back back display of link.
     * @param {String} link.entity name of destination entity.
     * @param {Object} [link.fk] list of entity linked fields indexed by entity corresponding field
     * @param {{entity: String, link: String}} link.associative information on associative
     * @param {Entity} entity source entity of link.
     * @constructor
     */
    function Link(link, entity) {
      var that = this,
        associativeLink;
      /**
       * Object represent name (front and back).
       * @type {{front: String, back: String}}
       */
      this.name = link.name;
      /**
       * Source entity of link.
       * Example : if contact have many task then task is source entity of link between contact and task.
       * @type {Entity}
       */
      this.srcEntity = entity;
      /**
       * Destination entity of link.
       * Example : if contact have many task then contact is destination entity of link between contact and task.
       * @type {Entity}
       */
      this.dstEntity = entityModel.entity(link.entity);
      /**
       * list of entity linked fields indexed by entity corresponding field
       * Example : {"creator":"id"} (creator is field of destination entity and id is field of source entity)
       * @type {Object}
       */
      this.fk = link.fk || {};
      /**
       * Associative entity of link (can be null).
       * Example : if contact have many to many association with task then contact_task is associative entity of link between contact and task.
       * @type {Entity}
       */
      this.associativeEntity = link.associative ? entityModel.entity(link.associative.entity) : null;

      Object.defineProperties(this, {
        /** @memberOf Link*/
        associativeLink: {
          get: getAssociativeLink
        }
      });
      /**
       * Indicate if link is associative link.
       * @type {Boolean}
       */
      this.associative = !!this.associativeEntity;

      this.setLinkedEntity = setLinkedEntity;

      this.getStringPkLinkEntity = getStringPkLinkEntity;

      // Implementations

      /**
       * Set foreign key of given destination entity with primary key of given source entity.
       * If link is associative then do nothing.
       * @param {Object} srcEntity to extract primary key
       * @param {Object} dstEntity to set foreign key
       * @return {undefined}
       */
      function setLinkedEntity(srcEntity, dstEntity) {
        if (that.associative) {
          return;
        }
        Object.keys(link.fk).forEach(function(fkField) {
          dstEntity[fkField] = srcEntity ? srcEntity[link.fk[fkField]] : null;
        });
      }

      function getStringPkLinkEntity(entity) {
        var pk = [];
        if (that.associative) {
          return;
        }
        var keys = Object.keys(link.fk);
        keys.forEach(function(fkField) {
          var value = entity[fkField];
          if (value !== null && value !== undefined) {
            pk.push(value);
          }
        });
        if (keys.length === pk.length) {
          return that.dstEntity.getStringPrimaryKey(pk);
        }
      }

      /**
       * Associative entity of link (can be null).
       * Example : if contact have many to many association with task then contact_task is associative entity of link between contact and task.
       * @return {Link}
       */
      function getAssociativeLink() {
        // Into getter because link is not necessary load before.
        if (undefined === associativeLink) {
          associativeLink = that.dstEntity ? that.dstEntity.getLink(link.associative.link) : null;
        }
        return associativeLink;
      }
    }
  }
}());
