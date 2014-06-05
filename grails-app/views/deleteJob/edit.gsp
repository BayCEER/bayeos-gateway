
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
			
			<div class="form-group">					
			<div class="make-switch switch-mini" id="sw">
				<g:checkBox type="checkbox" checked="${started}" />			
			</div>
			</div>
			<f:with bean="config">				
				<f:field label="Record Buffer" property="maxResultInterval.name"/>
				<f:field label="Message Buffer" property="maxMessageInterval.name"/>				
    			<f:field label="Delay Interval [min]" property="delayInterval"/>    		
    		</f:with>
    		
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