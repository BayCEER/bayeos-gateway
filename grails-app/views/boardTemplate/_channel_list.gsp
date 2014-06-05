
<div class="block">
	<div class="block-header">Channels</div>
	<table class="table table-hover">
		<g:link controller="channelTemplate" action="create"
			class="btn btn-xs btn-default"
			params='[boardTemplateId:"${boardTemplateInstance.id}"]'>
			<span class="glyphicon glyphicon-plus"></span>
			<g:message code="default.button.create.label" />
		</g:link>
		<tr>
			<th>Nr</th>
			<th>Label</th>
			<th>Phenomena</th>
			<th>Unit</th>
			<th></th>
		</tr>
		<tbody>
			<g:each in="${boardTemplateInstance.channelTemplates}" var="c">
				<tr>
					<td><g:link controller="channelTemplate" action="edit"
							id="${c?.id}">
							${c?.nr}
						</g:link></td>
					<td>
						${c?.label}
					</td>
					<td>
						${c?.phenomena}
					</td>
					<td>
						${c?.unit?.name}
					</td>

					<td><g:link
							onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"
							controller="channelTemplate" action="delete" id="${c.id}" class="btn btn-xs btn-default">
							<span class="glyphicon glyphicon-remove"></span>
							<g:message code="default.button.delete.label" />
						</g:link> <g:link controller="channelTemplate" action="edit" id="${c.id}" class="btn btn-xs btn-default">
							<span class="glyphicon glyphicon-edit"></span>
							<g:message code="default.button.edit.label" />
						</g:link></td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
