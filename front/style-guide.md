# Style guide
This document exposes how to compose your application to get a clean code.

## John Papa's style guide
The application follows [John Papa's style guide](https://github.com/johnpapa/angular-styleguide/tree/master/a1#table-of-contents). This document is a De Facto standard into the community AngularJS. The application uses particularly modules to respect the principle "[Single Responsibility](https://github.com/johnpapa/angular-styleguide/tree/master/a1#single-responsibility)".

## Modules
A module is a container for the different parts of the application. It is a way to organize the code like packages in Java or C#. See the [developer guide](https://code.angularjs.org/1.5.9/docs/guide/module) for more details.

The application uses modules for :
* Structural components (home, login, header, footer, breadcrumb)
* Reusable components or common utilities (services, UI components, etc)
* Application level components (functionalities) which are ordered by entity

For example, given the entity `CUSTOMER`, the application exposes a directory `customer` with the following :
* `queries` : lists' description files (they are required into routes or templates)
* `template` : UI components to display a customer.
* `customer.route.js` : configures the route provider
* `customer.translate.js`: registers the translations for the entity `CUSTOMER` using a file `customer-translate-locale.json` for each available locale
* `customer.entity.json` : Entity model (available actions, allowed values, links with other entities)
* `customer.module.js` : declares the module. It registers the above files while the application starts up :

```javascript
module.exports = (function(ng) {
  'use strict';

  return ng
    // Declares the module with a dependency to the core
    .module('app.customer', ['app.core'])
    // Configures the routes
    .config(require('./customer.route.js'))
    // Declares available components
    .component('customer', require('./template/customer.component.js'))
    .component('customerSearch', require('./template/customer-search.component.js'))
    // Registers the translations
    .config(require('./customer.translate.js'))
    // Configures the entity model
    .constant('customer', require('./customer.entity.json'))
    .config(require('../entity.configuration.js')(require('./customer.entity.json')))
    .name;
}(window.angular));
```

The application itself is a module tree, see [app.module.js](src/app/app.module.js).

## Components
When you create a component controller you must create `$onInit` and `$onDestroy` functions. You can found more information on [component](https://docs.angularjs.org/guide/component).  
On `$onInit` function you can access to binding parameters and you can call `$watch` or `$on` functions.  
On `$onDestroy` function you need call function given by `$watch` and `$on` functions.  
Example :  
```javascript
function Controller() {
  var $ctrl = this;
  $ctrl.$onInit = onInit;
  $ctrl.$onDestroy = onDestroy;

  var onRouteChangeSuccess, onRouteChangeStart;

  function onInit() {
    onRouteChangeStart = $rootScrope.$on('$routeChangeSuccess', whenRouteChange);
    onRouteChangeSuccess = $rootScrope.$on('$routeChangeSuccess', whenRouteChange);
  }

  function onDestroy() {
    if (onRouteChangeStart) {
      onRouteChangeStart();
    }
  }

  function whenRouteChange() {
    // Some code
    onRouteChangeSuccess();
  }
}
```

## Custom code
Custom code must be implemented into the module `custom` which is defined into the file [custom.module.js](src/custom/custom.module.js). Under this folder, you arrange your code __as you want__. You may follow these rules :
* Create a configuration file for each entity business logic (`customer.logic.js`, `order.logic.js`, etc).
* Create files as many as needed for easier debugging and testing.
* For each custom component, create a folder. For example, given the component `my-custom`,
  * create a folder `my-custom` which contains files :
    * `my-custom.component.js`
    * `my-custom.template.html`
    * `my-custom.less`
    * tranlations, etc
  * register this component :

```javascript
// custom.module.js
module.exports = (function(ng) {
  'use strict';

  return ng
    .module('custom', [
      require('../core/core.module.js')
    ])
    .component('myCustom', require('./my-custom/my-custom.component.js'))
    .name;
}(window.angular));
```

* to be completed