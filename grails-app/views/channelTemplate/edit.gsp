<%@ page import="gateway.ChannelTemplate" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'channelTemplate.label', default: 'ChannelTemplate')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">	
				
				<h3><g:message code="default.edit.label" args="[entityName]" /></h3>				
				
				<g:hasErrors bean="${channelTemplateInstance}">
				<bootstrap:alert class="alert-danger">
				<ul>
					<g:eachError bean="${channelTemplateInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
				</g:hasErrors>
				
				
				
					<g:form class="form" action="edit" id="${channelTemplateInstance?.id}" >
						<g:hiddenField name="version" value="${channelTemplateInstance?.version}" />	
						<g:hiddenField name="boardTemplateId" value="${channelTemplateInstance.boardTemplate.id}" />					
							<g:render template="/channelTemplate/channelTemplate" model="['channel':'channelTemplateInstance','header':'Template']"/>		
							<g:render template="/boardTemplate/channel_setting"
									model="['channel':'channelTemplateInstance','header':'Check Settings']" />
					
							<div class="form-group block-action">
								<button type="submit" class="btn btn-primary">
									<span class="glyphicon glyphicon-ok"> </span><g:message code="default.button.ok.label" default="Ok" />
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
