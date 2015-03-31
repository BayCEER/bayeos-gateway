<%@ page import="gateway.Comment" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'comment.label', default: 'Comment')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="container">									
				<h3><g:message code="default.edit.label" args="[entityName]" /></h3>
								
				<g:hasErrors bean="${commentInstance}">
				<bootstrap:alert class="alert-danger">
				<ul>
					<g:eachError bean="${commentInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
				</g:hasErrors>
				<g:form class="form" action="editComment" id="${commentInstance?.id}" >							
							<g:hiddenField name="boardId" value="${boardId}" />									
							
							<f:with bean="commentInstance">	
								<%--<f:display property="user"/>
								<f:display property="insert_time" label="Date">
									<g:formatDate format="dd MMM yyyy" date="${value}"/>
								</f:display>								
								--%><f:field property="content"/>
							</f:with>			
							
							<div class="form-group">
								<button type="submit" class="btn btn-primary">
									<span class="glyphicon glyphicon-ok"> </span><g:message code="default.button.update.label" default="Update" />
								</button>								
							</div>						
					</g:form>		
					<span class="help-block"><g:message code="default.form.asterisk-hint"/>(<span style="color: #f00;">*</span>)</span>					
		</div>
	</body>
</html>
