# Actions component

## Description
This component displays a list of actions (ie buttons) on lists, links, ...

## Custom methods

### max-display

max-display : `(entityName: String, rows?: [], {queryName: String, linkName?: String, maxDisplay?: Number}) => boolean` Indicates number max of action to display.

Parameters are:
* queryName : name of query of displayed list.
* linkName : name of link of displayed link (can be undefined).
* maxDisplay : number max to display defined into component (can be undefined).

The default implementation returns value define into attribute `maxDisplay`.

Here is an example of implementation:
```js
  function config(customServiceProvider) {
    customServiceProvider.setImplementation('max-display')('album', 'actions', maxDisplay, {});

    function maxDisplay() { 
      return function (entityName, bean, params) {
        if (params.queryName === 'ALBUM') {
          // Only 1 action button for the ALBUM query
          return 1;
        }
      };
    }
  }
```

The value returned is used like this, assume that max-display returns 2 :
* If there is 4 actions, 2 buttons are displayed + 1 button to show a list of 2 actions.
* If there is 3 actions, 3 buttons are displayed (no need of a list for 1 action).
* If there is 2 actions, 2 buttons are displayed.
