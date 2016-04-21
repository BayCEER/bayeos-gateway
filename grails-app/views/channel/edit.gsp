<%@ page import="gateway.Channel"%>
<html>
<head>
<meta name="layout" content="main" />
<g:set var="entityName" value="${message(code: 'channel.label', default: 'Channel')}" />
<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>

	<div class="container">
		
		<g:hasErrors bean="${channelInstance}">
				<bootstrap:alert class="alert-danger">
				<ul>
					<g:eachError bean="${channelInstance}" var="error">
					<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
					</g:eachError>
				</ul>
				</bootstrap:alert>
		</g:hasErrors>

		<g:form class="form" role="form" action="edit" id="${channelInstance?.id}">
			<f:with bean="channelInstance">
			<g:hiddenField name="version" value="${channelInstance?.version}" />
			
			<div class="block">
				<div class="row">
					<label class="col-sm-2 control-label" for="nr">Channel Nr:</label>
					<div class="col-sm-4 form-control-static" name="nr">
						${fieldValue(bean: channelInstance, field: 'nr')}
					</div>

					<label class="col-sm-2 control-label">Board Origin:</label>
					<div class="col-sm-4 form-control-static">
						<g:link controller="board" action="edit"
							id="${channelInstance.board.id}">
							${channelInstance.board.origin}
						</g:link>
					</div>
				</div>				
				<div class="row">		
					<div class="col-sm-6">			
						<f:field property="label"/>
					</div>
					<div class="col-sm-6">
						<f:field property="phenomena"/>
					</div>
																								
				</div>
				<div class="row">
				<div class="col-sm-6">
					<f:field property="unit"/>
					</div>
				<div class="col-sm-6">
					<f:field property="spline"/>
					</div>
				</div>
				
				
				<div class="row">
				<div class="col-sm-6">
					<f:field property="aggrInterval" label="Aggr. Interval:"/>
				</div>
				<div class="col-sm-6">
					<f:field property="aggrFunction" label="Aggr. Function:"/>
				</div>					
				</div>
			</div>
			<div class="block">
				<div class="block-header">Checks</div>
				<div class="row">
				<div class="col-sm-6">
					<f:field property="criticalMin"/>
					</div>
				<div class="col-sm-6">
					<f:field property="criticalMax"/>
					</div>
				</div>
				<div class="row">
				<div class="col-sm-6">
					<f:field property="warningMin"/>
					</div>
				<div class="col-sm-6">
					<f:field property="warningMax"/>
					</div>					
				</div>
				<div class="row">
				 <div class="col-sm-6">
					<f:field property="samplingInterval"/>
				 </div>
				 <div class="col-sm-6">
					<f:field property="checkDelay" label="Check Delay [sec]:"/>
				 </div>										
				</div>
				<div class="row">				
				 <div class="col-sm-6">
					<f:field property="excludeFromNagios"/>
				 </div>										
				</div>
			</div>

			<div class="block">
				<div class="block-header">Exports</div>			
				<div class="row">
				<div class="col-sm-6">
					<f:field property="dbSeriesId" />
				</div>
				<div class="col-sm-6">
					<f:field property="dbExcludeAutoExport" label="Exclude from Auto Export"/>
				</div>
				</div>
			</div>

				
			<div class="block-action">
			
				
				<button type="submit" class="btn btn-primary">
					<span class="glyphicon glyphicon-ok"></span> <g:message	code="default.button.ok.label" default="Ok" />
				</button>
				<button type="submit" class="btn btn-danger" name="_action_delete" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
					formnovalidate>
					<span class="glyphicon glyphicon-trash"></span> <g:message
							code="default.button.delete.label" default="Delete" />
				</button>				
				
					
			</div>
		</f:with>	
		</g:form>
		</div>
	
</body>
</html>
