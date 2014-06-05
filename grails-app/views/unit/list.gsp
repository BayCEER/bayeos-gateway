
<%@ page import="gateway.Unit" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'unit.label', default: 'Unit')}" />
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
						
									<g:sortableColumn property="name" title="${message(code: 'unit.name.label', default: 'Name')}" />
							
									<g:sortableColumn property="abbrevation" title="${message(code: 'unit.abbrevation.label', default: 'Abbrevation')}" />
							
									<g:sortableColumn property="dbUnitId" title="${message(code: 'unit.dbUnitId.label', default: 'Db Unit Id')}" />
							
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${unitInstanceList}" var="unitInstance">
						<tr>
						
							<td>${fieldValue(bean: unitInstance, field: "name")}</td>
						
							<td>${fieldValue(bean: unitInstance, field: "abbrevation")}</td>
						
							<td>${fieldValue(bean: unitInstance, field: "dbUnitId")}</td>
						
							<td class="link">
								<g:link action="edit" id="${unitInstance.id}" class="btn btn-xs btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label" args="[entityName]"/></g:link>
								<g:link action="delete" id="${unitInstance.id}" class="btn btn-xs btn-default" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span> <g:message code="default.button.delete.label" args="[entityName]"/></g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${unitInstanceTotal}" />
				</div>
		</div>
		
	</body>
</html>
