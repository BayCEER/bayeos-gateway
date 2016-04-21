 	
 	<div class="block-action">
 	<g:link controller="board" action="createComment"
			class="btn btn-default"
			params='[boardId:"${boardInstance.id}"]'>
			<span class="glyphicon glyphicon-plus"></span>
			<g:message code="default.button.create.label" />
		</g:link>		 	 	
 	</div>
 	
 	<table class="table table-hover">
 	<thead>
 	
 	<tr>
		<th class="col-sm-1">User</th>
		<th class="col-sm-2">Date</th>
		<th class="col-sm-7">Content</th>
		<th class="col-sm-2"></th>
	</tr>
	</thead>
	<tbody>
		<g:each in="${boardInstance.comments}" var="c">
		<tr>
					<td>${c?.user}</td>
					<td><g:formatDate date="${c?.insert_time}" format="dd.MM.yyyy"/></td>
					<td><g:link controller="board" action="editComment" params='[boardId:"${boardInstance.id}"]' id="${c?.id}">
							${c?.content}
					</g:link></td>					
					
					<td>
						<g:link controller="board" action="editComment" id="${c.id}" params='[boardId:"${boardInstance.id}"]' class="btn btn-xs btn-default">
							<span class="glyphicon glyphicon-edit"></span>
							<g:message code="default.button.edit.label" />
						</g:link>
					<g:link
							onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
							controller="board" action="deleteComment" params='[boardId:"${boardInstance.id}"]' id="${c.id}" class="btn btn-xs btn-default">
							<span class="glyphicon glyphicon-remove"></span>
							<g:message code="default.button.delete.label" />
						</g:link> 
					
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>