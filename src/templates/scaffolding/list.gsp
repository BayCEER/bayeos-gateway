<% import grails.persistence.Event %>
<%=packageName%>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
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
						<%  excludedProps = Event.allEvents.toList() << 'id' << 'version'
							allowedNames = domainClass.persistentProperties*.name << 'dateCreated' << 'lastUpdated'
							props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) && it.type != null && !Collection.isAssignableFrom(it.type) }
							Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
							props.eachWithIndex { p, i ->
								if (i < 6) {
								if (p.isAssociation()) { %>
									<th class="header"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></th>
							<%      } else { %>
									<g:sortableColumn property="${p.name}" title="\${message(code: '${domainClass.propertyName}.${p.name}.label', default: '${p.naturalName}')}" />
							<%  }   }   } %>
							<th></th>
						</tr>
					</thead>
					<tbody>
					<g:each in="\${${propertyName}List}" var="${propertyName}">
						<tr>
						<%  props.eachWithIndex { p, i ->
						        if (i < 6) {
									if (p.type == Boolean || p.type == boolean) { %>
							<td><g:formatBoolean boolean="\${${propertyName}.${p.name}}" /></td>
						<%          } else if (p.type == Date || p.type == java.sql.Date || p.type == java.sql.Time || p.type == Calendar) { %>
							<td><g:formatDate date="\${${propertyName}.${p.name}}" /></td>
						<%          } else { %>
							<td>\${fieldValue(bean: ${propertyName}, field: "${p.name}")}</td>
						<%  }   }   } %>
							<td class="link">
								<g:link action="edit" id="\${${propertyName}.id}" class="btn btn-sm btn-default"><span class="glyphicon glyphicon-edit"></span> <g:message code="default.button.edit.label" args="[entityName]"/></g:link>
								<g:link action="delete" id="\${${propertyName}.id}" class="btn btn-sm btn-default" onclick="return confirm('\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
								<span class="glyphicon glyphicon-remove"></span> <g:message code="default.button.delete.label" args="[entityName]"/></g:link>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<div class="pagination">
					<bootstrap:paginate total="\${${propertyName}Total}" />
				</div>
		</div>
		
	</body>
</html>
