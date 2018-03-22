module.exports = (function () {
  'use strict';
  return RestProvider;

  function RestProvider() {
    var backendUrl;

    this.setBackendUrl = setBackendUrl;
    this.$get = restService;

    restService.$inject = ['$http', '$rootScope'];
    /**
     * Set url to launch $http query.
     * @param {String} url to launch $http query.
     */
    function setBackendUrl(url) {
      backendUrl = url;
    }

    function restService($http, $rootScope) {
      return {
        noInput: noInput,
        noInputUpdate: noInputUpdate,
        create: create,
        save: save,
        multipleSave: multipleSave,
        entity: entity,
        multipleEntity: multipleEntity,
        query: query,
        list: list,
        listLink: listLink,
        'delete': del,
        deleteMultiple: deleteMultiple,
        backRef: backRef,
        link: link,
        getLink: getLink
      };

      /**
       * Save existing entity.
       * @param {String} id id of entity.
       * @param {Action} action action for save.
       * @param {Object} entity entity to save.
       * @return {Promise}
       */
      function save(id, action, entity) {
        var url = createURL(action.entity.name.front + '/id/' + encodeURIComponent(id), action);

        return trackLoading($http({
          url: url,
          method: 'PUT',
          data: entity
        }));
      }

      /**
       * Save existing entity.
       * @param {String[]} pks id of entity.
       * @param {Action} action action for save.
       * @param {Object} entity entity to save.
       * @return {Promise}
       */
      function multipleSave(pks, action, entity) {
        var url = createURL(action.entity.name.front, action);

        return trackLoading($http({
          url: url,
          method: 'PUT',
          data: {
            bean: entity,
            keys: pks
          }
        }));
      }

      /**
       *
       * @param {Action} action
       * @return {Promise}
       */
      function noInput(action) {
        return trackLoading($http({
          url: backendUrl + action.entity.name.front + '/action/' + action.name.front,
          method: 'GET',
          params: {
            'r-r': new Date().getTime()
          }
        }));
      }

      /**
       *
       * @param {Action} action
       * @return {Promise}
       */
      function noInputUpdate(action) {
        return trackLoading($http({
          url: backendUrl + action.entity.name.front + '/action/' + action.name.front,
          method: 'PUT',
          params: {
            'r-r': new Date().getTime()
          }
        }));
      }

      /**
       * Create given entity.
       * @param {Action} action action to create entity.
       * @param {Object} entity entity to create.
       * @return {Promise}
       */
      function create(action, entity) {
        var url = createURL(action.entity.name.front, action);

        return trackLoading($http({
          url:url,
          method: 'POST',
          data: entity
        }));
      }

      /**
       * Get entity form back.
       * @param {Action|String} obj action to get entity or name of entity.
       * @param {String} id id of entity.
       * @param {String} [action] name of action.
       * @return {Promise} promise of entity.
       */
      function entity(obj, id, action) {
        var entityName, actionName;
        if (typeof obj === 'string') {
          entityName = obj;
          actionName = action;
        } else {
          entityName = obj.entity.name.front;
          actionName = obj.name.front;
        }
        return trackLoading($http({
          url: backendUrl + entityName + '/id/' + encodeURIComponent(id) + '/action/' + actionName,
          method: 'GET',
          params: {
            'r-r': new Date().getTime()
          }
        }));
      }

      /**
       *
       * @param {String[]} pks primary keys
       * @param {Action} action action to get entity.
       * @return {Promise}
       */
      function multipleEntity(pks, action) {
        return trackLoading($http({
          url: backendUrl + action.entity.name.front + '/action/' + action.name.front,
          method: 'GET',
          params: {
            id: pks,
            'r-r': new Date().getTime()
          }
        }));
      }

      /**
       *
       * @param {String} id
       * @param {Action} action
       * @param {Object} entity
       * @return {Promise}
       */
      function del(id, action, entity) {
        var url = createURL(action.entity.name.front + '/id/' + encodeURIComponent(id), action);

        return trackLoading($http({
          url: url,
          method: 'POST',
          data: entity
        }));
      }

      /**
       *
       * @param {String[]} pks
       * @param {Action} action
       * @param {Object} entity
       * @return {Promise}
       */
      function deleteMultiple(pks, action, entity) {
        var url = createURL(action.entity.name.front, action);

        return trackLoading($http({
          url: url,
          method: 'POST',
          params: {id: pks},
          data: entity
        }));
      }

      /**
       *
       * @param {Action} action
       * @param {Object} entity
       * @param {Object} params
       * @return {Promise}
       */
      function list(action, entity, params) {
        return trackLoading($http({
          url: backendUrl + action.entity.name.front + '/action/' + action.name.front,
          method: 'POST',
          data: entity || {},
          params: params || {}
        }));
      }

      /**
       * Get all entity of given query.
       *
       * @param {Entity} queryEntity entity to get.
       * @param {String} query name of query.
       * @param {Object} [params] param to filter and sort query.
       * @return {Promise}
       */
      function query(queryEntity, query, params) {
        params = params || {};
        params['r-r'] = new Date().getTime();
        return $http({
          url: backendUrl + queryEntity.name.front + '/query/' + query,
          method: 'GET',
          params: params
        });
      }

      /**
       * Get all entity of source entity of link through link filter
       * by of source entity of link primary key.
       *
       * @param {Action} action action to launch query.
       * @param {String} query name of query.
       * @param {Link} link link throw search
       * @param {String} pk primary key of entity.
       * @param {Object} [params] param to filter and sort query.
       * @param {Entity} [entity] entity to query.
       * @return {Promise}
       */
      function listLink(action, query, link, pk, params, entity) {
        params = params || {};
        params['r-r'] = new Date().getTime();
        params['link-name'] = link.name.back;
        params['link-key'] = pk;
        params.action = action.name.back;
        if (entity) {
          params['link-entity'] = entity.name.back;
        }
        if (link.associative) {
          params['link-entity'] = link.dstEntity.name.back;
        }
        return $http({
          url: backendUrl + (entity ? entity.name.front : link.srcEntity.name.front) + '/query/' + query,
          method: 'GET',
          params: params
        });
      }

      function backRef(entityName, id, action, linkName, actionEntityName) {
        return $http({
          url: backendUrl + entityName + '/id/' + encodeURIComponent(id) + '/back-ref/' + linkName,
          method: 'GET',
          params: {
            'action': action,
            'entity-action': actionEntityName,
            'r-r': new Date().getTime()
          }
        });
      }

      /**
       * Link to given entity given primary keys.
       * @param {Action} action
       * @param {String[]} pks
       * @param {Link} link
       * @param {Object} entity
       * @return {Promise}
       */
      function link(action, pks, link, entity) {
        var linkIntoPut = link.associative ? link.associativeLink : link;
        return $http({
          url: backendUrl + linkIntoPut.srcEntity.name.front + '/action/' + action.name.front + '/' + link.name.front,
          method: 'PUT',
          data: {
            bean: entity,
            keys: pks
          }
        });
      }

      /**
       * Get view of entity for link.
       * @param {Action} action
       * @param {String} pk
       * @param {Link} link
       * @return {Promise}
       */
      function getLink(action, pk, link) {
        return $http({
          url: backendUrl + action.entity.name.front + '/id/' + encodeURIComponent(pk) + '/action/' +
          action.name.front + '/' + link.name.front,
          method: 'GET',
          params: {
            'r-r': new Date().getTime()
          }
        });
      }

      /**
       * Sets `true` to the property `loading` onto the root scope
       * and restores it to `false` while the asynchronous operation is done (either it succeeds or it fails).
       * @param {Promise} asyncOp Asynchronous operation to process.
       * @returns {Promise} A new promise which encapsulates the `asyncOp`.
       */
      function trackLoading(asyncOp) {
        $rootScope.loading = true;
        return asyncOp.finally(function() {
          $rootScope.loading = false;
        });
      }

      /**
       * Creates an URL with the given path and action.
       * @param {String} path First part of the URL.
       * @param {Action} action Action to execute.
       * @returns {String} `backendUrl/path/action/action-code` for a standard action
       * and `backendUrl/path/action/action-code/sub-action/sub-action-code` for an action with a sub-action.
       */
      function createURL(path, action) {
        var url = backendUrl + path + '/action/' + action.name.front;

        if (action.selectedSubAction) {
          url += '/sub-action/' + action.selectedSubAction.code.front;
        }
        return url;
      }
    }
  }
}());
