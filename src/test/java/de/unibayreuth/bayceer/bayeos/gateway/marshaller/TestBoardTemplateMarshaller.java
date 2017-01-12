package de.unibayreuth.bayceer.bayeos.gateway.marshaller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import de.unibayreuth.bayceer.bayeos.gateway.model.BoardTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.model.ChannelTemplate;
import de.unibayreuth.bayceer.bayeos.gateway.model.Function;
import de.unibayreuth.bayceer.bayeos.gateway.model.Interval;

public class TestBoardTemplateMarshaller {

	@Test
	public void unmarshal() {
		try(FileInputStream f = new FileInputStream("src/test/resources/boardTemplate.xml")) {			
			BoardTemplate t = BoardTemplateMarshaller.unmarshal(f);			
			assert(t!=null);			
			assertEquals("BayEOS Wetterstation",t.getName());			
			assertEquals(6,t.getTemplates().size());			
			assertEquals("Niederschlag Kippwaage", t.getTemplates().get(4).getSpline().getName());
			
		} catch (IOException e){
			fail(e.getMessage());
		}
	}
	
	@Test
	public void marshal(){
		BoardTemplate d = new BoardTemplate("BayEOS Wetterstation","Einfache Wetterstation mit LE Board");
		d.setRevision("2.0");
		d.setSamplingInterval(15);
		
		ChannelTemplate c = new ChannelTemplate("1","CPU Time","Time",null,null,new Interval("10 min"),new Function("avg"));
		
		d.addTemplate(c);
		
		String xml = BoardTemplateMarshaller.marshal(d);
		ByteArrayInputStream f = new ByteArrayInputStream(xml.getBytes());				
		BoardTemplate t = BoardTemplateMarshaller.unmarshal(f);
		
		assertEquals("BayEOS Wetterstation", t.getName());
		assertEquals("Einfache Wetterstation mit LE Board", t.getDescription());
		assertEquals(1, t.getTemplates().size());
		
		ChannelTemplate ct = t.getTemplate(0);
		assertEquals("10 min",ct.getAggrInterval().getName());
		assertEquals("avg",ct.getAggrFunction().getName());
				
		
		
	}
	

}
