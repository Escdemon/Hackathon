module.exports = (function (angular) { 
    'use strict';

    MonController.$inject = ['$scope', 'restService', 'entityModel'];

return {
    controller: MonController,
    template: require('./carte.template.html'),
    bindings: {}
};

function MonController($scope, restService, entityModel) {
    var $ctrl = this;
    $ctrl.rows = [];
    $ctrl.loadDatas = loadDatas;

    function loadDatas() {
        var entity = entityModel.entity('localisation');
        $ctrl.rows = [];
        restService.query(entity,'localisations').then(function (response) {
            response.data.results.forEach(function (localisation) {
                $ctrl.rows.push({x: localisation.T1_coordX, y: localisation.T1_coordY, statut: localisation.Val_T1_statut});
            });
        });
    }


    angular.element(document).ready(function () {
        // charger les données du back => elles sont toutes stockées ensuite dans $ctrl.rows
        $ctrl.loadDatas();
        console.log($ctrl.rows);
        
        var cases = new Array();
        var points = new Array();
        var map = new Array(600,600);
        var taille = new Array(25,50);
        var zoom = 1;        
        
        var canvas = document.getElementById("canvas");
          if (canvas.getContext) {
            var ctx = canvas.getContext("2d");
            
            // Quadrillage
            ctx.beginPath();
            for(var i=0; i<map[0]; i+=taille[0])
            {
                for(var j=0; j<map[1]; j+=taille[1])
                {
                    ctx.strokeRect(i, j, i+taille[0], j+taille[1]);
                    cases.push({x:i, y:j, status:""});
                }
            }
            ctx.closePath();
            ctx.stroke();
        }
        
        
         
�
        function getCase(x, y)
        {
            for(var z =0; z < cases.length; z++)
            {
                if(x == cases[z].x && y == cases[z].y)
                {
                    return cases[z];
                }
            }
            
            return null;
        }
    
        function getZone(x1, y1, x2, y2){
            var xMin = x1;
            var xMax = x2;    
            var yMin = y1;
            var yMax = y2;
            var temp = new Array();
            var tab = new Array();
            if(x1 > x2)
            {
                xMin = x2;
                xMax = x1;
            }
            if(y1 > y2)
            {
                yMin = y2;
                yMax = y1;
            }
            
            var xTemp = xMin;
            var yTemp = yMin;
            
            while(xTemp <= xMax || yTemp <= yMax)
            {
                temp.push(new Array(xTemp, yTemp));
                if (xTemp <= xMax)
                {
                    xTemp = parseInt(xTemp)+parseInt(taille[0]);
                }
                if (yTemp <= yMax)
                {
                    yTemp = parseInt(yTemp)+parseInt(taille[1]);
                }
            }
            
            for(var z =0; z < cases.length; z++)
            {
                for(var e = 0; e < temp.length; e++)
                {
                    if(cases[z].x == temp[e][0] && cases[z].y == temp[e][1] && cases[z].status == "")
                    {
                        tab.push(cases[z]);
        
                    }
                }
            }
            return tab;
        }
        
        function load()
        {
            $ctrl.loadDatas();

            if($ctrl.rows.length != 0 && points.length != 0)
            {
                var x_fin = points[points.length-1][0];
                var y_fin = points[points.length-1][1];
                var k = $ctrl.rows.length-1
                while($ctrl.rows[k].x != x_fin && $ctrl.rows[k].y != y_fin)
                {
                    draw($ctrl.rows[k].x, $ctrl.rows[k].y);
                    k--;
                }
            }
            else if ($ctrl.rows.length != 0)
            {
              points.push(new Array($ctrl.rows[$ctrl.rows.length-1].x,$ctrl.rows[$ctrl.rows.length-1].y,$ctrl.rows[$ctrl.rows.length-1].status));  
            }
          points.push(new Array(x_val,y_val,status));
            setTimeout('load',1000);
        }
        
        function draw(x_val, y_val)
        {
            var x_rec = Math.floor(x_val/taille[0])*taille[0];
              var y_rec = Math.floor(y_val/taille[1])*taille[1];
              var canvas = document.getElementById("canvas");
              if (canvas.getContext) {
                var ctx = canvas.getContext("2d");
                var origine = points[points.length-1];
                // Triangle filaire
                ctx.strokeStyle = 'rgb(75, 0, 130)';
                ctx.beginPath();
                ctx.moveTo(origine[0], origine[1]);
                ctx.lineTo(x_val, y_val);
                ctx.closePath();
                ctx.stroke();

                  var status = $("#stat").prop('checked');
                  var rec = getCase(x_rec, y_rec);
                  if(status == true && rec.status != "error")
                  {
                      ctx.fillStyle = 'rgb(200, 0, 0)';
                      ctx.fillRect(x_rec, y_rec, taille[0], taille[1]);
                      rec.status = "error";
                  }
                  else if(rec.status == "" || rec.status == "blank")
                  {
                      ctx.fillStyle = 'rgb(0, 200, 0)';
                      ctx.fillRect(x_rec, y_rec, taille[0], taille[1]);
                      rec.status = "success";
                  }
              }
        }
        
        load();
    });
}
}(window.angular));