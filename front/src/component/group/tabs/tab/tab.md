# Default component

## Description
This component manages a tab.

## Custom methods
* title : `(string, {}, {}) => Promise` Retrieves the key of the label to display into the tab.

  Default : `undefined` so the default label is used.

* tooltip : `(string, {}, {}) => Promise` Retrieves the key of the tooltip to display onto the tab.

  Default : `undefined` so the default tooltip is used.

* visible : `(string, {}, {}) => Promise` Indicates whether the template is visible or not.

  Default : `true`.

## Parameters
The `params` parameter contains the property `templateName` for all custom methods. 