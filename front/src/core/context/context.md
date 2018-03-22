# Context

## Description

This section describes the custom functions available onto the context.

Theses functions are :
* [title](#title) : Defines the action's title.
* [titleTooltip](#titleTooltip) : Defines the action's tooltip.

---
### title
title : `(string, {}, {}) => Promise` Defines the action's title which is displayed into the browser's title and into the breadcrumb.

The `params` parameter contains the following properties :
* `action-name` : Name of the current action
* `action` : current action to prepare
* `inFlow` : Indicates whether the action is in a flow
* `context` : current context
* `idContext` : current context's identifier

This method should return a promise which resolves to a string.

Default : the action's label defined into TOY.
#### Example :
```javascript
customServiceProvider.setImplementation('title')(entityName, 'context', getTitle, {});

getTitle.$inject = ['$translate'];
function getTitle($translate) {
  return function (entityName, bean, params) {
    return $translate('title.key');
  };
}
```

---
### titleTooltip
titleTooltip : `(string, {}, {}) => Promise` Defines the action's tooltip which is displayed into the breadcrumb.

The `params` parameter contains the following properties :
* `action-name` : Name of the current action
* `action` : current action to prepare
* `inFlow` : Indicates whether the action is in a flow
* `context` : current context
* `idContext` : current context's identifier

This method should return a promise which resolves to a string.

Default : the action's tooltip defined into TOY.
#### Example :
```javascript
customServiceProvider.setImplementation('titleTooltip')(entityName, 'context', getTooltip, {});

getTooltip.$inject = ['$translate'];
function getTooltip($translate) {
  return function (entityName, bean, params) {
    return $translate('tooltip.key');
  };
}
```
