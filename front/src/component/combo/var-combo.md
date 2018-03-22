# ComboBox component

## Description
This component displays a comboBox (an HTML select) for a variable with allowed values.

## Custom methods
* label : `(string, {}, {}) => string` Retrieves the key of the label to display next to the comboBox.

  Default : `undefined` so the default label is used.

* tooltip : `(string, {}, {}) => string` Retrieves the key of the tooltip to display onto the comboBox.

  Default : `undefined` so the default tooltip is used.

* mandatory : `(string, {}, {}) => boolean` Indicates whether whether the selection is mandatory or not.

  Default : `false`.

* protected : `(string, {}, {}) => boolean` Retrieves whether the comboBox is protected (selection is not possible) or not.

  Default : `false`.

* visible : `(string, {}, {}) => boolean` Indicates whether the comboBox is visible or not.

  Default : `true`.

* options : `(string, {}, {}, function) => Promise` Initialize the available options into the comboBox.

  Default : The allowed values for the given entity/variable.

  Example : 
```js
customServiceProvider.setImplementation('options')('entity-name', 'var-combo', getOptions, {
  varName: 'var-name'
});

function getOptions($q) {
  return function(entityName, bean, params, defaultImplFunction) {
    return $q.when([
      {label: 'Option 1', value: '1'},
      {label: 'Option 2', value: '2'},
      {label: 'Option 3', value: '3'}
    ]);
  };
}
getOptions.$inject = ['$q'];
```