<html>
<head>
	<meta name="layout" content="main" />
	<g:set var="entityName" value="${message(code: 'board.label', default: 'Board')}" />
	<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
	<div class="container">
		<g:hasErrors bean="${boardInstance}">
				<bootstrap:alert class="alert-danger">
				<ul>
					<g:eachError bean="${boardInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
		</g:hasErrors>
				

		<g:form class="form"  role="form" action="edit"
			id="${boardInstance?.id}">
			<g:hiddenField name="version" value="${boardInstance?.version}" />
			<g:hiddenField name="offset" value="${offset}" />
			<g:hiddenField name="group" value="${group}" />
			<div class="block">
				<div class="block-header">
					Board:${boardInstance.origin}
				</div>
				<div class="row">
					<div class="col-sm-6">
						<f:field bean="boardInstance" property="name" />
					</div>
					<div class="col-sm-6">
						<f:field bean="boardInstance" property="boardGroup" />
					</div>
				</div>
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
					<div class="col-sm-4">
						<f:field bean="boardInstance" property="checkDelay" label="Check Delay [sec]"/>
					</div>
					<div class="col-sm-4">
						<f:field bean="boardInstance" property="samplingInterval" label="Sampling Interval [sec]"/>
					</div>
					<div class="col-sm-4">
						<f:field bean="boardInstance" property="excludeFromNagios" />
					</div>
				</div>
								
				
			</div>
			<div class="block">
				<div class="block-header">Status</div>

				<div class="row">

					<div class="col-sm-5">
						<div class="row">
							<label class="col-sm-6 control-label">Validation:</label>
							<div class="col-sm-6">
								<div class="form-control-static">
									<nagios:statusMsg returnCode="${boardStatus?.status_valid}" />
								</div>
							</div>
						</div>
						<div class="row">
							<label class="col-sm-6 control-label">Completeness:</label>
							<div class="col-sm-6">
								<div class="form-control-static">
									<nagios:statusMsg returnCode="${boardStatus?.status_complete}" />
								</div>
							</div>
						</div>
					</div>


					<label class="col-sm-2 control-label">Last Result Time:</label>
					<div class="col-sm-2">
						<div class="form-control-static">
							<g:formatDate date="${boardStatus?.last_result_time}"
								format="dd.MM.yyyy HH:mm:ss z" />
						</div>
					</div>

					<label class="col-sm-1 control-label">RSSI:</label>
					<div class="col-sm-1">
						<xbee:rssiChart rssi="${boardInstance?.lastRssi}" />
					</div>
				</div>
			</div>


				
			


			<div class="block">
				<div class="block-header">Export</div>
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
			<div class="block">
				<div class="block-header">Channels</div>
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
								<td><g:link controller="channel" action="edit" id="${c.id}">
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
								
								<td class="link">
                         		<g:link controller="channel" action="edit" id="${c.id}" class="btn btn-xs btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label"/></g:link>
								<g:link controller="channel" action="delete" id="${c.id}" class="btn btn-xs btn-default" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span>
								<g:message code="default.button.delete.label"/></g:link>
								</td>

							</tr>
						</g:each>
					</tbody>
				</table>
			</div>

			<div class="block-action">
				<button type="submit" class="btn btn-primary">
					<span class="glyphicon glyphicon-ok"></span> <g:message
							code="default.button.update.label" default="Update" />
				</button>
				<button type="submit" class="btn btn-default" name="_action_remove" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
					formnovalidate>
					<span class="glyphicon glyphicon-trash"></span> <g:message
							code="default.button.delete.label" default="Delete" />
				</button>
				<g:link action="chart" params='[id:"${boardInstance?.id}"]'>
					<button type="button" class="btn btn-default">
						<span class="glyphicon glyphicon-stats"></span> Chart
					</button>
				</g:link>
				<div class="btn-group">
						<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"><span class="glyphicon glyphicon-hdd"></span> Template <span class="caret"></span>
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
 		$(document).ready(function(){
	 	<g:each in="${channelStati}" var="channel">	 	 
		 $('#nsw'+${channel.id}).on('switch-change', function(e, data) {
			 $.ajax({
				 url: '${createLink(controller:'channel', action: 'toggleNagios')}',          
		         dataType: "POST",	                  
		         data: "id=${channel.id}&enabled="+ data.value		               			 			 				 
		 		})
		 });
    </g:each>	 
});
    
</script>

</body>
</html>
