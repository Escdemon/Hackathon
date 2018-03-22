# Fieldset component

## Description
This component displays a fieldset as a Bootstrap panel.

## Custom methods
* title : `(string, {}, {}) => Promise` Retrieves the key of the label to display into the panel heading.

  Default : `undefined` so the default label is used.

* tooltip : `(string, {}, {}) => Promise` Retrieves the key of the tooltip to display onto the panel heading.

  Default : `undefined` so the default tooltip is used.

* isProtected : `(string, {}, {}) => Promise` Retrieves whether the fieldset is protected or not.

  Default : `false`.

* visible : `(string, {}, {}) => Promise` Indicates whether the fieldset is visible or not.

  Default : `true`.

* collapsed : `(string, {}, {}) => Promise` Indicates whether the fieldset is collapsed (the panel's body is hidden) or not. This method is available for collapsable fieldset.

  Default : `true`.

## Parameters
The `params` parameter contains the property `templateName` for all custom methods. 