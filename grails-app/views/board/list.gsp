
<html>
    <head>
    	<title>Boards</title>            
        <meta name="layout" content="main" />                            
    </head>
    <body>   
    	<div class="container">
    	<g:form controller="board" action="list" class="form-inline" role="form">
    	 	<div class="form-group">
    	 		<label>Group:</label>
    	 		<g:select name="group" from="${groups}" value="${group}" onchange="submit()"/>
    	 	</div>
    	</g:form>	
            
            		 	                 
      	<div class="row">                  
      	               	        	                   	            
        <table class="table table-hover col-sm-12">
        <thead>        	                        
                <tr>                                                                            
                   <th>Origin</th>                   
                   <th class="hidden-xs">Name</th>
                   <th class="hidden-xs">Rssi</th>
                   <th>Last Result Time</th>
                   <th>Status</th>
                   <th class="hidden-xs">Nagios</th>
                   </tr>
                    </thead>
                    <tbody>
                    <g:each in="${result}" status="i" var="res">
                        <tr>                        
                            <td><g:link action="edit" id="${res.id}">${res.origin}</g:link></td>                            
                            <td class="hidden-xs">${res.name}</td>
                            <td class="hidden-xs"><xbee:rssiChart rssi="${res.last_rssi}"/></td>
                            <td><g:formatDate date="${res.last_result_time}" format="dd.MM.yyyy HH:mm:ss z"/></td>                                                        
                            <td><nagios:statusMsg returnCode="${res.nagiosStatus}"/></td>
                            <td class="hidden-xs">
                            <div class="make-switch switch-mini" id="sw${res.id}" data-on="${res.nagiosChannelOff>0? 'warning':'success'}"
                      	 			data-toggle="tooltip"
                      	 			title="${res.nagiosChannelOff} channels excluded">                       	
                      	     		<g:checkBox type="checkbox" name="${res.id}"  checked="${res.nagiosOn==true}"/>
							</div>
							</td>
                            
                            <td class="link">
                                                         	                            	
                            	<g:link action="edit" id="${res.id}" class="btn btn-xs btn-default">
                            		<span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label"/>
                            	</g:link>
                            	<g:link action="chart" id="${res.id}" class="btn btn-xs btn-default">
                            		<span class="glyphicon glyphicon-stats"></span> Chart
                            	</g:link>
                            	
                            	<div class="btn-group">
  									<button class="btn btn-default btn-xs dropdown-toggle" type="button" data-toggle="dropdown">
  									<span class="glyphicon glyphicon-save"></span> Export <span class="caret"></span>
  									</button>
  									<ul class="dropdown-menu">
  										<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'Today',header:true]">Today</g:link></li>
    									<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'Last24h',header:true]">Last24h</g:link></li>
    									<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'Yesterday',header:true]">Yesterday</g:link></li>
    									<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'ThisWeek',header:true]">ThisWeek</g:link></li>
    									<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'LastWeek',header:true]">LastWeek</g:link></li>
    									<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'ThisMonth',header:true]">ThisMonth</g:link></li>
    									<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'LastMonth',header:true]">LastMonth</g:link></li>
    									<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'ThisYear',header:true]">ThisYear</g:link></li>
    									<li><g:link action="rawData" params="[origin:"${res.origin}", format:'xls',timeInterval:'LastYear',header:true]">LastYear</g:link></li>    									
  									</ul>
								</div>
                            	
                            	<g:link action="remove" id="${res.id}" class="btn btn-xs btn-default" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">                            	
									<span class="glyphicon glyphicon-remove"></span> <g:message code="default.button.delete.label"/>
								</g:link>	
                            </td>
                        </tr>
                    </g:each>
                    </tbody>
                </table> 
                <div class="pagination">                       
            		<bootstrap:paginate total="${total}" params="[group:"${group}"]"/>
            	</div>
            </div>
        </div>
            
       
        <script type="text/javascript"> 
 		$(document).ready(function(){
	 	<g:each in="${result}" var="board">	 	 
		 $('#sw'+${board.id}).on('switch-change', function(e, data) {
			 $.ajax({
				 url: '${createLink(controller:'board', action: 'toggleNagios')}',          
		         dataType: "POST",	                  
		         data: "id=${board.id}&enabled="+ data.value		               			 			 				 
		 		})
		 });
    </g:each>	 
});
    
</script> 
            
            
    </body>
</html>
