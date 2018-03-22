# Var-Input component

## Description
This component displays an input as:
* a checkbox
* a number
* a text
* an amount


## Custom methods

Documentation for generic custom methods can be found in [var-component.md](../var-component.md)

## Specific custom methods:
### amount-option

Give the rules to display an amount, returns an object describing the options
- position: can be 'right' or 'left'
- currency: a string that represents the currency used 
Default is € displayed at the right of the input : 50€ 

#### Example

This exemple set that the price for a film is displayed like $50.

```js
function customVar(customServiceProvider) {
  customServiceProvider.setImplementation('amount-option')('films', 'var-input', getAmountOptions, {
    name: 'price'
  });
}

function getAmountOptions() {
  return function() {
    return {
      position: 'left',
      currency: '$'
    };
  };
}
```