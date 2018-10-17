package de.unibayreuth.bayceer.bayeos.gateway.marshaller

import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelTemplate
import de.unibayreuth.bayceer.bayeos.gateway.model.Function
import de.unibayreuth.bayceer.bayeos.gateway.model.Interval
import de.unibayreuth.bayceer.bayeos.gateway.model.KnotPoint
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline
import de.unibayreuth.bayceer.bayeos.gateway.model.Unit
import groovy.xml.MarkupBuilder

class BoardTemplateMarshaller {	
	// Object to XML String 
	public static String marshal(BoardTemplate board){
		StringWriter writer = new StringWriter()
		MarkupBuilder xml = new MarkupBuilder(writer)
		xml.setOmitNullAttributes(true)
		xml.setOmitEmptyAttributes(true)
		xml.board(name:board.name,description:board.description,revision:board.revision,
		dataSheet:board.dataSheet, samplingInterval:board.samplingInterval,checkDelay:board.checkDelay){
			board.templates.each { item ->
				channel(nr:item.nr, name:item.name, phenomena:item.phenomena,aggrInterval:item.aggrInterval, aggrFunction:item.aggrFunction,
				criticalMax:item.criticalMax,criticalMin:item.criticalMin,warningMax:item.warningMax,warningMin:item.warningMin,samplingInterval:item.samplingInterval,checkDelay:item.checkDelay){
					if (item.unit){
						unit(name:item.unit.name)
					}
					if (item.spline){
						spline(name:item.spline?.name){
							item.spline?.knotPoints?.each { kp ->
								knotePoint(x:kp.x,y:kp.y)
							}
						}
					}
				}
			}
		}
		return writer.toString()
	}
			
	// XML Stream to Object 
	public static BoardTemplate unmarshal(InputStream stream){
		def n = new XmlParser().parse(stream)				
		def template = new BoardTemplate(name:n['@name'],revision:n['@revision'],dataSheet:n['@dataSheet'], description:n['@description'])		
		if (n['@samplingInterval']){
			template.samplingInterval = Integer.valueOf(n['@samplingInterval'])
		}
		
		if (n['@checkDelay']){
			template.checkDelay = Integer.valueOf(n['@checkDelay'])
		}	
		n.channel.each { ch ->			
			def cht = new ChannelTemplate(nr:Integer.valueOf(ch['@nr']),name:ch['@name']?:ch['@label'],phenomena:ch['@phenomena'])
			if (ch['@aggrInterval']){
				cht.aggrInterval = new Interval(ch['@aggrInterval'])				 
			}
			if (ch['@aggrFunction']){
				cht.aggrFunction = new Function(ch['@aggrFunction'])				 
			}
			if (ch['@criticalMax']){
				cht.criticalMax = Float.valueOf(ch['@criticalMax'])
			}
			if (ch['@criticalMin']){
				cht.criticalMin = Float.valueOf(ch['@criticalMin'])
			}
			if (ch['@warningMax']){
				cht.warningMax = Float.valueOf(ch['@warningMax'])
			}
			if (ch['@warningMin']){
				cht.warningMin = Float.valueOf(ch['@warningMin'])
			}
			if (ch['@samplingInterval']){
				cht.samplingInterval = Integer.valueOf(ch['@samplingInterval'])
			}			
			if (ch['@checkDelay']){
				cht.checkDelay = Integer.valueOf(ch['@checkDelay'])
			}			
			if (ch.unit) {
				String u = ch.unit[0]['@name']
				cht.setUnit(new Unit(name:u))
			}
			if (ch.spline) {
				String s = ch.spline[0]['@name']
				Spline spline = new Spline(name:s)
					ch.spline[0].knotePoint.each{ kp ->
						spline.addKnotPoint(new KnotPoint(x:Float.valueOf(kp['@x']),y:Float.valueOf(kp['@y'])))
					}					
				cht.setSpline(spline)
			}
			template.addTemplate(cht)
		}
		return template
	}
}
