# Custom code

> If you seek how to register your custom code, go to [the second part of this document](#custom-code---how-to-override).

----

Custom code is managed by the service `customService`. Its API is relatively simple, but under the hood, it is a bit more complicated.
`customService` exposes three functions :
* `setNewFunction` defines functions to override
* `setImplementation` allows developers to override functions
* `get` returns the customized function if it exists, the default function otherwise
s
## setNewFunction
This function is used by the framework (actions, context, components) to register a new custom function. Its signature is  
```javascript
setNewFunction(functionName : String, componentName : String, defaultImplementation : Function) => Void
```
* `functionName` : Name of the new available function (ex : `label`, `isProtected`, `override-action`).
* `componentName` : Name of the component which is registering the function (ex: `var-form`, `var-time`, `action`).
* `defaultImplementation` : Default implementation for the custom function. This implementation is used if there is no specific implementation.

`setNewFunction` is called while the application starts, during the configuration phase.

## get
This function is used by the framework to get a custom function to call while the application is running. Whereas `setNewFunction` is executed once, `get` is executed several times, each time a component needs it. Its signature is :
```javascript
get(functionName : String) => Function
```
`get` returns a function which is executed by the framework to :
* find the best implementation for the current context.
* execute the found implementation (it may be the default one).

Every custom functions will be called with the following arguments :
```javascript
func(entityName : String, bean : Object, params : Object, defaultFun : Function)
```
* `entityName` : Name of the current entity.
* `bean` : Current page's entity.
* `params` : Some additionnal parameters. These parameters differs; for example, a component which displays a variable gives a variable name whereas a component which displays a link gives a link name. This object is used :
  * As a filter by the custom service to find the best implementation.
  * As an argument into the best implementation to execute. 
* `defaultFun` : The default implementation provided during `setNewFunction` registration.

The algorithm to select the best implementation is the following :
* If a parameter (into `params`) differs between the call to `setImplementation` and the `get`, the function is rejected.
* The function with the most equal parameters is selected.
* If no function is found, the default implementation is executed.

---
* Given these parameters :
```javascript
customService.get('func')(entityName, type, bean, {
  id: 'foo',
  name: 'bar',
  entityName: 'bar',
  varName: 'var-bar',
  labelKey: 'bar.var-bar-label'
});
```

* Example 1 :
```javascript
// Function 1, excluded as id is different.
customServiceProvider.setImplementation('func')(entityName, type, impl, { id: 'bar' });

// Function 2, selected as id is equal.
customServiceProvider.setImplementation('func')(entityName, type, impl, { id: 'foo' });
```

* Example 2 :
```javascript
// Function 1, excluded as id is different.
customServiceProvider.setImplementation('func')(entityName, type, impl, {
  id: 'bar'
});

// Function 2, selected (it replaces the default implementation).
customServiceProvider.setImplementation('func')(entityName, type, impl, {});
```

* Example 3 :
```javascript
// Function 1, selected as there are two identical parameters.
customServiceProvider.setImplementation('func')(entityName, type, impl, { id: 'foo', name: 'bar' });

// Function 2, rejected as there is only one identical parameter.
customServiceProvider.setImplementation('func')(entityName, type, impl, { id: 'foo' });
```

----
# Custom code - How to override

## setImplementation
The last but not least function is used by developpers to add custom behaviors into the application. Its signature is :
```javascript
setImplementation(functionName : String)
```
* `functionName` : Name of the new available function (ex : `label`, `isProtected`, `override-action`).

It returns a function to call with the following :
```javascript
fun(entityName : String, componentName : String, impl : Function, filter : Object)
```
* `entityName` : Name of the current entity.
* `componentName` : Name of the component which is registering the function (ex: `var-form`, `var-time`, `action`).
* `impl` : New implementation for the custom function.
* `filter` : Filter to execute this implementation while the the component calls the custom function with the same given parameters. It may be empty or contains some parameters. It avoids using too many tests into the implementation and keeps the code cleaner.

Filtering for examples :  
For the given parameters :
```javascript
{
  id: 'foo',
  name: 'bar',
  entityName: 'bar',
  varName: 'var-bar',
  labelKey: 'bar.var-bar-label'
}
```
Example 1
```javascript
{ id: 'bar' } // not selected as the identifier is not the same
{ id: 'foo' } // selected
```
Example 2
```javascript
{ id: 'bar' } // not selected as the identifier is not the same
{ } // selected, it replaces the default implementation
```
Example 3
```javascript
{ id: 'foo', name: 'bar' } // This implementation is selected as there is two identical parameters
{ id: 'foo' }
```
