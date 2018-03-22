module.exports = (function () {
  'use strict';

  require('./var-header.less');

  HeaderController.$inject = [
    '$location',
    'customService',
    'loginService',
    'App',
    '$translate',
    'tmhDynamicLocale',
    'contextService'
  ];
  return {
    controller: HeaderController,
    template: require('./var-header.template.html'),
    bindings: {}
  };

  function HeaderController(
    $location,
    customService,
    loginService,
    App,
    $translate,
    tmhDynamicLocale,
    contextService
  ) {
    var $ctrl = this;
    $ctrl.goHome = goHome;
    $ctrl.gotoLogOut = gotoLogOut;
    $ctrl.login = sessionStorage.getItem('session.login');
    $ctrl.avatar = getAvatar($ctrl.login);
    $ctrl.locales = App.availableLocales;
    $ctrl.localeChanged = selectLocale;
    $ctrl.selectedLocale = $translate.use();

    function selectLocale(locale) {
      if (!locale) {
        return;
      }
      $ctrl.selectedLocale = locale;
      $translate.use($ctrl.selectedLocale).then(
        function(langKey) {
          tmhDynamicLocale.set(langKey.toLowerCase());
        }
      );
      contextService.reload();
    }

    function getAvatar(login) {
      return customService.get('user-avatar')('noEntityName', 'var-header', undefined, {
        'login': login
      });
    }

    function gotoLogOut() {
      $location.path('/logout');
    }

    function goHome() {
      $location.url('/');
    }
  }
}());
