package nagios


import gateway.ReturnCode;

class NagiosTagLib {
	
	static namespace = "nagios"
	
	
	
	def statusMsg = { attrs ->				
			def cssClass = "label-default"
			def code = attrs?.returnCode
			def msg = "Unknown"
			if (code == ReturnCode.OK.getValue()) {
				cssClass = "label-success"
				msg = "Ok"
			} else if (code == ReturnCode.WARN.getValue()){
				cssClass = "label-warning"
				msg = "Warning"
			} else if (code == ReturnCode.CRITICAL.getValue()){
				cssClass = "label-danger"
				msg = "Critical"
			} 
			
			if (attrs.tip != null) {
				out << "<span class=\"label $cssClass data-toggle=\"tooltip\" title=\"${attrs.tip}\">" << msg << "</span>"
			} else {
				out << "<span class=\"label $cssClass\">" << msg << "</span>"
			}						
		
	}
	

	def statusCell = { attrs, body   ->		
			def cssClass = "active"			
			def code = attrs?.returnCode						
			if (code == ReturnCode.OK.getValue()) {
				cssClass = "success"			
			} else if (code == ReturnCode.WARN.getValue()){
				cssClass = "warning"			
			} else if (code == ReturnCode.CRITICAL.getValue()){
				cssClass = "danger"			
			}
			
			if (attrs.tip != null) {
				out << "<td class=\"$cssClass\" data-toggle=\"tooltip\" title=\"${attrs.tip}\">" << body() << "</td>"
			} else {
				out << "<td class=\"$cssClass\">" << body()  << "</td>"
			}		
		
	}
	
	
	
	
}
