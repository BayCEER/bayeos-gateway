<html>
    <head>        
        <meta name="layout" content="main" />        
        <title>Apply Template</title>
    </head>
    <body> 
    	
            <div class="container">
                       
            <g:form method="post">
             <g:hiddenField name="id" value="${boardInstance?.id}" />
             <g:hiddenField name="version" value="${boardInstance?.version}" />
            
            <div class="row">
            	<label class="col-sm-2 control-label">Board:</label>
            	<div class="col-sm-4">
						<div class="form-control-static">
								${boardInstance.name}
						</div>
				</div>												
			</div>
            							
            
            
           <div class="row">
           	<div class="col-sm-6">
             	<div class="form-group">
             	<label class="control-label" for="template">Template:</label>             	
            	<g:select name="templateId" from="${gateway.BoardTemplate.list()}" optionKey="id" class="form-control"/>
            	</div>
            
            </div>
           </div>
            <g:actionSubmit class="btn btn-primary" action="applyTemplate" value="${message(code: 'default.button.save.label', default: 'Save')}" />     
           </g:form>
              
                        
              
        </div>     
    </body>

</html>