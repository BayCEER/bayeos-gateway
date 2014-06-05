<%@ page defaultCodec="html" %>
<div class="form-group ${invalid ? 'has-error' : ''}">
	<label class="control-label" for="${property}">	
	<g:if test="${required}"><span style="color: #f00;">* </span></g:if>${label}:</label>				
	<f:input bean="${bean}" property="${property}" class="form-control"/>	
	<g:if test="${invalid}"><span class="help-block">${errors.join('<br>')}</span></g:if>	
</div>