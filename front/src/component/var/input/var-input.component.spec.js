(function (mock) {
  'use strict';

  describe('app.component module', coreModule);

  function coreModule() {
    describe('var-input component', varInput);
  }

  function varInput() {
    var $compile,
      $rootScope,
      scope;

    it('should display an input with a label',
      displayInputWithLabel);

    beforeEach(initTest);

    function initTest() {
      mock.module(require('../../component.module.js'));
      mock.module(require('../../../app/app.module'));
      inject(setElements);
    }

    function setElements(_$compile_, _$rootScope_) {
      $compile = _$compile_;
      $rootScope = _$rootScope_;
      scope = $rootScope.$new();
    }

    /**
     * should display an input with a label
     */
    function displayInputWithLabel() {
      scope.parentId = 'parentId';
      scope.readonly = false;
      scope.isProtected = false;
      var data = {name : 'test'};
      scope.data = data;

      var element = $compile('<var-form><var-input ' +
                                'data-readonly="readonly" ' +
                                'data-is-protected="isProtected" ' +
                                'data-name="inputId" ' +
                                'data-name-parent="parentId" ' +
                                'data-ng-model="$ctrl.data[\'name\']" ' +
                                'data-type="text">' +
                                  '<var-label ' +
                                    'data-label="test" data-name="inputId-label" data-name-parent="parentId"> ' +
                                  '</var-label>' +
                            '</var-input></var-form>')
      (scope);

      scope.$digest();

      var input = element.find('input');
      var label = element.find('label');

      expect(input.attr('type')).toBe('text');
      expect(input.attr('id')).toBe('parentId-inputId');

      expect(label.attr('id')).toBe('parentId-inputId-label');
    }
  }
}(window.angular.mock));

