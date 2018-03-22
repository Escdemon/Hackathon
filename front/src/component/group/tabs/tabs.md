# Default component

## Description
This component displays a template `tab`.

The `params` parameter contains the following properties for all custom methods :
* `template` : Name of the template to display

## Custom methods
* tabToOpen : `(string, {}, {}) => Promise` Retrieves the index of the tab to open while the template is loaded.

  Default : `0`.

* isProtected : `(string, {}, {}) => Promise` Retrieves whether the template is protected or not.

  Default : `false`.

* visible : `(string, {}, {}) => Promise` Indicates whether the template is visible or not.

  Default : `true`.

## Parameters
The `params` parameter contains the property `templateName` for all custom methods. 