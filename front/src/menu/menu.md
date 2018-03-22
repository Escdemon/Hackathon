# Menu component

## Description

This component displays the menu.

## Custom methods

### Entries

entries : `(string, {}, {}) => Promise` Retrieves menu entries to add into a menu entry.  
This method is called for each menu.

Parameters:
* As the menu is not linked to a specific entity, the parameter `entityName` is equal to `noEntityName`.
* The `type` parameter is equal to `menu`.
* The `params` parameter contains the property `menu` which is the menu identifier.

Default : `[]` no entry to add.

An implementation of this custom method could be :
```js
customServiceProvider.setImplementation('entries')('noEntityName', 'menu', getEntries, {});

function getEntries($q) {
  return function(entityName, bean, params) {
      if (params.menu === 'menu.menustudios') {
        return $q.when([{
          id: 'last-option',
          display: 'Last Menu Option',
          href: 'path/page'
        }, {
          id: 'option-2',
          display: 'Menu option at index 2',
          href: 'path/page',
          index: 2
        }]);
      } else {
        return $q.when([]);
      }
  };
}
getEntries.$inject = ['$q'];
```

### count-timer
count-timer : `(string, {}, {}) => Number` Retrieves the number of milliseconds between two server call to get a menu counter.  
This method is called for each menu entry with a count query.

Parameters:
* The `type` parameter is equal to `menu`.
* The `params` parameter contains a property `queryName`.

Default : `300 000` (5 minutes).

An implementation of this custom method could be :
```js
customServiceProvider.setImplementation('count-timer')('entity-name', 'menu', getEntityMyQueryNameTimer, {queryName: 'my-query-name'});

function getEntityMyQueryNameTimer() {
  return function() {
    // Refreshes the menu counter every 5 seconds.
    return 5000;
  };
}
```

----

## Security

All entries is filter by `loginService.canUseFunction` function, with entry id properties.  
The menu directive add an listener onto `login.update-security-function` event to launch a new filter.

----

## Custom pages

For global custom pages defined in the menu, an angular component with a route should be created.

Menu Example:
```js
{
  'display': 'menu.MENU_CUSTOM',
  'id': 'menu-custom',
  'href': 'admin',
  'icon': 'fa fa-cog'
}
```

A route should be created to answer to "admin".  
For example:
```js
  function adminRoutes ($routeProvider) {
    $routeProvider
      .when('/admin', { template: '<admin></admin>' });
  }
```

The "template" part of the route is not mandatory, it's just the most common use of a route.
