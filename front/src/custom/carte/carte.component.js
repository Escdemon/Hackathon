module.exports = (function (angular) { 
    'use strict';

    MonController.$inject = ['$scope', 'restService', 'entityModel'];

return {
    controller: MonController,
    template: require('./carte.template.html'),
    bindings: {
        ngModel: '=' 
    }
};

function MonController($scope, restService, entityModel) {
    var $ctrl = this;
    $ctrl.rows = [];
    $ctrl.points = [];
    $ctrl.loadDatas = loadDatas;
    var ratio = 20;
    var deltaX = 300;
    var deltaY = 300;

    $scope.photo = undefined;


    function loadDatas() {
        var entity = entityModel.entity('localisation');
        
        restService.query(entity,'localisations',{length:10000}).then(function (response) {
            $ctrl.rows = [];
            response.data.results.forEach(function (localisation) {
                $ctrl.rows.push({x: localisation.T1_coordX, y: localisation.T1_coordY, statut: localisation.Val_T1_statut});
            });
        });
    }


    angular.element(document).ready(function () {
        
        var cases = new Array();
        var points = new Array();
        var map = new Array(600,600);
        var taille = new Array(25,50);
        var zoom = 1;
        var loop = 0;      
        
        var canvas = document.getElementById("canvas");
          if (canvas.getContext) {
            var ctx = canvas.getContext("2d");
            
            // Quadrillage
            ctx.beginPath();
            for(var i=0; i<map[0]; i+=taille[0])
            {
                for(var j=0; j<map[1]; j+=taille[1])
                {
                    ctx.strokeStyle="white";
                    ctx.strokeRect(i, j, i+taille[0], j+taille[1]);
                    cases.push({x:i, y:j, status:""});
                }
            }
            ctx.closePath();
            ctx.stroke();
            
        }
        
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
        
        function loadArray()
        {
            $ctrl.loadDatas();
            if($ctrl.rows.length != 0 && points.length != 0)
            {
                var x_fin = points[points.length-1][0];
                var y_fin = points[points.length-1][1];
                var k = $ctrl.rows.length-1
                while($ctrl.rows[k] && $ctrl.rows[k].x != x_fin && $ctrl.rows[k].y != y_fin)
                {
                    var drawX = (200 + $ctrl.rows[k].y/ratio);
                    var drawY = (200 + $ctrl.rows[k].x/ratio);
                    draw(drawX, drawY, $ctrl.rows[k].statut); 
                    k--;
                }
            }
            else if ($ctrl.rows.length != 0)
            {
              points.push(new Array($ctrl.rows[$ctrl.rows.length-1].x/ratio,$ctrl.rows[$ctrl.rows.length-1].y/ratio,$ctrl.rows[$ctrl.rows.length-1].statut));  
            }
        }
        
        function draw(x_val, y_val, status)
        {
              var x_rec = Math.floor(x_val/taille[0])*taille[0];
              var y_rec = Math.floor(y_val/taille[1])*taille[1];
              var canvas = document.getElementById("canvas");
              if (canvas && canvas.getContext) {
                var ctx = canvas.getContext("2d");
                var origine = points[points.length-1];
            

                  var rec = getCase(x_rec, y_rec);

                  if (rec){
                    if(status == false && rec.status != "error")
                    {
                        ctx.shadowColor = 'black';
                        ctx.shadowBlur = 10;
                        ctx.fillStyle = 'rgb(200, 0, 0)';
                        ctx.fillRect(x_rec+1, y_rec+1, taille[0]-2, taille[1]-2);
                        rec.status = "error";
                        easter();
                    }
                    else if(rec.status == "" || rec.status == "blank")
                    {
                        ctx.shadowColor = 'black';
                        ctx.shadowBlur = 10;
                        ctx.fillStyle = 'rgb(0, 200, 0)';
                        ctx.fillRect(x_rec+1, y_rec+1, taille[0]-2, taille[1]-2);
                        rec.status = "success";
                    }
                  }

              }
        }

        function easter(){
            loop = loop + 1 ;
            if (loop > 3){
                $("div.easter img").addClass("bouh");
            }
        }
        
        loadArray();
        var intervalID = setInterval(function(){loadArray();}, 200);
        
    });
}
}(window.angular));