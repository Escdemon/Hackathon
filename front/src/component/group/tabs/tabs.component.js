module.exports = (function() {
  'use strict';

  GroupTabsController.$inject = ['contextService', 'customService', '$translate', '$element'];

  var GroupController = require('../group-component.js');
  GroupTabsController.prototype = Object.create(GroupController.prototype);
  GroupTabsController.prototype.constructor = GroupTabsController;

  return {
    require: {
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./tabs.template.html'),
    styles: [require('./tabs.less')],
    controller: GroupTabsController,
    bindings: {
      name: '@',
      entityName: '@',
      isProtected: '=',// to update the variable into the parent controller, so children will be updated.
      visible: '<',
      titleKey: '@?',
      tooltipKey: '@?'
    },
    transclude: true
  };

  function GroupTabsController(contextService, customService, $translate, $element) {
    var $ctrl = this;
    GroupController.call($ctrl, contextService, customService, $translate, 'group-tabs');
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.tabs = [];
    $ctrl.addTab = add;
    $ctrl.selectTab = select;

    /**
     * Initializes this component.
     */
    function onInit() {
      GroupController.prototype.onInit.call($ctrl);
      var state = $ctrl.ctx.getComponentState($ctrl.id);

      if (state.tabToOpen) {
        $ctrl.selectedIndex = state.tabToOpen;

      } else {
        $ctrl.waitDataToExecute(selectTabToOpen);
      }
    }

    function selectTabToOpen() {
      var data = $ctrl.ctx.getData();
      var params = {templateName: $ctrl.name};
      var getTabToOpen = $ctrl.customService.get('tabToOpen')($ctrl.entityName, 'group-tabs', data, params);
      if (getTabToOpen){
        getTabToOpen.then(function(index) {
          if (typeof index === 'number' && index > -1 && index < $element.children.length) {
            $ctrl.selectedIndex = index;
          } else {
            $ctrl.selectedIndex = 0;
          }
        });
      } else {
    	console.error('The custom function tabToOpen for group-tabs can\'t return undefined.');
      }
    }

    function onDestroy() {
      var selectedIndex = -1;
      for (var i = 0; i <  $ctrl.tabs.length; ++i) {
        if ($ctrl.tabs[i].selected) {
          selectedIndex = i;
          break;
        }
      }
      $ctrl.saveState({
        tabToOpen: selectedIndex
      });
    }

    /**
     * Adds a tab to this controller. If there is a single tab, it is selected.
     *
     * @param {TabController} tab Tab to add.
     */
    function add(tab) {
      $ctrl.tabs.push(tab);
      if ($ctrl.tabs.length - 1 === $ctrl.selectedIndex) {
        select(tab);
        delete $ctrl.selectedIndex;
      }
    }

    /**
     * Select a tab (hide the current selected tab and displays the given tab).
     *
     * @param {TabController} tab Tab to select.
     * @param {Object} $event jQuery-like event object.
     */
    function select(tab, $event) {
      $ctrl.tabs.forEach(unselectTab);
      tab.isSelected(true);

      if ($event) {
        // To avoid redirection to the home page.
        $event.preventDefault();
        $event.stopPropagation();
      }
    }

    /**
     * Unselect a tab (hides its associated content pane).
     *
     * @param {TabController} tab Tab to unselect.
     */
    function unselectTab(tab) {
      tab.isSelected(false);
    }
  }
}());
