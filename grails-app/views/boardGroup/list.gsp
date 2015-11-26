
<html>
    <head>
        <g:set var="entityName" value="${message(code: 'boardGroup.label', default: 'Group')}" />
        <meta name="layout" content="main" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>        
    </head>
    <body>
        <div class="container">
        	<div class="btn-group">
        	<g:link class="btn btn-sm btn-default" action="create">
            	<span class="glyphicon glyphicon-plus"></span> <g:message code="default.button.create.label" args="[entityName]"/>
            </g:link>        	
        	</div>
               <table class="table table-hover">
                    <thead>
                        <tr>                        
                            <th>Name</th>
                            <th>Boards</th>
                            <th>Time</th>
                            <th>Status</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${result}" status="i" var="res">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">                        
                            <td><g:link action="edit" id="${res.id}">${res.name}</g:link></td>
                            <td>${res.board_count}</td>
                            <td><g:formatDate date="${res.lrt}" format="dd.MM.yyyy HH:mm:ss z"/></td> 
                         	<td><nagios:statusMsg returnCode="${res.status}"/></td>
                         	<td class="link">
                         		<g:link action="edit" id="${res.id}" class="btn btn-xs btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label"/></g:link>
								<g:link action="delete" id="${res.id}" class="btn btn-xs btn-default" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span>
								<g:message code="default.button.delete.label"/>
							</g:link>
							</td>            
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            
            <bootstrap:paginate total="${total}"/>
            
            
        
        </div>
    </body>
</html>
