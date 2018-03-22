# header component

## Description
This component displays the header. it contains a logo (logo1.png), the breadcrumbs and the connected user's avatar.

## Custom methods

### user-avatar

user-avatar : `(string, undefined, {}, function) => String` Retrieves user's avatar URL to display.  
The URL must be accessible through a 'ng-src' attribute.

Parameters are:
* `'noEntityName'` : forced value
* `undefined` : No bean passed
* `params` : This object contains a 'login' attribute equals to current user login.

To set the avatar for a single user, you could write:
```js
  customServiceProvider.setImplementation('user-avatar')('noEntityName', 'var-header', getUserAvatar, {});

    function getUserAvatar() {
      return function(entityName, bean, params, defaultImpl) {
        if (params.login !== 'bureauco') {
          return "http://www.jqueryscript.net/images/Simplest-Responsive-jQuery-Image-Lightbox-Plugin-simple-lightbox.jpg";
        } else {
          return defaultImpl();
        }
      };
    }
```
