# Default component

## Description
This component displays a template default as a `div` element.

## Custom methods

* isProtected : `(string, {}, {}) => Promise` Retrieves whether the template is protected or not.

  Default : `false`.

* visible : `(string, {}, {}) => Promise` Indicates whether the template is visible or not.

  Default : `true`.

## Parameters
The `params` parameter contains the property `templateName` for all custom methods. 