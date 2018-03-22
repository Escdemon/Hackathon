# LinkComboBox component

## Description
This component displays a linkComboBox (an HTML select) for a link.

## Custom methods
* label : `(entityName: String, entity: Object, {id: String, queryName: String, entityLinkName: String, linkName: String, row: Object, label?: String}) => String`
  
  Retrieves the key of the label to display next to the comboBox.
  
  - `id` : id of component.
  - `queryName` : name of query.
  - `entityLinkName` : name of linked entity.
  - `linkName` : name of link.
  - `row` : object represents an object to display into option.
  - `label` : given key label (can be undefined).
  
  Default : `label` value.

* tooltip : `(entityName: String, entity: Object, {id: String, queryName: String, entityLinkName: String, linkName: String, row: Object, tooltip?: String}) => String`  
  
  Retrieves the key of the tooltip to display onto the comboBox.

  - `id` : id of component.
  - `queryName` : name of query.
  - `entityLinkName` : name of linked entity.
  - `linkName` : name of link.
  - `row` : object represents an object to display into option.
  - `tooltip` : given key tooltip (can be undefined).
  
  Default : `tooltip` value.

* mandatory : `(entityName: String, entity: Object, {id: String, queryName: String, entityLinkName: String, linkName: String, row: Object}) => String`
  
  Indicates whether whether the selection is mandatory or not.

  - `id` : id of component.
  - `queryName` : name of query.
  - `entityLinkName` : name of linked entity.
  - `linkName` : name of link.
  - `row` : object represents an object to display into option.
  
  Default : `false`.

* protected : `(entityName: String, entity: Object, {id: String, queryName: String, entityLinkName: String, linkName: String, row: Object}) => String`
 
  Retrieves whether the comboBox is protected (selection is not possible) or not.

  - `id` : id of component.
  - `queryName` : name of query.
  - `entityLinkName` : name of linked entity.
  - `linkName` : name of link.
  - `row` : object represents an object to display into option.
  
  Default : `false`.

* visible : `(entityName: String, entity: Object, {id: String, queryName: String, entityLinkName: String, linkName: String, row: Object}) => String`
 
  Indicates whether the comboBox is visible or not.

  - `id` : id of component.
  - `queryName` : name of query.
  - `entityLinkName` : name of linked entity.
  - `linkName` : name of link.
  - `row` : object represents an object to display into option.
  
  Default : `true`.
    
* option-label : `(entityName: String, entity: Object, {id: String, queryName: String, entityLinkName: String, linkName: String, row: Object}) => String`
 
  Retrieves the label to display for option.

  - `id` : id of component.
  - `queryName` : name of query.
  - `entityLinkName` : name of linked entity.
  - `linkName` : name of link.
  - `row` : object represents an object to display into option.
  
  Default : `row.internalCaption || row.primaryKey`.
  
  Example : 
```js
customServiceProvider.setImplementation('option-label')
  ('entity-name', 'link-combo', description, {
    queryName: 'query-name',
    entityLinkName: 'entity-link-name'
  });

function description() {
  return function (entityName, bean, params) {
    return params.row['column'];
  };
}
```
