module.exports = (function() {
  'use strict';

  var entityName = 'balise';
  var templatesQuery = {
    'balise': require('.\/queries\/balise.template.html')
  };

  baliseRoutes.$inject = ['$routeProvider'];

  return baliseRoutes;

  function baliseRoutes ($routeProvider) {
    $routeProvider
      .when('/balise/attach/:entity/:link', {
          template: function(param) {
              return '<var-form' +
              '  data-action-name="attach"' +
              '  data-entity-name="' + entityName + '">' +
              '<query-list' +
              '  data-query-name="balise"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.balise +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'balise\'"' +
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
      .when('/balise/detach/:entity/:link', {
          template: function(param) {
              return '<var-form' +
              '  data-action-name="detach"' +
              '  data-entity-name="' + entityName + '">' +
              '<query-list' +
              '  data-query-name="balise"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.balise +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'balise\'"' +
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
      .when('/balise/create', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="create"' +
            '>' +
              '<balise' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="balise"' +
              '</balise>' +
            '</var-form>'
        }
      )
      .when('/balise/update/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="update"' +
            '>' +
              '<balise' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="balise"' +
              '</balise>' +
            '</var-form>'
        }
      )
      .when('/balise/display/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="display"' +
            '>' +
              '<balise' +
                  ' data-readonly="false"' +
                  ' data-protected="false"' +
                  ' data-name="balise"' +
              '</balise>' +
            '</var-form>'
        }
      )
      .when('/balise/delete/:pks*', {
          template: '<var-form' +
                ' data-entity-name="' + entityName + '"' +
                ' data-action-name="delete"' +
            '>' +
              '<balise' +
                  ' data-readonly="true"' +
                  ' data-protected="false"' +
                  ' data-name="balise"' +
              '</balise>' +
            '</var-form>'
        }
      )
      .when('/balise/list', {
        template: '<query-list' +
              '  data-action-name="list"' +
              '  data-query-name="balise"' +
              '  data-entity-name="' + entityName + '"' +
              '  data-has-search-tpl="false">' +
              ' <actions-list ' +
              '   data-actions="[\'attach\', \'detach\', \'create\', \'update\', \'display\', \'delete\', \'list\']">' +
              ' </actions-list>' +
              templatesQuery.balise +
              ' <column data-sortable="false"></column>' +
              ' <renderer>' +
              '  <actions' +
              '    class="inline-action"' +
              '    data-query-name="\'balise\'"' +
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
