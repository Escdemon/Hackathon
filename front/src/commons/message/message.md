# Message

## Description

This folder contains both a service to manage messages ([message.service.js](./message.service.js)) and an UI componnent to display messages ([message.component.js](./message.component.js))

A message is a Javascript object with the following properties :
* `display : String` : Key of the message to display
* `parameters : String` : Parameters to pass into translation
* `level : String` : `Error`, `Warning`, `Information` or `Success` (Bootstrap message levels)
* `duration : Number` : Duration of display in millisecond

The component `message` is already inserted into the page `index.html`. To display a message, use the message service's function `display`. For example :

```javascript
function validate($q, messageService) {
  return function(entityName, bean) {
    if (bean.deleted) {
      messageService.display({
        display: 'You can not perform this action, item does not exist anymore',
        level: 'danger'
      });
      return $q.when(false);
    }
    messageService.display({
      display: 'Let\'s go !',
      level: 'Success',
      duration: 1500
    });
    return $q.when(true);
  };
}
validate.$inject = ['$q', 'messageService'];
```
