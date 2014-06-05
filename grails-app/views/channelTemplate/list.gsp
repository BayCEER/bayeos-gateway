
<%@ page import="gateway.ChannelTemplate" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'channelTemplate.label', default: 'ChannelTemplate')}" />
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
						
									<g:sortableColumn property="criticalMax" title="${message(code: 'channelTemplate.criticalMax.label', default: 'Critical Max')}" />
							
									<g:sortableColumn property="warningMax" title="${message(code: 'channelTemplate.warningMax.label', default: 'Warning Max')}" />
							
									<g:sortableColumn property="warningMin" title="${message(code: 'channelTemplate.warningMin.label', default: 'Warning Min')}" />
							
									<g:sortableColumn property="criticalMin" title="${message(code: 'channelTemplate.criticalMin.label', default: 'Critical Min')}" />
							
									<g:sortableColumn property="samplingInterval" title="${message(code: 'channelTemplate.samplingInterval.label', default: 'Sampling Interval')}" />
							
									<g:sortableColumn property="nr" title="${message(code: 'channelTemplate.nr.label', default: 'Nr')}" />
							
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${channelTemplateInstanceList}" var="channelTemplateInstance">
						<tr>
						
							<td>${fieldValue(bean: channelTemplateInstance, field: "criticalMax")}</td>
						
							<td>${fieldValue(bean: channelTemplateInstance, field: "warningMax")}</td>
						
							<td>${fieldValue(bean: channelTemplateInstance, field: "warningMin")}</td>
						
							<td>${fieldValue(bean: channelTemplateInstance, field: "criticalMin")}</td>
						
							<td>${fieldValue(bean: channelTemplateInstance, field: "samplingInterval")}</td>
						
							<td>${fieldValue(bean: channelTemplateInstance, field: "nr")}</td>
						
							<td class="link">
								<g:link action="edit" id="${channelTemplateInstance.id}" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label" args="[entityName]"/></g:link>
								<g:link action="delete" id="${channelTemplateInstance.id}" class="btn btn-sm btn-default" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span> <g:message code="default.button.delete.label" args="[entityName]"/></g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="${channelTemplateInstanceTotal}" />
				</div>
		</div>
		
	</body>
</html>
