(function (mock) {
  'use strict';

  describe('app.component module', coreModule);

  function coreModule() {
    describe('var-label component', varLabel);
  }

  function varLabel() {
    var $compile,
      $rootScope,
      scope,
      contextService,
      Context,
      Action;

    it('should display label into label when readonly is true and have label',
      displayLabelWhenReadonlyTrueAndLabelExist);
    it('should display label into label when readonly is false and have label',
      displayLabelWhenReadonlyFalseAndLabelExist);
    it('should display label into label when protected is true and have label',
      displayLabelWhenProtectedTrueAndLabelExist);
    it('should display label into label when protected is false and have label',
      displayLabelWhenProtectedFalseAndLabelExist);

    beforeEach(initTest);

    function initTest() {
      mock.module(require('../component.module.js'), provide);
      mock.module(require('../../app/app.module'));
      inject(setElements);
      contextService.getCurrent.and.returnValue(new Context(
        new Action({
          name : {front: '', back: ''},
          label: '',
          title: '',
          icon: '',
          input: 'object-one',
          process: 'none',
          persistence: 'none',
          'read-only': false,
          'io-flux': 'none'
        }, null), [], 0
      ));
    }

    function setElements(_$compile_, _$rootScope_, _contextService_, _Context_, _Action_) {
      $compile = _$compile_;
      $rootScope = _$rootScope_;
      scope = $rootScope.$new();
      contextService = _contextService_;
      Context = _Context_;
      Action = _Action_;
    }

    function provide($provide) {
      $provide.decorator('contextService', delegateBySpy('getCurrent'));
    }

    function delegateBySpy(name) {
      return function ($delegate) {
        $delegate[name] = jasmine.createSpy(name);
        return $delegate;
      };
    }

    /**
     * should display label into label when readonly is true and have label
     */
    function displayLabelWhenReadonlyTrueAndLabelExist() {
      contextService.getCurrent.and.returnValue(new Context(
        new Action({
          name : {front: '', back: ''},
          label: '',
          title: '',
          icon: '',
          input: 'object-one',
          process: 'none',
          persistence: 'none',
          'read-only': true,
          'io-flux': 'none'
        }, null), [], 0
      ));
      var element = $compile(
        '<var-label label="label-test"></var-label>')
      (scope);
      scope.$digest();

      var label = element.find('label');

      expect(label.hasClass('edit-mode')).toBe(false);
      expect(label.hasClass('lookup-mode')).toBe(true);
      expect(label.text().trim()).toBe('label-test');
    }

    /**
     * should display label into label when readonly is false and have label
     */
    function displayLabelWhenReadonlyFalseAndLabelExist() {
      var element = $compile(
        '<var-label label="label-test"></var-label>')
      (scope);
      scope.$digest();

      var label = element.find('label');

      expect(label.hasClass('edit-mode')).toBe(true);
      expect(label.hasClass('lookup-mode')).toBe(false);
      expect(label.text().trim()).toBe('label-test');
    }

    /**
     * should display label into label when protected is true and have label
     */
    function displayLabelWhenProtectedTrueAndLabelExist() {
      scope.e = true;
      var element = $compile(
        '<var-label is-protected="e" label="label-test"></var-label>')
      (scope);
      scope.$digest();

      var label = element.find('label');

      expect(label.hasClass('non-editable')).toBe(true);
      expect(label.hasClass('editable')).toBe(false);
      expect(label.text().trim()).toBe('label-test');
    }

    /**
     * should display label into label when protected is false and have label
     */
    function displayLabelWhenProtectedFalseAndLabelExist() {
      scope.e = false;
      var element = $compile(
        '<var-label is-protected="e" label="label-test"></var-label>')
      (scope);
      scope.$digest();

      var label = element.find('label');

      expect(label.hasClass('non-editable')).toBe(false);
      expect(label.hasClass('editable')).toBe(true);
      expect(label.text().trim()).toBe('label-test');
    }

  }
}(window.angular.mock));
