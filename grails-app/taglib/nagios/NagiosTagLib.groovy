package nagios


import gateway.ReturnCode;

class NagiosTagLib {
	
	static namespace = "nagios"
	
	
	
	def statusMsg = { attrs ->				
			def cssClass
			def msg
			
			def code = attrs?.returnCode			
			if (code == ReturnCode.OK.getValue()) {
				cssClass = "label-success"
				msg = "Ok"
			} else if (code == ReturnCode.WARN.getValue()){
				cssClass = "label-warning"
				msg = "Warning"
			} else if (code == ReturnCode.CRITICAL.getValue()){
				cssClass = "label-danger"
				msg = "Critical"
			} else if (code == ReturnCode.UNKNOWN.getValue()){
				cssClass = "label-active"
				msg = "Unknown"
			} else {
				cssClass = "label-default"
				msg = ""
			}			
			out << "<span class=\"label $cssClass\""
			if (attrs.tip != null) 	out << " data-toggle=\"tooltip\" title=\"${attrs.tip}\""
			out << ">" << msg << "</span>"							
	}
	

	def statusCell = { attrs, body   ->		
			def cssClass 			
			def code = attrs?.returnCode						

			if (code == ReturnCode.OK.getValue()) {
				cssClass = "success"			
			} else if (code == ReturnCode.WARN.getValue()){
				cssClass = "warning"			
			} else if (code == ReturnCode.CRITICAL.getValue()){
				cssClass = "danger"			
			} else if (code == ReturnCode.UNKNOWN.getValue()){
				cssClass = "active"			
			} else {
				cssClass = null				
			}			
			out << "<td" 
			if (cssClass != null) out << " class=\"$cssClass\""
			if (attrs.tip != null) out << " data-toggle=\"tooltip\" title=\"${attrs.tip}\""			
			out << ">" << body()  << "</td>"
					
		
	}
	
	
	
	
}
