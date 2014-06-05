

<%@ page import="gateway.BoardGroup" %>
<html>
    <head>
        
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'boardGroup.label', default: 'BoardGroup')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>        
    </head>
    <body>        
        <div class="container">
            <h3><g:message code="default.create.label" args="[entityName]"/></h3>
                       
			<g:hasErrors bean="${boardGroupInstance}">
					<bootstrap:alert class="alert-danger">
						<ul>
							<g:eachError bean="${boardGroupInstance}" var="error">
								<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}" args="${error.field}"/></li>
							</g:eachError>
						</ul>
					</bootstrap:alert>
			</g:hasErrors>
                                   
            <g:form class="form" role="form" action="create" >
            	<f:field bean="boardGroupInstance" property="name"/>
             	
             	<div class="form-group">
							<button type="submit" class="btn btn-primary">
									<span class="glyphicon glyphicon-ok"></span> <g:message code="default.button.create.label" default="Create" />									
							</button>
				</div>		
             	
               
            
            </g:form>
        </div>
    </body>
</html>
