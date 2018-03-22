module.exports = (function () {
  'use strict';

  VarImageController.$inject = ['Upload', 'contextService', 'customService',
          'entityModel', 'Url', 'backendRouter', 'utilsService', '$window'];

  var ComponentController = require('../var-component.component.js');
  VarImageController.prototype = Object.create(ComponentController.prototype);
  VarImageController.prototype.constructor = VarImageController;

  return {
    require: {
      varForm: '^varForm'
    },
    template: require('./var-image.template.html'),
    controller: VarImageController,
    styles: [require('./var-image.less')],
    bindings: {
      classInput: '@',
      classLabel: '@',
      classOffset: '@',
      label: '@',
      name: '@',
      ngModel: '=',
      isProtected: '<',
      mandatory: '<',
      tooltip: '@',
      entityName: '@',
      varName: '@'
    }
  };

  function VarImageController(Upload, contextService, customService,
          entityModel, Url, backendRouter, utilsService, $window) {

    ComponentController.call(this, contextService, customService, 'var-image');
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.download = download;
    $ctrl.upload = upload;
    $ctrl.deleteFile = deleteFile;
    $ctrl.getImageURL = getImageURL;
    var fileDownloadPath = 'file/image/';// REST path to download with an entityName and a varName.
    var fileUuidPath = 'file/dl/';// REST path to download with an UUID.
    var ts = new Date().getTime();

    /**
     * Initializes this component.
     */
    function onInit() {
      $ctrl.init();
      $ctrl.initialize('validation', 'image/*');

      $ctrl.url = backendRouter.request({url: Url.backend}).url;
      var entity = entityModel.entity($ctrl.entityName);
      $ctrl.formCtrl.promiseData.then(function(data) {
        var bean = data || {};
        var container = bean[utilsService.camelize($ctrl.varName)];
        if (!$ctrl.ctx.action.isCreate() && container && !container['null']) {// null is a keyword.
          var encodedPk = entity.getStringPrimaryKey(entity.extractPrimaryKey(data));
          var path = $ctrl.entityName + '/' + encodedPk + '/' + $ctrl.varName;
          $ctrl.imageURL = $ctrl.url + fileDownloadPath + path;
          $ctrl.tooltip = $ctrl.tooltip || $ctrl.varName;
        }
      });
    }

    /**
     * Saves this component's state into the current context.
     */
    function onDestroy() {
      var state = {
        validation: $ctrl.validation
      };
      $ctrl.saveState(state);
    }

    /**
     * Uploads the selected image onto the server and updates the underlying model.
     */
    function upload(file) {
      if (!file) {
        return;
      }
      var m = $ctrl.formCtrl.form[$ctrl.id];
      m.$setDirty();
      m.$setTouched();
      $ctrl.inProgress = true;
      var uploadPromise = Upload.upload({
        url: Url.backend + 'file/upload',
        data: {
          file: Upload.rename(file, $window.btoa(file.name))
        }
      });
      uploadPromise.then(function(response) {
        $ctrl.ngModel = response.data;
        $ctrl.inProgress = false;
        $ctrl.imageURL = $ctrl.url + fileUuidPath + response.data.uuid + '?rm=false';
        $ctrl.tooltip = $ctrl.tooltip || $ctrl.ngModel.name;

        if ($ctrl.isMandatory) {
          m.$setValidity('required', true);
        }

      }, function(response) {
        $ctrl.inProgress = false;
        console.log(response.data);
      });
    }

    /**
     * Downloads the image.
     */
    function download() {
      if (!$ctrl.imageURL) {
        return;
      }
      window.open($ctrl.imageURL);
    }

    /**
     * Removes the image. It only updates the underlying model, it does not call server.
     */
    function deleteFile() {
      if ($ctrl.tooltip === $ctrl.ngModel.name) {
        $ctrl.tooltip = '';
      }
      $ctrl.downloadLink = undefined;
      $ctrl.imageURL = null;
      $ctrl.ngModel = {
        name: null,
        uuid: null,
        'null': true
      };
      var m = $ctrl.formCtrl.form[$ctrl.id];
      m.$setDirty();
      m.$setTouched();
      if ($ctrl.isMandatory) {
        m.$setValidity('required', false);
      }
    }

    /**
     * Retrieves the image URL to display it.
     */
    function getImageURL() {
      var url = $ctrl.imageURL;
      if (!url) {
        return;
      }
      // A timestamp is added to the URL to avoid caching.
      if (url.indexOf(fileDownloadPath) > -1) {
        return url + '?attachment=false' + '&ts=' + ts;
      } else if (url.indexOf(fileUuidPath) > -1) {
        return url + '&attachment=false' + '&ts=' + ts;
      }
    }
  }
}());
