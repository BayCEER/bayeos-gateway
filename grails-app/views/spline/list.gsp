
<%@ page import="gateway.Spline" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code:'spline.label', default: 'Spline')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">										
				<div class="btn-group">
					<g:link action="list" class="btn btn-sm btn-default active">
						<span class="glyphicon glyphicon-list"></span> <g:message code="default.button.list.label" args="[entityName]"/>						
					</g:link>
					<g:link action="create" class="btn btn-sm btn-default">
						<span class="glyphicon glyphicon-plus"></span> <g:message code="default.button.create.label" args="[entityName]"/>
					</g:link>
					<g:link action="upload" class="btn btn-sm btn-default">
						<span class="glyphicon glyphicon-upload"></span> <g:message code="default.button.upload.label" args="[entityName]"/>						
					</g:link>							
				</div>
			
				<h3><g:message code="default.list.label" args="[entityName]" /></h3>
		
				<table class="table table-striped">
					<thead>
						<tr>
						
								<g:sortableColumn property="name" title="${message(code: 'spline.name.label', default: 'Name')}" />
							
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${splineInstanceList}" var="splineInstance">
						<tr>
							
							<td><g:if test="${splineInstance.locked}">
									<span class="glyphicon glyphicon-lock" title="Spline is locked. Editing not allowed."></span>								
							</g:if>${splineInstance.name}</td>
						
							<td class="link">
								<g:if test="${!splineInstance.locked}">
								<g:link action="edit" id="${splineInstance.id}" class="btn btn-xs btn-default">
									<span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label" args="[entityName]"/></g:link>
								<g:link action="delete" id="${splineInstance.id}" class="btn btn-xs btn-default" 
								onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span> <g:message code="default.button.delete.label" args="[entityName]"/></g:link>															
								</g:if>
								
								
								<g:link controller="spline" action="export" class="btn btn-xs btn-default" id="${splineInstance?.id}">
								<span class="glyphicon glyphicon-save"></span> Export</g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${splineInstanceTotal}" />
				</div>
		</div>
		
	</body>
</html>
