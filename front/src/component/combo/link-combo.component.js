module.exports = (function (ng) {
  'use strict';

  LinkComboController.$inject = ['customService',
    'entityModel',
    'contextService',
    'restService',
    '$translate',
    '$q',
    '$scope'];

  return {
    require: {
      innerTemplate: '?^^innerTemplate',
      varForm: '?^^varForm',
      searchForm: '?^^searchForm'
    },
    template: require('./combo.template.html'),
    controller: LinkComboController,
    bindings: {
      classInput: '@',
      classLabel: '@?',
      classOffset: '@?',
      entity: '<',
      entityLinkName: '@',
      id: '@',
      isProtected: '<?',
      label: '@',
      linkName: '@',
      mandatory: '<?',
      placeholder: '@',
      prefix: '@',
      queryName: '@',
      tooltip: '@'
    }
  };

  function LinkComboController(customService,
                               entityModel,
                               contextService,
                               restService,
                               $translate,
                               $q,
                               $scope) {
    var $ctrl = this,
      onChangeData,
      onChangeLink;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = destroy;
    $ctrl.getOption = getOption;
    $ctrl.change = changeValue;

    function onInit() {
      $ctrl.formCtrl = $ctrl.innerTemplate || $ctrl.varForm || $ctrl.searchForm;
      // never mandatory if it is in a searchTemplate
      if ($ctrl.searchForm) {
        $ctrl.isMandatory = false;
      } else {
        $ctrl.isMandatory = $ctrl.mandatory;
      }
      $ctrl.showLabel = !!$ctrl.classLabel;
      var entityLinkName = entityModel.entity($ctrl.entityLinkName);
      $ctrl.link = entityLinkName.getLink($ctrl.linkName);
      var restValues = restService.query(
        $ctrl.link.dstEntity,
        $ctrl.queryName,
        {action: contextService.getCurrent().action.name.back});
      $ctrl.formCtrl.promiseData.then(function (data) {
        extractForeignKeyOfEntity(data);
      });
      var options = [];

      restValues.then(function (response) {
        response.data.results.forEach(function (allowedValue) {
          options.push(createOption(allowedValue));
        });
        $ctrl.opts = options;
        if (!$ctrl.mandatory) {
          var emptyLabelPromise =
            $ctrl.placeholder ?
              $translate($ctrl.placeholder).then(function (rtn) {
                return '' === rtn ? '' : '-- ' + rtn + ' --';
              }) :
              $q.when('');
          emptyLabelPromise.then(function (emptyLabel) {
            $ctrl.opts.unshift({label: emptyLabel, selected: 'selected'});
          });
        }
      });

      var params = {
        id: $ctrl.id,
        queryName: $ctrl.queryName,
        entityLinkName: $ctrl.entityLinkName,
        linkName: $ctrl.linkName,
        prefix: $ctrl.prefix
      };
      var dstEntityName = $ctrl.link.dstEntity.name.front;
      $ctrl.label = customService.get('label')
        (dstEntityName, 'link-combo', $ctrl.entity, ng.extend({label: $ctrl.label}, params));
      $ctrl.tooltip = customService.get('tooltip')
        (dstEntityName, 'link-combo', $ctrl.entity, ng.extend({tooltip: $ctrl.tooltip}, params));
      $ctrl.mandatory = $ctrl.mandatory ||
        customService.get('mandatory')(dstEntityName, 'link-combo', $ctrl.entity, params);
      $ctrl.isProtected = $ctrl.isProtected || contextService.getCurrent().action.readOnly ||
        customService.get('protected')(dstEntityName, 'link-combo', $ctrl.entity, params);
      $ctrl.visible = customService.get('visible')(dstEntityName, 'link-combo', $ctrl.entity, params);
      /**
       * Listen if the link may have changed
       */
      onChangeData = $scope.$on('link-changed-' + $ctrl.formCtrl.id, function (event, data) {
        if (!data.eventData) {
          return;
        }
        var columnsFKName = Object.keys($ctrl.link.fk);
        var loadNeeded = false;
        // If update is one of column of FK reload is needed
        for (var i = 0; i < columnsFKName.length; i++) {
          if (data.eventData.names.indexOf(columnsFKName[i]) !== -1) {
            loadNeeded = true;
            break;
          }
        }
        if (loadNeeded) {
          extractForeignKeyOfEntity();
        }
      });
      onChangeLink = $scope.$on('data-changed-' + $ctrl.formCtrl.id, function (event, eventData) {
        extractForeignKeyOfEntity(eventData.data);
      });
    }

    /**
     * To update selected value.
     */
    function extractForeignKeyOfEntity(data) {
      var toSet = data || $ctrl.entity;
      if (toSet) {
        $ctrl.ngModel = $ctrl.link.getStringPkLinkEntity(toSet);
      }
    }

    /**
     * Creates an option object used into the ngOptions directive.
     * @param {object} row An object which contains a row to display.
     * @returns {object} An object with a label and a value.
     */
    function createOption(row) {
      var params = {
        id: $ctrl.id,
        queryName: $ctrl.queryName,
        entityLinkName: $ctrl.entityLinkName,
        linkName: $ctrl.linkName,
        row: row,
        prefix: $ctrl.prefix
      };
      var option = {
        value: row.primaryKey
      };
      option.label = customService.get('option-label')
      ($ctrl.link.dstEntity.name.front, 'link-combo', $ctrl.entity, params);
      return option;
    }

    /**
     * Retrieves an option object {value, label} corresponding to the given value.
     *
     * @param {object} value Value.
     */
    function getOption(value) {
      if (!$ctrl.opts || !$ctrl.opts.length) {
        return null;
      }
      var selectedOpts = $ctrl.opts.filter(function (opt) {
        return opt.value === value;
      });
      return selectedOpts.length === 1 ? selectedOpts[0] : null;
    }

    /**
     * Execute when value of select is changed, to update entity.
     */
    function changeValue() {
      var fk = $ctrl.link.srcEntity.getPrimaryKeyFromString($ctrl.ngModel);
      $ctrl.link.setLinkedEntity(fk, $ctrl.entity);
      change(fk);
    }

    function destroy() {
      if (onChangeData) {
        onChangeData();
      }
      if (onChangeLink) {
        onChangeLink();
      }
    }

    function change(fk) {
      var toEmit = {
        idParent: $ctrl.formCtrl.id,
        idComponent: $ctrl.id,
        names: Object.keys($ctrl.link.fk),
        values: fk
      };
      /**
       * Indicate to others links : this link have changed
       */
      $scope.$emit('link-updated-' + $ctrl.formCtrl.id, toEmit);
    }

  }
}(window.angular));
