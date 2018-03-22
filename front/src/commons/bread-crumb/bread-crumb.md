# Breadcrumb component

## Description

Component to display the navigation bar on top of the pages.

```
Menu list > Action 1 > Action 2 > Action 3 > Action Page currently being viewed
```

## Constants

* `breadCrumb.MAX` : max elements to switch into compact mode:
  ```
  Menu list > ... > Action 3 > Action Page currently being viewed
  ```
  > `7` by default.
* `breadCrumb.NB_DISPLAY_BEGIN` : number of elements to display at the begin of breadcrumb when displayed in compact mode.
  > `1` by default.
* `breadCrumb.NB_DISPLAY_END` : number of elements to display at end of breadcrumb when displayed in compact mode.
  > `2` by default.


## Override

Thoses constants can be overriden by custom code if needed.  
Alternatively the file can be changed and put in generation exclusion.
