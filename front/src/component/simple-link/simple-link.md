# Simple-link component

## Description
This component displays :
* a simple link
* a quick search

## Custom methods

### Params

All custom methods are called with same params :
- `linkName` : The name of link.
- `actionName` :  The name of current action.
- `label` :  The given label key.
- `prefix` :  The prefix value to search default value to display.
- `isProtected` : The given protected value.
- `mandatory` : The given mandatory value.
- `hidden` : The given mandatory value.
- `tooltip` : The given tooltip key. 
- `placeholder` : The given placeholder key. 

### label

Give label to display.  
Return promise of label (`string`) to display.   
Called on init of component.  
Default function return translate of given key label.  

#### Example

With promise.
```js
customServiceProvider.setImplementation('label')('contact', 'link', tooltip, {
  linkName: 'task-r-contact'
});
tooltip.$inject = ['$q'];
function tooltip ($q) {
  return function () {
    return $q.when('Label');
  };
}
```
With translate.
```js
customServiceProvider.setImplementation('label')('contact', 'link', tooltip, {
  linkName: 'task-r-contact'
});
tooltip.$inject = ['$translate'];
function tooltip ($translate) {
  return function () {
    return $translate('translate.key');
  };
}
```

### tooltip

Give tooltip to display on input hover.  
Return promise of tooltip (`string`) to display.  
Called on init of component.  
Default function return translate of given key tooltip.  

#### Example

With promise.
```js
customServiceProvider.setImplementation('tooltip')('contact', 'link', tooltip, {
  linkName: 'task-r-contact'
});
tooltip.$inject = ['$q'];
function tooltip ($q) {
  return function () {
    return $q.when('Tooltip');
  };
}
```
With translate.
```js
customServiceProvider.setImplementation('tooltip')('contact', 'link', tooltip, {
  linkName: 'task-r-contact'
});
tooltip.$inject = ['$translate'];
function tooltip ($translate) {
  return function (entityName, entity) {
    return $translate('translate.key', {param1: entity.name});
  };
}
```

### placeholder

Give placeholder to display onto input.  
Return promise of placeholder (`string`) to display.  
Called on init of component.  
Default function return translate of given key placeholder.  

#### Example

With promise.
```js
customServiceProvider.setImplementation('placeholder')('contact', 'link', placeholder, {
  linkName: 'task-r-contact'
});
placeholder.$inject = ['$q'];
function placeholder ($q) {
  return function () {
    return $q.when('placeholder');
  };
}
```
With translate.
```js
customServiceProvider.setImplementation('placeholder')('contact', 'link', placeholder, {
  linkName: 'task-r-contact'
});
placeholder.$inject = ['$translate'];
function placeholder ($translate) {
  return function () {
    return $translate('translate.key');
  };
}
```

### value

Give value to display into input or into each result of quick search.  
Return promise of value (`string`) to display.  
Called on each change of value of input and for each result of quick search.  
`resultList` is valued at `true` into params when function is call for result drop-down list.  
Default function return the first entity field of :  
1. 
```js
  entity[prefix + '_internalCaption']
```

2. 
```js
  entity.internalCaption
```

3. 
```js
  entity.primaryKey
```

#### Example

```js
customServiceProvider.setImplementation('value')('contact', 'link', value, {
  linkName: 'task-r-contact'
});
function value () {
  return function (entityName, entity, param, defaultFn) {
    return defaultFn(entityName, entity, param).then(function(defaultValue) {
      if (param.resultList) {
        return (entity ? 'Result : ' : '') + defaultValue;
      }
      return (entity ? 'Selected : ' : '') + defaultValue;
    });
  };
}
```

### protected

To protect input.  
Return promise of boolean to indicate a protected input.  
Called on init of component.  
Default function return given protected value.  

#### Example

```js
customServiceProvider.setImplementation('protected')('contact', 'link', isProtected, {
  linkName: 'task-r-contact'
});
isProtected.$inject = ['$q'];
function isProtected ($q) {
  return function () {
    return $q.when(true);
  };
}
```

### hidden

To hide all component.  
Return promise of boolean to indicate a hidden component.  
Called on init of component.  
Default function return false.  

#### Example

```js
customServiceProvider.setImplementation('hidden')('contact', 'link', hidden, {
  linkName: 'task-r-contact'
});
hidden.$inject = ['$q'];
function hidden ($q) {
  return function () {
    return $q.when(true);
  };
}
```

### mandatory

To set mandatory the input.  
Return promise of boolean to indicate a mandatory input.  
Called on init of component.  
Default function return given mandatory value.  

#### Example
 
```js
customServiceProvider.setImplementation('mandatory')('contact', 'link', mandatory, {
  linkName: 'task-r-contact'
});
mandatory.$inject = ['$q'];
function mandatory ($q) {
  return function () {
    return $q.when(true);
  };
}
```
