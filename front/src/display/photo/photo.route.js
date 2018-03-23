module.exports = (function() {
  'use strict';

  var entityName = 'photo';
  var templatesQuery = {
    'photo': require('.\/queries\/photo.template.html')
  };

  photoRoutes.$inject = ['$routeProvider'];

  return photoRoutes;

  function photoRoutes ($routeProvider) {
    $routeProvider
      .when('/photo/attach/:entity/:link', {
          template: function(param) {
              return '<var-form' +
              '  data-action-name="attach"' +
              '  data-entity-name="' + entityName + '">' +
              '<query-list' +
              '  data-query-name="photo"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.photo +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'photo\'"' +
              '    max-display="2"' +
              '    data-action-names="[\'attach\', \'update\', \'display\', \'delete\']"' +
              '    data-link-name="' + param.link + '"' +
              '    data-entity-name="' + entityName + '"' +
              '    data-minimal="true"' +
              '    data-row="row"' +
              '    data-without-label="true">' +
              '  </action>' +
              ' </renderer>' +
              '</query-list>' +
              '</var-form>';
            }
        }
      )
      .when('/photo/detach/:entity/:link', {
          template: function(param) {
              return '<var-form' +
              '  data-action-name="detach"' +
              '  data-entity-name="' + entityName + '">' +
              '<query-list' +
              '  data-query-name="photo"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.photo +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'photo\'"' +
              '    max-display="2"' +
              '    data-action-names="[\'attach\', \'update\', \'display\', \'delete\']"' +
              '    data-link-name="' + param.link + '"' +
              '    data-entity-name="' + entityName + '"' +
              '    data-minimal="true"' +
              '    data-row="row"' +
              '    data-without-label="true">' +
              '  </action>' +
              ' </renderer>' +
              '</query-list>' +
              '</var-form>';
            }
        }
      )
      .when('/photo/create', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="create"' +
            '>' +
              '<photo' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="photo"' +
              '</photo>' +
            '</var-form>'
        }
      )
      .when('/photo/update/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="update"' +
            '>' +
              '<photo' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="photo"' +
              '</photo>' +
            '</var-form>'
        }
      )
      .when('/photo/display/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="display"' +
            '>' +
              '<photo' +
                  ' data-readonly="false"' +
                  ' data-protected="false"' +
                  ' data-name="photo"' +
              '</photo>' +
            '</var-form>'
        }
      )
      .when('/photo/delete/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="delete"' +
            '>' +
              '<photo' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="photo"' +
              '</photo>' +
            '</var-form>'
        }
      )
      .when('/photo/list', {
        template: '<query-list' +
              '  data-action-name="list"' +
              '  data-query-name="photo"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.photo +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'photo\'"' +
              '    max-display="2"' +
              '    data-action-names="[\'attach\', \'update\', \'display\', \'delete\']"' +
              '    data-entity-name="' + entityName + '"' +
              '    data-minimal="true"' +
              '    data-row="row"' +
              '    data-without-label="true">' +
              '  </action>' +
              ' </renderer>' +
              '</query-list>'
      });
  }
}());
