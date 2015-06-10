<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'board.label', default: 'Board')}" />
<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
	<div class="container">
		<g:hasErrors bean="${boardInstance}">
			<bootstrap:alert class="alert-danger">
				<ul>
					<g:eachError bean="${boardInstance}" var="error">
						<li
							<g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
								error="${error}" /></li>
					</g:eachError>
				</ul>
			</bootstrap:alert>
		</g:hasErrors>




		<g:form class="form" role="form" action="save"
			id="${boardInstance?.id}">			

			<div class="block">

				<div class="block-header">
					Origin:${boardInstance.origin}
				</div>

				<div class="row">
					<div class="col-sm-4">
						<f:field bean="boardInstance" property="name" />
					</div>
					<div class="col-sm-4">
						<f:field bean="boardInstance" property="boardGroup" />
					</div>
					<div class="col-sm-4">
						<f:field bean="boardInstance" property="samplingInterval"
							label="Sampling Interval [sec]" />
					</div>
				</div>


				<!--  Tabs Start -->
				<div role="tabpanel">
					<!-- Nav tabs -->
					<ul class="nav nav-tabs" role="tablist" id="myTab">
						<li role="presentation"><a href="#channels"	aria-controls="channels" role="tab" data-toggle="tab">Channels</a>
						</li>
						<li role="presentation"><a href="#settings"	aria-controls="settings" role="tab" data-toggle="tab">Settings</a>
						</li>
						<li role="presentation"><a href="#messages"	aria-controls="messages" role="tab" data-toggle="tab">Messages
								<span class="badge" id="msgBadge"></span>
						</a></li>
						<li role="presentation"><a href="#comments" aria-controls="comments" role="tab" data-toggle="tab">Comments <span class="badge" id="comBadge">${boardInstance.comments.size()}</span></a>
						</li>
					</ul>

					<!-- Tab panes -->
					<div class="tab-content">
						<div role="tabpanel" class="tab-pane fade in" id="channels">
							<table class="table table-hover">
								<thead>
									<tr>
										<th>Channel</th>
										<th>Last Value</th>
										<th>Label [Unit]</th>
										<th>Last Result Time</th>
										<th>Nagios</th>
										<th></th>
									</tr>
								</thead>
								<tbody>
									<g:each in="${channelStati}" var="c">
										<tr>
											<td><g:link controller="channel" action="edit"
													id="${c.id}">
													${c?.nr}
												</g:link></td>
											<nagios:statusCell returnCode="${c?.status_valid}"
												tip="${c?.status_valid_msg}">
												${c?.last_result_value}
											</nagios:statusCell>
											<td>
												${c?.label} [${c?.unit}]
											</td>
											<nagios:statusCell returnCode="${c?.status_complete}"
												tip="${c?.status_complete_msg}">
												<g:formatDate date="${c?.last_result_time}"
													format="dd.MM.yyyy HH:mm:ss z" />
											</nagios:statusCell>

											<td>
												<div class="make-switch switch-mini" id="nsw${c.id}">
													<g:checkBox type="checkbox" name="${c.id}"
														checked="${c.nagiosOn}" />
												</div>
											</td>

											<td class="link"><g:link controller="channel"
													action="edit" id="${c.id}" class="btn btn-xs btn-default">
													<span class="glyphicon glyphicon-edit"></span>
													<g:message code="default.button.edit.label" />
												</g:link> <g:link controller="channel" action="delete" id="${c.id}"
													class="btn btn-xs btn-default"
													onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
													<span class="glyphicon glyphicon-remove"></span>
													<g:message code="default.button.delete.label" />
												</g:link></td>

										</tr>
									</g:each>
								</tbody>
							</table>

						</div>

						<div role="tabpanel" class="tab-pane fade" id="messages">

							<table id="messages" class="table table-condensed col-sm-12">
								<thead>
									<tr>
										<th class="col-sm-1">Type</th>
										<th class="col-sm-2">Time</th>
										<th class="col-sm-7">Content</th>
										<th class="col-sm-2"></th>
									</tr>
								</thead>
								<tbody>
								</tbody>
							</table>
							<div class="pagination">
								<a id="msgBackward" class="btn btn-default"
									role="button" href="#"></span>«</a> 
								<a id="msgForward"	class="btn btn-default" role="button" href="#"></span>»</a>
							</div>
						</div>
						<div role="tabpanel" class="tab-pane fade" id="settings">
							<div class="block">
								<div class="block-header">Checks</div>
								<div class="row">
									<div class="col-sm-6">
										<f:field bean="boardInstance" property="criticalMin" />
									</div>
									<div class="col-sm-6">
										<f:field bean="boardInstance" property="criticalMax" />
									</div>
								</div>
								<div class="row">
									<div class="col-sm-6">
										<f:field bean="boardInstance" property="warningMin" />
									</div>
									<div class="col-sm-6">
										<f:field bean="boardInstance" property="warningMax" />
									</div>
								</div>
								<div class="row">
									<div class="col-sm-6">
										<f:field bean="boardInstance" property="checkDelay"
											label="Check Delay [sec]" />
									</div>
									<div class="col-sm-6">
										<f:field bean="boardInstance" property="excludeFromNagios" />
									</div>
								</div>
							</div>

							<div class="block">
								<div class="block-header">Exports</div>
								<div class="row">
									<div class="col-sm-6">
										<f:field bean="boardInstance" property="dbFolderId" />
									</div>
									<div class="col-sm-6">
										<f:field bean="boardInstance" property="dbAutoExport"
											label="Auto Export" />
									</div>
								</div>
							</div>
						</div>
						
						
						
						
						<div role="tabpanel" class="tab-pane fade" id="comments">													
								<g:render template="/board/comment_list"/>						
						</div>







			</div>




			<div class="block-action">
				<button type="submit" class="btn btn-primary">	
					<span class="glyphicon glyphicon-save"></span>
					<g:message code="default.button.save.label" default="Save" />										
				</button>
				<button type="submit" class="btn btn-default" name="_action_remove"
					onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
					formnovalidate>
					<span class="glyphicon glyphicon-trash"></span>
					<g:message code="default.button.delete.label" default="Delete" />
				</button>
				<g:link action="chart" params='[id:"${boardInstance?.id}"]'>
					<button type="button" class="btn btn-default">
						<span class="glyphicon glyphicon-stats"></span> Chart
					</button>
				</g:link>
				<div class="btn-group">
					<button type="button" class="btn btn-default dropdown-toggle"
						data-toggle="dropdown">
						<span class="glyphicon glyphicon-hdd"></span> Template <span
							class="caret"></span>
					</button>
					<ul class="dropdown-menu">
						<li><g:link action="editTemplate"
								params='[id:"${boardInstance?.id}"]'>Apply Template</g:link></li>
						<li><g:link action="saveAsTemplate"
								params='[id:"${boardInstance?.id}"]'>Save as Template</g:link></li>
					</ul>
				</div>

			</div>


		</g:form>


	</div>

	<script type="text/javascript">

		var rowsTotal = 0;
		var max = 10;		
		var step = 0
	 
 		$(document).ready(function(){
 	 		
 	 	// Nagios toggle 
	 	<g:each in="${channelStati}" var="channel">	 	 
		 $('#nsw'+${channel.id}).on('switch-change', function(e, data) {
			 $.ajax({
				 url: '${createLink(controller:'channel', action: 'toggleNagios')}',          
		         dataType: "POST",	                  
		         data: "id=${channel.id}&enabled="+ data.value		               			 			 				 
		 		})
		 });
        </g:each>

     	// Select tab by name
        $('#myTab a[href="#channels"]').tab('show')        					   	   		

		$("#msgBackward").bind("click", msgBackward);
		$("#msgForward").bind("click", msgForward);

		function setPager(){
			if (rowsTotal < max){
				$("#msgForward").addClass("disabled")
				$("#msgBackward").addClass("disabled")
			} else {
				// Backward 
				if (step > 0){
					$("#msgBackward").removeClass("disabled")
				} else {
					$("#msgBackward").addClass("disabled")
				}
				// Forward 
				if ((step + 1)*max >= rowsTotal ){
					$("#msgForward").addClass("disabled")
				} else {
					$("#msgForward").removeClass("disabled")
				}				
			}
		}

		function msgForward(){
			step++;					
			fetchMsg(max,step*max);
			setPager()						
		}

		function msgBackward(){
			step--;				
			fetchMsg(max, step*max);
			setPager()						
		}


		function fetchMsg(max, offset) {	
			$.ajax(
			        {
			          url: '${createLink(controller: 'message', action: 'getMessagesByOrigin')}',          
			          dataType: "json",
			          cache: false,            
			          data: 'origin=${boardInstance.origin}&max='+max+'&offset='+ offset,  
			          timeout: 5000, 
			          success: function(data){ 
			        	  $("#messages tbody tr").remove()	
			        	  rowsTotal = data.count;
			        	  $('#msgBadge').text(rowsTotal)			        	 		        	  			        	 
			        	  $.each(data.messages, function(key, val) {
			        		  var cellType;
				        	  if (val.type == "ERROR"){
				        		  cellType = "danger";  
					          } else {
					        	  cellType = "info";
						      }				   

				        	       	  		        					        	     
			        		  $("#messages tbody").append(			        				  
			        					"<tr id=\"row"  + val.id + "\">" +
			        					"<td class=\"" + cellType + "\">" + val.type + "</td>" + 			        					   							        		  			        								        					
			        					"<td>" + new Date(val.result_time).toLocaleString()+ "</td>" +			        					
			        					"<td>" + val.content + "</td>" +
			        					"<td><a id=\"row\" rowId=\"" + val.id + "\" class=\"btn btn-xs btn-default msgDelete\" role=\"button\">Delete <span class=\"glyphicon glyphicon-remove\"></span></a></td>" +		
			        					"</tr>");								
			              });			        	  
			      		  $(".msgDelete").click(function() {
				      		var id = $(this).attr("rowId")				      		
			      			console.log("click:" + id);			      			
			      			$.ajax({
			      				url: '${createLink(controller: 'message', action: 'deleteMessage')}',          
						          dataType: "json",
						          cache: false,            
						          data: "id="+id,  
						          timeout: 5000, 
					      	});				 
			      			$('#row' + id).remove();
			      			$('#msgBadge').text(--rowsTotal)
			      		});
			      		setPager();
				     }
			        });
		}
		
		fetchMsg(10,0);
		
        

         	 
});
    
</script>

</body>
</html>
