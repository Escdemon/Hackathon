# Form component

## Description
Global form of a page.

## Custom methods

All methods get the following parameters:
  - `entityName` : entity displayed on page.
  - `bean` : current bean displayed on page.
  - `params` : method parameters.
    - `params.action-name` : current action.
    - `params.sub-action-name` : sub-action we want to display [could be undefined]

### entity-post-load

entity-post-load : `() => void` Called after main entity is loaded.  
The default implementation does `nothing`.

The following example initialize the comment field in creation if not already defined:
```js
  function entityPostLoad(customServiceProvider) {
    customServiceProvider.setImplementation('entity-post-load')('album', 'var-form', entityPostLoad, {});

    function entityPostLoad() { 
      return function (entityName, bean, params) {
        if (params['action-name'] === 'create' && !bean.comment) {
          bean.comment = 'Enter a comment';
        }
      };
    }
  }
```

### entity-unload

entity-unload : `() => void` Called when form is destroyed.  
The default implementation does `nothing`.

The following example initialize the comment field in creation if not already defined:
```js
  function entityUnload(customServiceProvider) {
    customServiceProvider.setImplementation('entity-post-load')('album', 'var-form', entityUnload, {});

    function entityUnload() { 
      return function (entityName, bean, params) {
        if (params['action-name'] === 'create' && !bean.comment) {
          bean.comment = 'Enter a comment';
        }
      };
    }
  }
```

### sub-action-visible

sub-action-visible : `(entityName, bean, params) => boolean` Called when sub-action buttons are loaded. Indicates whether a sub-action button should be displayed.  
The default implementation returns `true`.


The following example hides the sub-action button 'save' for action 'update' when album's genre is '1':
```js
  function subActionIsVisible(customServiceProvider) {
    customServiceProvider.setImplementation('sub-action-visible')('album', 'var-form', subActionVisible, {});

    function subActionVisible() { 
      return function (entityName, bean, params) {
        return !(bean.genre === 1 && params['action-name'] === 'update' && params['sub-action-name'] === 'save');
      };
    }
  }
```
