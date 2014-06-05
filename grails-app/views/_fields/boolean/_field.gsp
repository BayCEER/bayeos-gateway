<%@ page defaultCodec="html"%>

	<div class="checkbox ${invalid ? 'has-error' : ''}">
		<label> <g:if test="${required}">
				<span style="color: #f00;">* </span>
			</g:if>
			${label} <g:checkBox name="${property}" value="${value}" />
		</label>
		<g:if test="${invalid}">
			<span class="help-block">
				${errors.join('<br>')}
			</span>
		</g:if>
	</div>
