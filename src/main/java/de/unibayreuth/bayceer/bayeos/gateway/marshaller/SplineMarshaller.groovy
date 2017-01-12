package de.unibayreuth.bayceer.bayeos.gateway.marshaller

import java.io.InputStream
import de.unibayreuth.bayceer.bayeos.gateway.model.KnotPoint
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline
import groovy.xml.MarkupBuilder

class SplineMarshaller {
	
	public static String marshal(Spline s){
		StringWriter writer = new StringWriter()
		MarkupBuilder xml = new MarkupBuilder(writer)
		xml.setOmitNullAttributes(true)
		xml.setOmitEmptyAttributes(true)
		xml.spline(name:s.name){
			s.knotPoints.each { item ->
				data(x:item.x,y:item.y)
			}
		}
		return writer.toString()		
	}
	
	public static Spline unmarshal(InputStream stream){
		def result = new XmlParser().parse(stream)
		def spline = new Spline()
		spline.name = result.@name
		result.data.each {
			KnotPoint p = new KnotPoint(x:new Float(it.attribute("x")),y:new Float(it.attribute("y")))
			spline.addKnotPoint(p)
		}
		return spline
	}
}
