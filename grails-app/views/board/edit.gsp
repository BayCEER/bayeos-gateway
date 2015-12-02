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
					<div class="col-sm-3">
						<f:field bean="boardInstance" property="name" />
					</div>
					<div class="col-sm-3">
						<f:field bean="boardInstance" property="boardGroup" />
					</div>
					<div class="col-sm-3">
						<f:field bean="boardInstance" property="samplingInterval"
							label="Sampling Interval [sec]" />
					</div>
					<div class="col-sm-3">
						<f:field bean="boardInstance" property="denyNewChannels"
							label="Deny New Channels" />
					</div>
				</div>


				<!--  Tabs Start -->
				<div role="tabpanel">
					<!-- Nav tabs -->
					<ul class="nav nav-tabs" role="tablist" id="myTab">
						<li role="presentation"><a href="#channels"
							aria-controls="channels" role="tab" data-toggle="tab">Channels</a>
						</li>
						<li role="presentation"><a href="#settings"
							aria-controls="settings" role="tab" data-toggle="tab">Settings</a>
						</li>
						<li role="presentation"><a href="#messages"
							aria-controls="messages" role="tab" data-toggle="tab">Messages
								<span class="badge" id="msgBadge"></span>
						</a></li>
						<li role="presentation"><a href="#comments"
							aria-controls="comments" role="tab" data-toggle="tab">Comments
								<span class="badge" id="comBadge">
									${boardInstance.comments.size()}
							</span>
						</a></li>
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
													format="dd.MM.yyyy HH:mm:ss Z" />
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

							<table id="tab-messages" class="table table-condensed col-sm-12">
								<thead>
									<tr>
										<th class="col-sm-1">Type</th>
										<th class="col-sm-3">Time</th>
										<th class="col-sm-8">Content</th>										
									</tr>
								</thead>
								<tbody>
								</tbody>
							</table>							
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
							<g:render template="/board/comment_list" />
						</div>







					</div>




					<div class="block-action">
						<button type="submit" class="btn btn-primary">
							<span class="glyphicon glyphicon-save"></span>
							<g:message code="default.button.save.label" default="Save" />
						</button>
						<button type="submit" class="btn btn-default"
							name="_action_remove"
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
        $('#myTab a[href="#channels"]').tab('show');
                     
        $('#tab-messages').DataTable( {
 				"responsive": true,
        		"autoWidth": false,
                "processing": true,
                "serverSide": true,
                "lengthChange": false,
                "searching": false,
                "rowId": 'id',                                    
                "columns": [
                    {data: 'type'},
                    {data: 'result_time'},
                    {data: 'content'}
                ],
                "ajax": {
                    "url": '${createLink(controller:'message', action:'listData')}',
                    "data": function (d) {
                    	d.origin="${boardInstance.origin}"
                     }                        
                },
                "columnDefs": [
                    // https://datatables.net/reference/option/columns.render
                    // Type
            		{ "targets": 0 , "render":
            			function ( data, type, row , meta) {
            				 var cellType;
			        		 if (row.type == "ERROR"){
			        			return "<td><span class=\"text-danger\"/>" + row.type + "</span></td>";  
				          	 } else {
				          		return "<td>" + row.type + "</td>";
					      	 }				          			            				             			            			                    		
                		}
            		},
            		// Result Time
            		{ "targets": 1 , "render":
            			function ( data, type, row, meta ) {
            				return "<td>" + getDateString(new Date(row.result_time)) + "</td>";	
                		}
            		},

            		// Content
            		{ "targets": 2, "render":
            			function ( data, type, row, meta ) {
            				return "<td>" + row.content + "</td>" ;
            			
                		}
            		}

        		],
        		"initComplete": function () {
        			$('#msgBadge').text(this.api().page.info().recordsTotal);
        		}
        });
        
         	 
});

		 	
</script>

</body>
</html>
