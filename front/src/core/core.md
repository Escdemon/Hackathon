# Core

## Description
This folder is the core framework of the application. It contains some internal stuff but also exposes some useful services :
* [context.service.js](#context-service)
* [custom.provider.js](./custom/custom.provider.md)
* [entity-model.provider.js](#entity-model)
* [Action life-cycle](./action.configuration.md)
* [load.service.js](#load-service)
* [rest.provider.js](#rest-service)
* [message.service.js](#message-service)
* [backend-router.service.js](#backend-router)

## Context service
The [Context Service](./context/context.service.js) is used to manage [contexts](./context/context.class.js). The context is a key concept into this architecture. The context stores a lot of informations (data, primary keys, action, components' states, etc) concerning the current action. The context is useful to perform custom functions. It avoids using too many parameters into function calls.

Among its functions, `getCurrent` is the main one.
#### Example :
```javascript
customServiceProvider.setImplementation('isProtected')('entity-name', 'group-fieldset', isEntityProtected, {});

function isEntityProtected($q, contextService, loadService) {
  return function(entityName, obj, params, defaultFun) {
    if (params.templateName === 'entity-template-1') {
      var ctx = contextService.getCurrent();
      var data = loadService.loadData(ctx.action, ctx.pks);
      return data.then(function(entity) {
        return entity && entity.status === 12;
      });
    }
    return $q.when(false);
  };
}
isEntityProtected.$inject = ['$q', 'contextService', 'loadService'];
```
## Entity model
The [Entity model](./model/entity-model.provider.js) exposes the following API :
```javascript
entity(entityName : String) => Entity
```
Retrieves an [entity object](./model/entity.class.js) which contains the model (actions, links, allowed values, etc) and utility functions (essentially to play with keys).
```javascript
action(entityName : String, actionName : String) => Action`
```
Retrieves an [action object](./model/action.class.js) which contains the model (input, persistence, process, flux) and a lot of functions concerning actions (execute, cancel, isDisplayable, etc).

## Load service
The [Load service](./load.service.js) exposes a single function : `loadData`. This function has the following signature :
```javascript
function loadData(action : Action, pks : String[], options : Object[]) => Promise
```
Globally, it delegates server calls to the [REST service](#rest-service) selecting the right function, which depends on the action's properties (input, process)

## REST service
The [REST service](./rest.provider.js) is a service to perform REST calls easily. Its API is : 
* `noInput` : `GET` to perform an action without any input.
* `create` : `POST` to create an entity.
* `save` : `PUT` to update an entity.
* `multipleSave` : `PUT` to update several entities.
* `entity` : `GET` to retrieve an entity.
* `multipleEntity` : `GET` to retrieve several entities.
* `query` : `GET` to perform a query.
* `list` : `POST` to perform a query defined as an action.
* `listLink` : `GET` to retrieve linked entities.
* `delete` : `POST` to delete an entity. The HTTP verb `delete` is not used as it is not possible to send content with Angular.
* `deleteMultiple` : `POST` to delete several entities.
* `backRef` : `GET` to retrieve a linked entity through a backref link.
* `link` : `PUT` to create a new link (associate an entity with another one).
* `getLink`: `GET` to retrieve a linked entity.

## Message service
The [Message service](../commons/message/message.service.js) manages the messages to display to the user. In fact, it does not display the messages itself, it only registers messages (function `display`) and listeners (function `addListener`). While a message is added, it notifies listeners which may display these messages, like the component `message` located into [message.component.js](../commons/message/message.component.js)

## Backend router
The [Backend router](./backend-router.service.js) is a service used as an interceptor for the `$httpProvider`. Its API is :
* `request` : Called while a request is sent to the server. It "translates" the URL.
* `response` : Executed while a successful response is coming. It displays messages, launches file downloading and extracts content.
* `responseError` : Executed while an error response is coming. It displays error messages.