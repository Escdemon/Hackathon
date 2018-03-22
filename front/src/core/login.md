# Login service

## disableSecurity

To disable security put false into constant `disableSecurity` (define into `/src/app/app.constant.js`).  
If this constant is false a query is send at login time to get all security function.
This list is in memory and into session storage.  


## canUseFunction

This function indicate if you can use the security function (aka action or menu entry).  
Example :  
```js
/**
* @param {loginService} loginService
*/
function actionExample(loginService) {
    var action = entityModel.action("entity", "action");
    if (loginService.canUseFunction(action)) {
      // Do something
    }
}
```

```js
/**
* @param {loginService} loginService
*/
function menuExample(loginService) {
  var menuEntrtyId = "menu-entry-id";
  if (loginService.canUseFunction(menuEntrtyId)) {
    // Do something
  }
}
```

## filterSecurityFunctions

This function filter the security functions.

```js
/**
* @param {loginService} loginService
*/
function filterExample(loginService) {
  var filterFunction = function(securityFunction) {
    if (securityFunction.menu === 'menu-id' || securityFunction.menuOption === 'menu-id') {
      return false;
    }
    if (securityFunction.action === 'actionName' && securityFunction.entite === 'entityName') {
      return false;
    }
    return true;
  }
  loginService.filterSecurityFunctions(filterFunction);
}
```

When this function is call an event `login.update-security-function` is broadcast into `$rootScope`.