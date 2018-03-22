module.exports = (function(ng) {
  'use strict';

  require('./menu.less');

  MenuController.$inject = [
    'customService',
    'entityModel',
    '$location',
    '$log',
    'counterService',
    'loginService',
    '$rootScope',
    'contextService'
  ];
  return menu;

  function menu() {
    return {
      restrict: 'E',
      replace: true,
      template: require('./menu.template.html'),
      scope: true,
      controller: MenuController,
      controllerAs: 'vm',
      bindToController: {
        menuOptions: '='
      }
    };
  }

  function MenuController(customService, entityModel, $location, $log, counterService, loginService, $rootScope, contextService) {
    var vm = this;
    vm.$onInit = onInit;
    vm.$onDestroy = onDestroy;
    vm.menu = {
      entries: [
        {
          'display': 'menu.ALERTE',
          'id': 'alerte',
          'entity': 'localisation',
          'action': 'create-alert',
          'icon': 'fa fa-exclamation-triangle'
        }, {
          'display': 'menu.CARTE',
          'id': 'carte',
          'href': 'carte',
          'icon': 'fa fa-globe'
        }, {
          'display': 'menu.OPERATEUR',
          'id': 'operateur',
          'entity': 'balise',
          'action': 'display',
          'icon': 'fa fa-cog'
        }
      ]
    };
    vm.menuFiltered = {};
    vm.launch = launch;
    vm.openMenu = openMenu;
    vm.closeMenu = closeMenu;
    vm.count = count;
    var deregisterUpdateFunction;

    function onInit() {
      var entries = vm.menu.entries;
      entries.forEach(addCustomEntries);
      vm.currentMenu = '';
      vm.selectedMenu = undefined;
      filterMenu();
      deregisterUpdateFunction = $rootScope.$on('login.update-security-function', function() {
        filterMenu();
      });
      $rootScope.$on('$locationChangeSuccess', selectMenuEntry);
    }

    function onDestroy() {
      if (deregisterUpdateFunction) {
        deregisterUpdateFunction();
      }
    }

    function filterMenu() {
      var entries = [];
      for (var index = 0; index < vm.menu.entries.length; index++) {
        filterEntryMenu(vm.menu.entries[index], entries);
      }
      vm.menuFiltered.entries = entries;
    }

    function filterEntryMenu(toFilter, inToSave) {
      if (!loginService.canUseFunction(toFilter.id)) {
        return;
      }
      var savedItemMenu = ng.extend({}, toFilter);
      inToSave.push(savedItemMenu);
      if (!savedItemMenu.nested || !savedItemMenu.nested.length) {
        return;
      }
      savedItemMenu.nested = [];
      for (var index = 0; index < toFilter.nested.length; index++) {
        filterEntryMenu(toFilter.nested[index], savedItemMenu.nested);
      }
    }

    /**
     * Calls the custom code to add custom entries to a menu entry.
     *
     * @param {object} existingEntry Menu entry to update.
     * @see doAddCustomEntries
     */
    function addCustomEntries(existingEntry) {
      var params = {menu: existingEntry.display};
      var getEntries = customService.get('entries')('noEntityName', 'menu', null, params);
      if (getEntries){
        getEntries.then(doAddCustomEntries(existingEntry));
      } else {
        console.error('The custom function entries for menu can\'t return undefined.');
      }
      
      if (existingEntry.nested) {
        existingEntry.nested.forEach(function(subEntry) {
          if (subEntry.nested) {
            addCustomEntries(subEntry);
          }
        });
      }
    }

    /**
     * Creates a function which adds custom entries to a menu entry.
     *
     * @param {object} entry Menu entry to update.
     * @returns ([]) => undefined
     */
    function doAddCustomEntries(entry) {
      return function(customEntries) {
        if (Array.isArray(customEntries) && customEntries.length) {
          var nested = entry.nested;
          customEntries.forEach(function(value) {

            if (value.display && ((value.entity && value.action) || value.href)) {

              if (value.index !== undefined && value.index > -1 && value.index < nested.length) {
                nested.splice(value.index, 0, value);

              } else {
                nested.push(value);
              }
            } else {
              $log.debug(JSON.stringify(value) + ' is not a valid menu entry');
            }
          });
        }
      };
    }

    /**
     * Launches an action. Generally, it performs a redirection on a list/action page
     * but it may be an action without UI.
     *
     * @param {object} [entry] Clicked menu option.
     */
    function launch(entry) {
      if (entry.entity && entry.action) {
        var model = entityModel.entity(entry.entity);
        var action = model ? model.getAction(entry.action) : undefined;

        if (action) {
          action.redirectToPageAction([], {}, {}, true);
        }
      } else if (entry.href) {
        // Create a new context
        var action = entityModel.getDummyAction();

        var customCtx = contextService.initCurrent(action, [], true);
        customCtx.customPath = entry.href;
        contextService.goTo(customCtx);
      }
      vm.menuOptions.shown = false;
    }

    function closeMenu() {
      vm.menuOptions.shown = false;
      vm.currentMenu = '';
    }

    function openMenu(entry) {
      vm.currentMenu = entry;
      if (entry.nested) {
        vm.menuOptions.shown = true;
      } else {
        launch(entry);
      }
    }

    function count(entry) {
      if (entry.entity && entry.count) {
        return counterService.count(entry.entity, entry.count);
      }
      return undefined;
    }

    /**
     * Sets the selected menu entry on location change.
     */
    function selectMenuEntry() {
      if (vm.currentMenu) {
        vm.selectedMenu = vm.currentMenu;
        vm.currentMenu = undefined;
      }
    }
  }
}(window.angular));
