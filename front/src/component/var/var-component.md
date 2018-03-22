# Var components

## Description
The layer provides the following components to display variables in forms :
* Date
* Datetime
* Image
* Radio button
* Rich text
* Time
* Upload

## Custom methods

### Params

All custom methods are called with same params :
- `entityName` : The name entity.
- `componentType` :  The Type of component.
- `data` : Datas currently displayed
- `params` :  Params witch contains 'varName'

### label
Used to change the label displayed before any variable.  
Returns a string used as the label. This string can be retrieved by a key from the translate directive.

#### Example

This exemple set that the label for the upload component that dislay the album's cover is the translation matching the key 'lib-album-t1-cover-name' in translation file lib-album.

```js
    customServiceProvider.setImplementation('label')('lib-album', 'var-upload', setCoverLabel, {
      name: 'lib-album-cover'
    });

    function setCoverLabel($translate) {
      return function() {
        return $translate('lib-album.lib-album-t1-cover-name');
      }
    }

    setCoverLabel.$inject = ['$translate'];
```


### placeholder
Used to defined the placeholder set in a var-component.
Returns a string  used as placeholder. This string can be retrieved by a key from the translate directive.

#### Example
This exemple set that the placeholder for the upload component that dislay the album's cover is the translation matching the key 'lib-album-t1-cover-name' in translation file lib-album.

``` js
    customServiceProvider.setImplementation('placeholder')('lib-album', 'var-upload', setPlaceholder, {
      name: 'lib-album-cover'
    });

    function setPlaceholder($translate) {
      return function() {
        return $translate('lib-album.lib-album-t1-cover-name');
      }
    }

    setPlaceholder.$inject = ['$translate'];
```

### tooltip
Used to defined the tooltip set in a var-component.
Returns a string  used as the tooltip. This string can be retrieved by a key from the translate directive.

#### Example
This exemple set that the tooltip for the upload component that dislay the album's cover is the translation matching the key 'lib-album-t1-cover-name' in translation file lib-album.

``` js
    customServiceProvider.setImplementation('tooltip')('lib-album', 'var-upload', setTooltip, {
      name: 'lib-album-cover'
    });

    function setTooltip($translate) {
      return function() {
        return $translate('lib-album.lib-album-t1-cover-name');
      }
    }

    setTooltip.$inject = ['$translate'];
```
### mandatory
Define if a variable is mandatory. Checks can be done by filtering on the action as shown in the example below.
Returns 'true' if the variable is mandatory, default behavior is 'false'

#### Example
This example set that the title is mandatory for creating an album.

``` js
    customServiceProvider.setImplementation('mandatory')('lib-album', 'var-input', setMandatory, {
      name: 'lib-album-title'
    });

    function setMandatory(contextService) {
      return function(entityName, bean, params, defaultFn) {
        // Check if the action is 'create'
        if (contextService.getCurrent().action.isCreate()) {
          return true;
        }
        // Returns default behavior
        return defaultFn(entityName, bean, params);
      }
    }

    setMandatory.$inject = ['contextService'];
```

### isProtected
Define if a variable is protected. The input field will be disabled if the variable is protected. Checks can be done by filtering on the action as shown in the example below.
Returns 'true' if the variable is protected, default behavior is 'false'

#### Example
This example set that the title is protected when updating an album.

``` js
    customServiceProvider.setImplementation('isProtected')('lib-album', 'var-input', protectTitle, {
      name: 'lib-album-title'
    });

    function protectTitle(contextService) {
      return function(entityName, bean, params, defaultFn) {
        var action = contextService.getCurrent().action;
        // Check if the action is update, also checks if we are in a query-action, to make the rule suitable for search-tpl
        if (action.isUpdate() && !action.hasQueryInput()) {
          return true;
        }
        // Returns default behavior
        return defaultFn(entityName, bean, params);
      }
    }

    protectTitle.$inject = ['contextService'];
```

### isReadOnly
Define if a variable is only readable. The input field will be a label if the variable is read only. Checks can be done by filtering on the action as shown in the example below.
Returns 'true' if the variable is only readable, default behavior is 'false'

#### Example
This example set that the title is only readable when updating an album.

``` js
    customServiceProvider.setImplementation('isReadOnly')('lib-album', 'var-input', isAlbumReadOnly, {
      name: 'lib-album-title'
    });

    function isAlbumReadOnly(contextService) {
      return function(entityName, bean, params, defaultFn) {
        var action = contextService.getCurrent().action;
        // Check if the action is update, also checks if we are in a query-action, to make the rule suitable for search-tpl
        if (action.isUpdate() && !action.hasQueryInput()) {
          return true;
        }
        // Returns default behavior
        return defaultFn(entityName, bean, params);
      }
    }

    protectTitle.$inject = ['contextService'];
```

### visible
Define if a variable is visible. Checks can be done by filtering on the action as shown in the example below.
Returns 'false' if the variable is not visible, default behavior is 'true'

#### Example
This example set that the title is not visible when updating an album.

``` js
    customServiceProvider.setImplementation('visible')('lib-album', 'var-input', isVisible, {
      name: 'lib-album-title'
    });

    function isVisible(contextService) {
      return function(entityName, bean, params, defaultFn) {
        var action = contextService.getCurrent().action;
        // Check if the action is update, also checks if we are in a query-action, to make the rule suitable for search-tpl
        if (action.isUpdate() && !action.hasQueryInput()) {
          return false;
        }
        // Returns default behavior
        return defaultFn(entityName, bean, params);
      }
    }

    isVisible.$inject = ['contextService'];
```