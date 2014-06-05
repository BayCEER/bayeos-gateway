<%@ page defaultCodec="html"%>
<div class="col-sm-offset-2 col-sm-4 ${invalid ? 'has-error' : ''}">
	<div class="checkbox">
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
</div>