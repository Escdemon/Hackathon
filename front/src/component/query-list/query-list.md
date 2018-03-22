# Data list component

## Description
This component displays :
* A quick search.
* Item count / Total row count.
* Action buttons.
* A data table.
* Pagination links.

## Custom methods

### Parameters
* queryName : Name of the query to display
* columnKey : Identifier of the column (this parameter is not available for the method `list-readonly`)

### list-readonly 

Indicates whether the list is readonly or not. 

Function to return : `(entityName: string, {}, param: {}, defaultFn: ()) => boolean`

The default implementation returns `false`.

### column-title 

Retrieves the label displayed into the table header. 

Function to return : `(entityName: string, {}, param: {}, defaultFn: ()) => Promise` 

The default implementation returns the prototyped label.

### column-visible 

Indicates whether the column is visible or not.

Function to return : `(entityName: string, {}, param: {}, defaultFn: ()) => boolean`

The default implementation returns `true`.

**Example :** 
```js
customServiceProvider.setImplementation('column-visible')('contact', 'query-list', isColumnVisible, {});
function isColumnVisible() {
  return function(w,x, params) {
    console.log(arguments);
    if ('contact.contact-t1-name' === params.columnKey) {
      return true;
    }
    return false;
  };
}
```


### cell-css

Give additional class css to add on one cell. (call for every cell in tab)

Function to return : `(entityName: string, row: {}, param: {}, defaultFn: ()) => String|undefined`

The default implementation returns `undefined`.

Params : 
- `columnKey` : key of column.
- `queryName` : name of query.

**Example :**
```js
customServiceProvider.setImplementation('cell-css')('contact', 'query-list', cssCell, {});
function cssCell() {
  return function(w, row, params) {
    if ('contact.contact-t1-name' === params.columnKey) {
      var caption = 'T1_internalCaption';
      if ('Francis Bellanger' === row[caption]) {
        return 'success';
      } else if ('Fran�is Bellanger' === row[caption]) {
        return 'danger';
      }
    }
  };
}
```
