
<div class="block">
	<div class="block-header">Knote Points</div>
	<table class="table table-hover">
		<g:link controller="knotPoint" action="create"
			class="btn btn-xs btn-default"
			params='[splineId:"${splineInstance.id}"]'>
			<span class="glyphicon glyphicon-plus"></span>
			<g:message code="default.button.create.label" />
		</g:link>
		<tr>
			<th>Nr</th>
			<th>Values</th>			
			<th></th>
		</tr>
		<tbody>
			<g:each in="${splineInstance.knotePoints}" var="c">
				<tr>
					<td><g:link controller="knotPoint" action="edit"
							id="${c?.id}">
							${c?.id}
						</g:link></td>					
					<td>
						${c?.toString()}
					</td>
					
					<td><g:link
							onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
							controller="knotPoint" action="delete" id="${c.id}" class="btn btn-xs btn-default">
							<span class="glyphicon glyphicon-remove"></span>
							<g:message code="default.button.delete.label" />
						</g:link> <g:link controller="knotPoint" action="edit" id="${c.id}" class="btn btn-xs btn-default">
							<span class="glyphicon glyphicon-edit"></span>
							<g:message code="default.button.edit.label" />
						</g:link></td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
