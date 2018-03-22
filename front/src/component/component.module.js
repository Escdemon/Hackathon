module.exports = (function(angular) {
  'use strict';

  return angular
    .module('component', [
      'ui.bootstrap',
      'pascalprecht.translate',
      require('./action/action.module.js'),
      require('./combo/combo.module.js'),
      require('./global-search/global-search.module.js'),
      require('./label/var-label.module.js'),
      require('./query-list/query-list.module.js'),
      require('./simple-link/simple-link.module.js'),
      require('./var/input/var-input.module.js'),
      require('./var/string-long/string-long.module.js'),
      require('./form/var-form.module.js'),
      require('./form/search/search-form.module.js'),
      require('./group/default/default.module.js'),
      require('./group/fieldset/fieldset.module.js'),
      require('./group/line/line.module.js'),
      require('./group/tabs/tabs.module.js'),
      require('./var/date/var-date.module.js'),
      require('./var/datetime/var-datetime.module.js'),
      require('./var/time/var-time.module.js'),
      require('./var/upload/var-upload.module.js'),
      require('./var/image/var-image.module.js'),
      require('./var/radio-button/var-radio-button.module.js'),
      require('./inner-template/inner-template.module.js')
    ])
    .name;

}(window.angular));