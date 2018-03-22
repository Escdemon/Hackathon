module.exports = (function () {
  'use strict';

  return EntityModelProvider;

  function EntityModelProvider() {
    const DUMMY_NAME = { front: 'ng-dummy', back: 'NG_DUMMY' };
    var entitiesJson = {},
      entities = {};
    this.$get = service;
    this.addEntity = addEntity;

    service.$inject = ['$injector'];
    function service($injector) {
      return {
        entity: entity,
        action: action,
        getDummyEntity: getDummyEntity,
        getDummyAction: getDummyAction
      };

      /**
       * return entity with given name.
       * @param {String} entityName name of entity search.
       * @returns {Entity} entity with given name.
       */
      function entity(entityName) {
        if (DUMMY_NAME.front === entityName) {
          return getDummyEntity();
        }

        var Entity = $injector.get('Entity');
        if (!entities[entityName]) {
          if (!entitiesJson[entityName]) {
            console.error('Entity ' + entityName + ' does\'nt exist', entitiesJson, entities, arguments);
            throw new Error('Entity ' + entityName + ' does\'nt exist');
          }
          entities[entityName] = new Entity(entitiesJson[entityName]);
          entities[entityName].init();
        }
        return entities[entityName];
      }

      /**
       * Return action of entity with given name.
       * @param {String} entityName name of entity search.
       * @param {String} actionName name of action search.
       * @returns {Action} action with given name.
       */
      function action(entityName, actionName) {
        return entity(entityName).getAction(actionName);
      }

      /**
       * Returns a "dummy" entity used for custom menu entries.
       * @returns {Entity} the "dummy" entity.
       */
      function getDummyEntity() {
        var entityName = DUMMY_NAME.front;
        var Entity = $injector.get('Entity');
        if (!entities[entityName]) {
          entities[entityName] = new Entity({ 
            name: DUMMY_NAME,
            pk: [],
            pkMap: {},
            actions: [
              {
                name: DUMMY_NAME
              }
            ],
            links: [],
            queries: []
          });
          entities[entityName].init();
        }
        return entities[entityName];
      }

      /**
       * Returns the "dummy" action of the "dummy" entity.
       * @returns {Action} the "dummy" action.
       */
      function getDummyAction() {
        return getDummyEntity().getAction(DUMMY_NAME.front);
      }
    }

    /**
     * Add definition of an entity.
     * @param entityJson json who's describe entity.
     */
    function addEntity(entityJson) {
      entitiesJson[entityJson.name.front] = entityJson;
    }
  }
}());
