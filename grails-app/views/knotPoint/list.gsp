
<%@ page import="gateway.KnotPoint" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'knotPoint.label', default: 'KnotPoint')}" />
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
				</div>
			
				<h3><g:message code="default.list.label" args="[entityName]" /></h3>
		
				<table class="table table-striped">
					<thead>
						<tr>
						
									<th class="header"><g:message code="knotPoint.spline.label" default="Spline" /></th>
							
									<g:sortableColumn property="x" title="${message(code: 'knotPoint.x.label', default: 'X')}" />
							
									<g:sortableColumn property="y" title="${message(code: 'knotPoint.y.label', default: 'Y')}" />
							
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${knotPointInstanceList}" var="knotPointInstance">
						<tr>
						
							<td>${fieldValue(bean: knotPointInstance, field: "spline")}</td>
						
							<td>${fieldValue(bean: knotPointInstance, field: "x")}</td>
						
							<td>${fieldValue(bean: knotPointInstance, field: "y")}</td>
						
							<td class="link">
								<g:link action="edit" id="${knotPointInstance.id}" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label" args="[entityName]"/></g:link>
								<g:link action="delete" id="${knotPointInstance.id}" class="btn btn-sm btn-default" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span> <g:message code="default.button.delete.label" args="[entityName]"/></g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${knotPointInstanceTotal}" />
				</div>
		</div>
		
	</body>
</html>
