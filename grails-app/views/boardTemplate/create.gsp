<%@ page import="gateway.BoardTemplate" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'boardTemplate.label', default: 'BoardTemplate')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">
				<div class="btn-group">
					<g:link action="list" class="btn btn-sm btn-default">
						<span class="glyphicon glyphicon-list"></span> <g:message code="default.button.list.label" args="[entityName]"/>						
					</g:link>
					<g:link action="create" class="btn btn-sm btn-default active">
						<span class="glyphicon glyphicon-plus"></span> <g:message code="default.button.create.label" args="[entityName]"/></g:link>					
				</div>
				
				
				
				<g:hasErrors bean="${boardTemplateInstance}">
					<bootstrap:alert class="alert-danger">
						<ul>
							<g:eachError bean="${boardTemplateInstance}" var="error">
								<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}" args="${error.field}"/></li>
							</g:eachError>
						</ul>
					</bootstrap:alert>
				</g:hasErrors>
				
				

				<g:form role="form" action="create" >							
							<g:render template="/boardTemplate/board" model="['template':'boardTemplateInstance','header':'Board Template']"/>							  
							<g:render template="/boardTemplate/channel_setting" model="['channel':'boardTemplateInstance','header':'Default Channel Settings']"/>	
													
							<div class="form-group block-action">
							<button type="submit" class="btn btn-primary">
									<span class="glyphicon glyphicon-ok"></span> <g:message code="default.button.create.label" default="Create" />									
							</button>
							</div>						
				</g:form>
					<span class="help-block"><g:message code="default.form.asterisk-hint"/>(<span style="color: #f00;">*</span>)</span>	

					
		</div>	
	</body>
</html>
