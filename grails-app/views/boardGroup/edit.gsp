<%@ page import="gateway.BoardGroup"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'boardGroup.label', default: 'BoardGroup')}" />
<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
	<validation bean="boardGroupInstance"> 	
	<g:form	class="form" action="edit" id="${boardGroupInstance?.id}">
		<div class="form-group block-action">
			<div class="block">
				<div class="block-header">Board Group</div>
				<div class="row">
					<div class="col-sm-6">
						<f:field bean="${boardGroupInstance}" property="name" />
					</div>
				</div>
			</div>

			<div class="block">
				<div class="block-header">Boards</div>
				<table class="table table-hover nowrap">
							
				
				<g:link action="addBoard"
					class="btn btn-xs btn-default"
					id="${boardGroupInstance.id}"
					><span class="glyphicon glyphicon-plus"></span>
					<g:message code="default.button.add.label" />
				</g:link>
					<thead>
						<tr>
							<th class="min-tablet-l">Origin</th>
							<th>Name</th>
							<th class="min-tablet-l">Rssi</th>
							<th>Last Result Time</th>
							<th>Status</th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${boardStatus}" var="bs">
							<tr>
								<td><g:link controller="board" action="edit" id="${bs.id}">${bs.origin}</g:link></td>
								<td><g:link controller="board" action="edit" id="${bs.id}">${bs.name}</g:link></td>
								<td><xbee:rssiChart rssi="${bs.last_rssi}"/></td>
								<td><g:formatDate date="${bs.last_result_time}"	format="dd.MM.yyyy HH:mm:ss Z" /></td>
								<td><nagios:statusMsg returnCode="${bs.status}"/></td>
								<td>
								<g:link	data-toggle="tooltip" title="Removes board from group." onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" 
								action="removeBoard" id="${bs.id}"  class="btn btn-xs btn-default">
									<span class="glyphicon glyphicon-remove"></span>
									<g:message code="default.button.remove.label"/>
									</g:link> 
								</td>
								
							</tr>
							
							
							
						</g:each>
					</tbody>
				</table>
			</div>

		<div class="block-action">
			<button type="submit" class="btn btn-primary">
				<span class="glyphicon glyphicon-save"></span>
				<g:message code="default.button.ok.label" default="Save" />
			</button>
			<button type="submit" class="btn btn-default" name="_action_delete"
				onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
				formnovalidate>
				<span class="glyphicon glyphicon-trash"></span>
				<g:message code="default.button.delete.label" default="Delete" />
			</button>
		</div>
	</g:form> 
	<span class="help-block"><g:message	code="default.form.asterisk-hint" />(<span style="color: #f00;">*</span>)</span>
	</validation>	
</body>
</html>
