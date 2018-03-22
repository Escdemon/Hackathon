# Line component

## Description
This component displays a template line as a Bootstrap fieldset.

## Custom methods
* title : `(string, {}, {}) => Promise` Retrieves the key of the label to display into the panel heading.

  Default : `undefined` so the default label is used.

* tooltip : `(string, {}, {}) => Promise` Retrieves the key of the tooltip to display onto the panel heading.

  Default : `undefined` so the default tooltip is used.

* isProtected : `(string, {}, {}) => Promise` Retrieves whether the fieldset is protected or not.

  Default : `false`.

* visible : `(string, {}, {}) => Promise` Indicates whether the fieldset is visible or not.

  Default : `true`.

## Parameters
The `params` parameter contains the property `templateName` for all custom methods. 