# Action component

## Description
This component displays an action button.

## Custom methods

### css
css : `(entityName: String, rows?: [], {css?: String, currentActionName: String, executeAction: String, executeActionEntity: String, minimal: Boolean}) => String`  
To get class css.  

Parameters:
* `css` : calculated css to be added to the button (can be undefined).
* `currentActionName` : action name of page.
* `executeAction` : action name of the button.
* `executeActionEntity` : entity name of the action of the button.
* `minimal` : true if action need to be display with icon only.  

The default implementation returns value defined into attribute `css`.

### icon
icon : `(entityName: String, rows?: [], {icon?: String, currentActionName: String, executeAction: String, executeActionEntity: String, minimal: Boolean}) => String`  
  To get css class of the icon.  

Parameters:
* `icon` : calculated css class of icon to be added to the span into button (can be undefined).
* `currentActionName` : action name of page.
* `executeAction` : action name of the button.
* `executeActionEntity` : entity name of the action of the button.
* `minimal` : true if action need to be display with icon only.  

The default implementation returns value defined into attribute `icon`.

#### Example of custom
Here is an example of how to customize the icon of an action button:
```js
    customServiceProvider.setImplementation('icon')('entityName', 'action', calledFn, {
      executeAction: 'actionName',
      executeActionEntity: 'entityName'
    });
    calledFn.$inject = ['somethingToInject'];

    function calledFn(somethingToInject) {
      return function (entityName, beans, params, defaultFn) {
        if (params.minimal) {
          return 'fa fa-copy';
        }
        if (somethingToInject.isTrue()) {
          return 'fa fa-delete';
        }
        return defaultFn(entityName, beans, params);
      };
    }
```

### label
label : `(entityName: String, rows?: [], {label?: String, currentActionName: String, executeAction: String, executeActionEntity: String, minimal: Boolean}) => String`  
  To get label.  

Parameters:
* `label` : calculated label (translate key) to be added into button (can be undefined).
* `currentActionName` : action name of page.
* `executeAction` : action name of the button.
* `executeActionEntity` : entity name of the action of the button.
* `minimal` : true if action need to be display with icon only.  

The default implementation returns value defined into attribute `label`.

### tooltip
tooltip : `(entityName: String, rows?: [], {tooltip?: String, currentActionName: String, executeAction: String, executeActionEntity: String, minimal: Boolean}) => String`  
  To get tooltip.  

Parameters:
* `label` : calculated tooltip (translate key) to be added on button (can be undefined).
* `currentActionName` : action name of page.
* `executeAction` : action name of the button.
* `executeActionEntity` : entity name of the action of the button.
* `minimal` : true if action need to be display with icon only.  

The default implementation returns value defined into attribute `tooltip`.
