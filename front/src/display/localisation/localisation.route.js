module.exports = (function() {
  'use strict';

  var entityName = 'localisation';
  var templatesQuery = {
    'localisation': require('.\/queries\/localisation.template.html'),
    'localisations': require('.\/queries\/localisations.template.html')
  };

  localisationRoutes.$inject = ['$routeProvider'];

  return localisationRoutes;

  function localisationRoutes ($routeProvider) {
    $routeProvider
      .when('/localisation/attach/:entity/:link', {
          template: function(param) {
              return '<var-form' +
              '  data-action-name="attach"' +
              '  data-entity-name="' + entityName + '">' +
              '<query-list' +
              '  data-query-name="localisation"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create-alert\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.localisation +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'localisation\'"' +
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
      .when('/localisation/detach/:entity/:link', {
          template: function(param) {
              return '<var-form' +
              '  data-action-name="detach"' +
              '  data-entity-name="' + entityName + '">' +
              '<query-list' +
              '  data-query-name="localisation"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create-alert\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.localisation +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'localisation\'"' +
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
      .when('/localisation/create-alert', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="create-alert"' +
            '>' +
              '<localisation' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="localisation"' +
              '</localisation>' +
            '</var-form>'
        }
      )
      .when('/localisation/create', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="create"' +
            '>' +
              '<localisation' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="localisation"' +
              '</localisation>' +
            '</var-form>'
        }
      )
      .when('/localisation/update/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="update"' +
            '>' +
              '<localisation' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="localisation"' +
              '</localisation>' +
            '</var-form>'
        }
      )
      .when('/localisation/display/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="display"' +
            '>' +
              '<localisation' +
                  ' data-readonly="false"' +
                  ' data-protected="false"' +
                  ' data-name="localisation"' +
              '</localisation>' +
            '</var-form>'
        }
      )
      .when('/localisation/delete/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="delete"' +
            '>' +
              '<localisation' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="localisation"' +
              '</localisation>' +
            '</var-form>'
        }
      )
      .when('/localisation/list', {
        template: '<query-list' +
              '  data-action-name="list"' +
              '  data-query-name="localisation"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create-alert\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.localisation +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'localisation\'"' +
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
