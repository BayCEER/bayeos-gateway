<%@ page import="gateway.User" %>
<%@ page import="gateway.Role" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">	
				<div class="btn-group">
					<g:link action="list" class="btn btn-sm btn-default">
						<span class="glyphicon glyphicon-list"></span> <g:message code="default.button.list.label" args="[entityName]"/>						
					</g:link>
					<g:link action="create" class="btn btn-sm btn-default">
						<span class="glyphicon glyphicon-plus"></span> <g:message code="default.button.create.label" args="[entityName]"/></g:link>					
				</div>		
				
				<h3><g:message code="default.edit.label" args="[entityName]" /></h3>
				
				
				<g:hasErrors bean="${userInstance}">
				<bootstrap:alert class="alert-danger">
				<ul>
					<g:eachError bean="${userInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
				</g:hasErrors>
				
				
				
					<g:form class="form" action="edit" id="${userInstance?.id}" >
						<g:hiddenField name="version" value="${userInstance?.version}" />
						
							<f:field bean="userInstance" property="username"/>						  	
						  	<div class="form-group ${userInstance.errors?.hasFieldErrors('password')} ? 'has-error' : ''}">						  								  		
						  		<label class="control-label" for="${password}">
						  			<span style="color: #f00;">* </span> Password:
						  		</label>						  		
						  		<g:passwordField name="password" class="form-control" value="${userInstance.password}"/>						  								  								  							  	
						  	</div>
						  	
						  	
						  	<f:field bean="userInstance" property="accountExpired"/>						  
						  	<f:field bean="userInstance" property="accountLocked"/>
						  	
						  	<f:field bean="userInstance" property="enabled"/>
						  	<f:field bean="userInstance" property="passwordExpired"/>
						  	
						  	
						  	
						  	
						  										  		
						  	<div class="form-group">
						  	<label class="control-label">Roles:</label>										  								  		
						  		<g:select class="form-control" name="roles" from="${Role.list(sort:"authority", order:"asc")}" optionKey="id" optionValue="name" multiple="true" value="${userInstance.getAuthorities()*.id}"/>						  		
						  	</div>													  				  							  						
							
							<div class="form-group">
								<button type="submit" class="btn btn-primary">
									<span class="glyphicon glyphicon-ok"> </span><g:message code="default.button.update.label" default="Update" />
								</button>
								<button type="submit" class="btn btn-danger" name="_action_delete" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" formnovalidate>
									<span class="glyphicon glyphicon-trash"> </span><g:message code="default.button.delete.label" default="Delete" />
								</button>
							</div>						
					</g:form>		
					<span class="help-block"><g:message code="default.form.asterisk-hint"/>(<span style="color: #f00;">*</span>)</span>					
		</div>
	</body>
</html>
