<%@ page import="gateway.Board" %>
<html>
  <head>            
    <meta name="layout" content="main"/>                   
  </head>
  <body> 
  <script src="${resource(dir:'js/flot',file:'jquery.flot.min.js')}" type="text/javascript"></script>
  <script src="${resource(dir:'js/flot',file:'jquery.flot.time.js')}" type="text/javascript"></script>
  
  
				

          
  <div class="container">
  
  <div class="hidden" id="timeOutWarning">
  	<bootstrap:alert class="alert-warning">Data loading stopped. Please reload the page.</bootstrap:alert>
  </div>
                
   <div class="block">
    <div class="block-header">Data of Board: ${boardInstance.origin}</div>
    <table class="table">    
      <tr>      
        <th>Channel</th>
        <th>Value</th>
        <th>Quantity</th>         
        <th>Time</th>
      </tr>  
      <g:each in="${channels}" var="channel">
      <tr>
        <td class="cell${channel.nr}"><input type="checkbox" checked="checked" name="${channel.nr}" id="chk${channel.nr}"/><label for="chk${channel.nr}">Nr. ${channel.nr}</label></td>                
        <td id="cv${channel.nr}" />
        <td>${channel?.label} [${channel?.unit?.abbrevation}]</td>
        <td id="ct${channel.nr}"/>
      </tr>
      </g:each>       
    </table>     
  <div id="chart" class="col-sm-12" style="height:300px;">   
  </div>
   
  </div>  
 </div>   
     
 <script type="text/javascript">
    var channels = ${channelList}; 
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
     var colors = [];
          
     for(var i = 0;i<channels.length;++i){       
       colors[channels[i]] = d[i].color;
       $(".cell" + channels[i]).css("background-color",d[i].color);
     }
     
                       
        
     function fetchData() {
 	 	// Show a message and stop loading after 10 min         
 		if (++refreshCounts > 60){
 	 		clearInterval(interval)
 	 		$("#timeOutWarning").removeClass("hidden") 	 		
 	 		return;
 	 	}
         
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
              $('#cv' + val.ch).html(val.value);                       
              $('#ct' + val.ch).html(new Date(val.millis).toString());
              var s = series[val.ch];
              s.push([val.millis,val.value]);              
            });

            // Plot series according to choices 
            var points = [];                          
            $(".block").find("input:checked").each(function () {
              var key = $(this).attr("name");
              points.push({data:series[key], color:colors[parseInt(key)]});            
            }); 
            var plot = $.plot($("#chart"),points,options);                                                
            
          },
         }) // ajax end 
       } // fetch Data end 

       fetchData()
       interval = setInterval(fetchData,10000);
   
    });      
    </script>    
  
  </body>
</html>
