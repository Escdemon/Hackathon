$( document ).ready(function(){
    var cases = new Array();
    var points = new Array();
    var map = new Array(600,600);
    var taille = new Array(5,10);
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
    
    ctx.fillRect(240, 240, 5, 10);
      }
    
    $("#valid_can").on("click", function(event){
        event.preventDefault();
      var x_val = $("#x").val();
      var y_val = $("#y").val();
      if(points.length != 0)
      {
        var x_rec = Math.floor(x_val/taille[0])*taille[0];
        var y_rec = Math.floor(y_val/taille[1])*taille[1];
  //      alert(x_rec);
  //      alert(y_rec);
  //      alert(y_val);
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
    //            setStatus(rec);
            }
            else if(rec.status == "" || rec.status == "blank")
            {
                ctx.fillStyle = 'rgb(0, 200, 0)';
                ctx.fillRect(x_rec, y_rec, taille[0], taille[1]);
                rec.status = "success";
    //            setStatus(rec);
            }

//            var zone = getZone(x_rec, y_rec, origine[0], origine[1]);
//            for(var a = 0; a < zone.length ; a++)
//            {
//                ctx.fillStyle = 'rgb(186, 186, 186)';
//                ctx.fillRect(zone[a].x, zone[a].y, taille[0], taille[1]);
//                zone[a].status = "blank";            
//            }
        }
    }
      points.push(new Array(x_val,y_val,status));    
    });
    
     
// Exemple d'utilisation :
 
$("#canvas").on("click", function(e){
  var coords = getCoords(this,e);
  var quartX = Math.floor(coords.x/(map[0]/2));
  var quartY =Math.floor(coords.y/(map[1]/2));
  alert("Clic -> X:"+coords.x+" - Y:"+coords.y);
});

 
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

function getZone(x1, y1, x2, y2)
{
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

});

function getCoords(el,event) {
  var ox = -el.offsetLeft,
  oy = -el.offsetTop;
  while(el=el.offsetParent){
    ox += el.scrollLeft - el.offsetLeft;
    oy += el.scrollTop - el.offsetTop;
  }
  return {x:event.clientX + ox , y:event.clientY + oy};
}
