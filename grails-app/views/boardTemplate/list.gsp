
<html>
    <head>        
        <meta name="layout" content="main" />        
        <g:set var="entityName" value="${message(code: 'template.label', default: 'Template')}" />
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
					
					<g:link action="upload" class="btn btn-sm btn-default">
						<span class="glyphicon glyphicon-upload"></span> <g:message code="default.button.upload.label" args="[entityName]"/>						
					</g:link>			            
        </div>
        
         <table class="table table-striped">
                   <thead>
                        <tr>                                                                         
                            <g:sortableColumn property="name" title="Name"/>
                            <g:sortableColumn property="revision" title="Revision"/>                            
                            <g:sortableColumn property="dateCreated" title="Date created" class="hidden-xs"/>
                            <th class="hidden-xs">Number of Channels</th>    
                            <th></th>                                                  
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${boardTemplateInstance}" var="res">
                        <tr>                        
                            <td><g:link action="edit" id="${res.id}">${res.name}</g:link></td>                        
                            <td>${res.revision}</td>                            
                            <td class="hidden-xs"><g:formatDate date="${res.dateCreated}" format="dd.MM.yyyy HH:mm:ss z"/></td>
                            <td class="hidden-xs">${res.channelTemplates?.size()}</td>
                            <td>                             	
                            	<g:link action="edit" id="${res.id}" class="btn btn-xs btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label"/></g:link>                      			 
                       			<g:link onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" controller="boardTemplate" action="delete" class="btn btn-xs btn-default" params='[id:"${res?.id}"]'><span class="glyphicon glyphicon-remove"></span> Delete</g:link>                       			                       			
                       			<g:link controller="boardTemplate" action="export" class="btn btn-xs btn-default" params='[id:"${res?.id}"]'><span class="glyphicon glyphicon-save"></span> Export</g:link>
							</td>
                        </tr>
                    </g:each>
                    </tbody>
            </table>
            <bootstrap:paginate total="${total}" />            
            </div>
        
        
           

           
           
        
    </body>
</html>
