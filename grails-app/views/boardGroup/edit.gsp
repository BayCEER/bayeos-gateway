

<%@ page import="gateway.BoardGroup"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'boardGroup.label', default: 'BoardGroup')}" />
</head>
<body>
	<div class="container">
		<g:renderErrors bean="${boardGroupInstance}" as="list" />

		<g:form class="form" role="form" method="post"	action="edit">
			<g:hiddenField name="id" value="${boardGroupInstance?.id}" />
			
			
			<f:all bean="boardGroupInstance"/>			
			<div class="form-group">

				<button type="submit" class="btn btn-default">
					<span class="glyphicon glyphicon-ok"> Ok</span>
				</button>
				
				
				<button type="submit" class="btn btn-danger" name="_action_delete" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" formnovalidate>
					<span class="glyphicon glyphicon-trash"> </span><g:message code="default.button.delete.label" default="Delete" />
				</button>

			</div>
		</g:form>
	</div>


</body>
</html>
