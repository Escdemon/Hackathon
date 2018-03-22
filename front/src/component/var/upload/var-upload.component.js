module.exports = (function () {
  'use strict';

  VarUploadController.$inject = ['Upload', '$http', 'contextService', 'customService', 'entityModel', 'Url', 'backendRouter', '$window', 'utilsService'];

  var ComponentController = require('../var-component.component.js');
  VarUploadController.prototype = Object.create(ComponentController.prototype);
  VarUploadController.prototype.constructor = VarUploadController;

  return {
    require: {
      varForm: '^varForm'
    },
    template: require('./var-upload.template.html'),
    controller: VarUploadController,
    styles: [require('./var-upload.less')],
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

  function VarUploadController(Upload, $http, contextService, customService, entityModel, Url, backendRouter, $window, utilsService) {
    ComponentController.call(this, contextService, customService, 'var-upload');
    var $ctrl = this;
    $ctrl.$onInit = onInit;
    $ctrl.$onDestroy = onDestroy;
    $ctrl.download = download;
    $ctrl.upload = upload;
    $ctrl.deleteFile = deleteFile;
    var fileDownloadPath = 'file/download/';// REST path to download with an entityName and a varName.
    var fileUuidPath = 'file/dl/';// REST path to download with an UUID.

    function onInit() {
      $ctrl.init();
      $ctrl.initialize('uploadLabel', 'var-upload.upload');
      $ctrl.initialize('validation');

      $ctrl.url = backendRouter.request({url: Url.backend}).url;
      var entity = entityModel.entity($ctrl.entityName);
      $ctrl.formCtrl.promiseData.then(function(data) {
        var backVarName = utilsService.camelize($ctrl.varName);
         if (!$ctrl.ctx.action.isCreate() && data[backVarName] && !data[backVarName]['null']) {
          var encodedPk = entity.getStringPrimaryKey(entity.extractPrimaryKey(data));
          $ctrl.downloadLink = fileDownloadPath + $ctrl.entityName + '/' + encodedPk + '/' + $ctrl.varName;
        }
      });
    }

    /**
     * Saves this component's state into the current context.
     */
    function onDestroy() {
      var state = {
        uploadLabel: $ctrl.uploadLabel,
        validation: $ctrl.validation
      };
      $ctrl.saveState(state);
    }

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
        $ctrl.downloadLink = fileUuidPath + response.data.uuid + '?rm=false';

        if ($ctrl.isMandatory) {
          m.$setValidity('required', true);
        }
      }, function() {
        $ctrl.inProgress = false;
      });
    }

    function download() {
      if (!$ctrl.downloadLink) {
        return;
      }
      if ($ctrl.downloadLink.indexOf(fileDownloadPath) === 0) {
        // While downloading with entityName/varName, the server returns an UUID to get the file.
        $http.get(Url.backend + $ctrl.downloadLink).then(function(response) {
          window.open($ctrl.url + fileUuidPath + response.data.uuid);
        });
      } else if ($ctrl.downloadLink.indexOf(fileUuidPath) === 0) {
        window.open($ctrl.url + $ctrl.downloadLink);
      }
    }

    function deleteFile() {
      $ctrl.downloadLink = undefined;
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

  }
}());
