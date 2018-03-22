# Developper guide

## Index
* [Style guide](./style-guide.md)
* Core framework :
  * [Context](src/core/context/context.md)
  * [Core](src/core/core.md)
  * [Custom service provider](src/core/custom/custom.provider.md)
  * [Actions lifecycle's custom functions](src/core/action.configuration.md)
  * Miscellaneous
    * [Action](src/core/model/action.md)
    * [Login](src/core/login.md)
    * [Messages](src/commons/message/message.md)
    * [Translations](src/commons/translate/translate.md)
    * [Title](src/commons/title/title.md)
    * [UI](src/commons/ui/ui.md)
* Reusable components :
  * [List of all custom methods](src/custom-methods.md)
  * Variables
    * [Common custom functions](src/component/var/var-component.md)
    * [Input](src/component/var/input/var-input.md)
    * [Date](src/component/var/date/var-date.md)
    * [Datetime](src/component/var/datetime/var-datetime.md)
    * [Image](src/component/var/image/var-image.md)
    * [Radio buttons](src/component/var/radio-button/var-radio-button.md)
    * [Textarea](src/component/var/string-long/string-long.md)
    * [Time](src/component/var/time/var-time.md)
    * [Upload](src/component/var/upload/var-upload.md)
    * [Combo](src/component/combo/var-combo.md)
  * Links
    * [Simple link / quick-search](src/component/simple-link/simple-link.md)
    * [Combo](src/component/combo/link-combo.md)
    * [Lists](src/component/query-list/query-list.md)
  * Templates
    * [Default template](src/component/group/default/default.md)
    * [Fieldset](src/component/group/fieldset/fieldset.md)
    * [Line](src/component/group/line/line.md)
    * [Tabs](src/component/group/tabs/tabs.md)
      * [Tab](src/component/group/tabs/tab/tab.md)
  * Miscellaneous
    * [Action button](src/component/action/action.md)
    * [Actions (list)](src/component/action/actions.md)
    * [Query Lists](src/component/query-list/query-list.md)
    * [Global-search (quick-search, auto-complete)](src/component/global-search/global-search.md)

* Structural components :
  * [Home](src/home/home.md)
  * [Login](src/login/login.md)
  * [Header](src/header/header.md)
  * [Menu](src/menu/menu.md)
  * [Breadcrumb](src/commons/bread-crumb/bread-crumb.md)
  * [Form](src/component/form/var-form.md)
* [Available custom functions](./src/custom-methods.md)

## Build
### Configuration

You may have to add proxy to can get all package form `npm`.
Put into file `~/.npmrc` :

```properties
proxy=http://fr-proxy.groupinfra.com:3128/
https-proxy=http://fr-proxy.groupinfra.com:3128
```

### Launch application

```shell
    npm start
```

Application launched on http://localhost:8888/ with watch on modify files.

### Launch test

```shell
    npm test
```

Execute JsHint and JSCS on javascript file, and execute Karma.

```shell
    npm run test-single-run test-name
```

Execute Karma on designed test.

### Build application

```shell
    npm run dist
```

You can find all build file into dist directory.

### Production mode

To build application in production mode.
```shell
    npm run dist --hash
```