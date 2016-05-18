<%@ page import="gateway.Board" %>
<html>
  <head>            
    <meta name="layout" content="main"/>                   
  </head>
  <body> 
  <script src="${resource(dir:'javascripts',file:'jquery.flot.min.js')}" type="text/javascript"></script>
  <script src="${resource(dir:'javascripts',file:'jquery.flot.time.min.js')}" type="text/javascript"></script>
  <link rel="stylesheet" href="${resource(dir:'stylesheets',file:'font-awesome.min.css')}">
  
 

  
  <div class="hidden" id="timeOutWarning">
  	<bootstrap:alert class="alert-warning">Data loading stopped. Please reload the page.</bootstrap:alert>
  </div>
         
  <div class="row">       
   <div class="block">
    <div class="block-header">Data of Board: ${boardInstance.origin}</div>
     <div class="pull-right hidden" id="spinner">
    	<i class="fa fa-spinner fa-spin" style="font-size:24px"></i>
    </div>
    
    
    <div class="btn-group btn-group-default" role="group" aria-label="Selection">
    <button class="btn" id="allChannels" aria-label="Select All" data-toggle="tooltip" title="Select all channels"><span class="glyphicon glyphicon-ok"></span></button>    
    <button class="btn" id="noChannel" aria-label="Select None" data-toggle="tooltip" title="Delselect all channels"><span class="glyphicon glyphicon-remove"></span></button>
    </div>
    <table class="table">    
      <tr>      
        <th>Channel</th>
        <th>Value</th>
        <th>Quantity</th>         
        <th>Time</th>
      </tr>  
      <g:each in="${channels}" var="channel">
      <tr>
        <td class="cell${channel.id}"><input type="checkbox" checked="checked" name="${channel.id}" id="${channel.id}"/><label for="chk${channel.id}">${channel.nr}</label></td>                
        <td id="cv${channel.id}"/>
        <td>${channel?.label} [${channel?.unit}]</td>
        <td id="ct${channel.id}"/>
      </tr>
      </g:each>       
    </table>
    </div>
   </div> 
  
   <div class="row">
  		<div id="chart" class="col-sm-12" style="height:300px;">
  		</div>
   </div>
        
     
 <script type="text/javascript">
    var channels = ${channels.id};
    var colors = [];     
    var series = [];
               
    var refreshCounts;
    var interval = null;
    var lastRowId;
        
    var options = {
      lines: { show: true }, 
      points: { show: true } ,               
      xaxis: {mode: "time",  timeformat: "%H:%M:%S\n%d.%m",timezone: "browser", zoomRange: [0.1, 10], panRange: [-10, 10] },
      yaxis: { zoomRange: [0.1, 10], panRange: [-10, 10] }     
    };

    $('#allChannels').click(function(){
    	$(".block").find("input").prop("checked",true);
    	drawChart();
    });

	$('#noChannel').click(function(){
		$(".block").find("input").prop("checked",false);
		drawChart();        
    });


	$(".block").find("input").change( function() {
		drawChart();
	});


	function drawChart(){
		 // Plot series according to choices 
        var points = [];                          
        $(".block").find("input:checked").each(function () {
          var key = $(this).attr("id");
          points.push({data:series[key], color:colors[parseInt(key)]});            
        }); 
        var plot = $.plot($("#chart"),points,options);     
        $("#spinner").addClass("hidden")
	}


    function fetchData() {
	 	// Show a message and stop loading after 10 min         
	 	if (++refreshCounts > 60){
	 	 		clearInterval(interval)
	 	 		$("#timeOutWarning").removeClass("hidden") 	 		
	 	 		return;
	 	}

 		$("#spinner").removeClass("hidden")
	    $.ajax(
	        {
	          url: '${createLink(controller: 'board', action: 'chartData')}',          
	          dataType: "json",
	          cache: false,            
	          data: "lastRowId="+lastRowId+"&boardId="+${boardInstance.id} ,          
	          timeout: 5000, 
	          success: function(data){    
	            lastRowId = data.lastRowId;  	        
	            $.each(data.observations, function(key, val) {              
	              $('#cv' + val.cid).html(val.value);                       
	              $('#ct' + val.cid).html(getDateString(new Date(val.millis)));
	              var s = series[val.cid];
	              s.push([val.millis,val.value]);              
	            });
	            drawChart();                                          	            
	          },
	         }) // ajax end 
    } // fetch Data end 

    
    $(document).ready(function(){

     refreshCounts = 0;
      
     // Init arrays for data 
     for(var i = 0;i<channels.length;++i){       
       series[channels[i]] = [];     
     }
               
     dummy = [];
     for(var i = 0;i<channels.length;++i){       
       dummy.push([]);
     }
          
     // Plot empty chart to get some colors !
     var p = $.plot($("#chart"),dummy,options);  
     var d = p.getData();              
     for(var i = 0;i<channels.length;++i){       
       colors[channels[i]] = d[i].color;
       $(".cell" + channels[i]).css("background-color",d[i].color);
     }
     
     fetchData()
     interval = setInterval(fetchData,10000);
   
    });      
    </script>    
  
  </body>
</html>
