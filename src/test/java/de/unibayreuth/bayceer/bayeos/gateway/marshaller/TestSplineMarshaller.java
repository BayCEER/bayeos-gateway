package de.unibayreuth.bayceer.bayeos.gateway.marshaller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;

import de.unibayreuth.bayceer.bayeos.gateway.model.KnotPoint;
import de.unibayreuth.bayceer.bayeos.gateway.model.Spline;

public class TestSplineMarshaller {

	@Test
	public void unmarshal() {
		try(FileInputStream f = new FileInputStream("src/test/resources/spline.xml")) {			
			Spline s = SplineMarshaller.unmarshal(f);
			
			assert(s!=null);						
			
			assertEquals("1:10",s.getName() );
			assertEquals(2, s.getKnotPoints().size());
			
			KnotPoint a = s.getKnotPoints().get(0);						
			assertEquals(0.0,a.getX(),0.1);
			assertEquals(0.0,a.getY(),0.1);
			
			KnotPoint b = s.getKnotPoints().get(1);						
			assertEquals(10.0,b.getX(),0.1);
			assertEquals(1.0,b.getY(),0.1);
			
		} catch (IOException e) {
			fail(e.getMessage());
		}						
	}

	@Test
	public void marshal() {
		Spline s = new Spline("Dummy");
		s.addKnotPoint(new KnotPoint(1.12F,2.01F));								
		
		String xml = SplineMarshaller.marshal(s);		
		ByteArrayInputStream f = new ByteArrayInputStream(xml.getBytes());			
		Spline b  = SplineMarshaller.unmarshal(f);
		
		assertEquals("Dummy", b.getName());				
		assertEquals(1, b.getKnotPoints().size());		
		KnotPoint a = b.getKnotPoints().get(0);				
		assertEquals(1.12,a.getX(),0.1);
		assertEquals(2.01,a.getY(),0.1);
				
	}
}
