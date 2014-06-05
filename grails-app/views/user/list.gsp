
<%@ page import="gateway.User" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
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
						
									<g:sortableColumn property="username" title="${message(code: 'user.username.label', default: 'Username')}" />
																
							
									<g:sortableColumn property="accountExpired" title="${message(code: 'user.accountExpired.label', default: 'Account Expired')}" />
							
									<g:sortableColumn property="accountLocked" title="${message(code: 'user.accountLocked.label', default: 'Account Locked')}" />
							
									<g:sortableColumn property="enabled" title="${message(code: 'user.enabled.label', default: 'Enabled')}" />
							
									<g:sortableColumn property="passwordExpired" title="${message(code: 'user.passwordExpired.label', default: 'Password Expired')}" />
							
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${userInstanceList}" var="userInstance">
						<tr>
						
							<td>${fieldValue(bean: userInstance, field: "username")}</td>
																			
							<td><g:formatBoolean boolean="${userInstance.accountExpired}" /></td>
						
							<td><g:formatBoolean boolean="${userInstance.accountLocked}" /></td>
						
							<td><g:formatBoolean boolean="${userInstance.enabled}" /></td>
						
							<td><g:formatBoolean boolean="${userInstance.passwordExpired}" /></td>
						
							<td class="link">
								<g:link action="edit" id="${userInstance.id}" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label" args="[entityName]"/></g:link>
								<g:link action="delete" id="${userInstance.id}" class="btn btn-sm btn-default" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span> <g:message code="default.button.delete.label" args="[entityName]"/></g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${userInstanceTotal}" />
				</div>
		</div>
		
	</body>
</html>
