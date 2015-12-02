package xbee

class XBeeTagLib {

	static namespace = "xbee"


	def rssiChart = { attrs ->
		if (attrs.rssi != null){
			def rssi = attrs.rssi.toInteger()
			if (rssi < -85){
				out << "<img src=\"${resource(dir:'images',file:'level_1.gif')}\"/>"
			} else if (rssi >= -85 && rssi < -75){
				out << "<img src=\"${resource(dir:'images',file:'level_2.gif')}\"/>"
			} else if (rssi >= -75 && rssi < -65){
				out << "<img src=\"${resource(dir:'images',file:'level_3.gif')}\"/>"
			} else if (rssi >= -65 && rssi < -45){
				out << "<img src=\"${resource(dir:'images',file:'level_4.gif')}\"/>"
			} else if (rssi >= -45){
				out << "<img src=\"${resource(dir:'images',file:'level_5.gif')}\"/>"
			}
		}
	}
	
	
}
