
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>${name}</title>
</head>
<body>
	
	<div class="container">
		
		
		<div class="block">
		
		<div class="block-header">
			<span class="glyphicon glyphicon-time"></span> ${name} 					
		</div>
							
		<g:form class="form" action="edit">	
			<g:hiddenField name="enabled" value="${started}" />
			
			
			<div class="row">	
			<div class="col-sm-2">				
			<div class="make-switch switch-mini" id="sw">
				<g:checkBox type="checkbox" checked="${started}" />			
			</div>			
			</div>
			
			<div class="col-sm-offset-2">
				<label class="control-label">Status:</label>				
				<nagios:statusMsg returnCode="${nagiosMsg?.status}" tip="${nagiosMsg?.text}" />			
			</div>
																
			
			
			</div>
						
			
				<f:field bean="config" property="url"/>
				<f:field bean="config" property="userName"/>
				<div class="form-group">
				<label class="form-label">Password:</label>
					<g:passwordField name="password" class="form-control" value="${config.password}"/>
				</div> 
				
				
				<f:field bean="config" property="dbHomeFolderId"/>
				<f:field bean="config" property="dbHomeUnitId"/>												
    			<f:field bean="config" label="Sleep Interval [min]" property="sleepInterval"/>
    			<f:field bean="config" property="recordsPerBulk"/>    			
    		
    		
    		<div class="form-group block-action">
					<button type="submit" class="btn btn-primary">
						<span class="glyphicon glyphicon-ok"> </span><g:message code="default.button.update.label" default="Update" />
					</button>
			</div>
		</g:form>
		
		
			
		
		</div>
		<span class="help-block"><g:message code="default.form.asterisk-hint"/>(<span style="color: #f00;">*</span>)</span>				
			
			
			
		
	
	</div>	
	
	<script type="text/javascript"> 
 		$(document).ready(function(){	 		 	 
		 $('#sw').on('switch-change', function(e, data) {			 
			 $('#enabled').val(data.value)			
		 });
    	 
	});	    
	</script>
</body>
</html>