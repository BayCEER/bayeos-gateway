
<%@ page import="gateway.Interval" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'interval.label', default: 'Interval')}" />
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
						
									<g:sortableColumn property="name" title="${message(code: 'interval.name.label', default: 'Name')}" />
							
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${intervalInstanceList}" var="intervalInstance">
						<tr>
						
							<td>${fieldValue(bean: intervalInstance, field: "name")}</td>
						
							<td class="link">
								<g:link action="edit" id="${intervalInstance.id}" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label" args="[entityName]"/></g:link>
								<g:link action="delete" id="${intervalInstance.id}" class="btn btn-sm btn-default" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span> <g:message code="default.button.delete.label" args="[entityName]"/></g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${intervalInstanceTotal}" />
				</div>
		</div>
		
	</body>
</html>
