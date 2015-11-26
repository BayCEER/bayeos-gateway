package gateway

class ValidationTagLib {
	
	static namespace = "validation"
	
	def msg = { attrs, body ->
		
		out << '<g:hasErrors bean="${' << arrts.bean << '}">'
		out << '<bootstrap:alert class="alert-danger">'
		out << '<ul>'
			out << '<g:eachError bean="${' << attrs.bean << '}" var="error">'
			out << '<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>'
			out << '</g:eachError>'
		out << '</ul>'
		out << '</bootstrap:alert>'
		out << '</g:hasErrors>'
		
		
	}

}
