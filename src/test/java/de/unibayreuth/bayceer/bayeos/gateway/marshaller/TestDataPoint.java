package de.unibayreuth.bayceer.bayeos.gateway.marshaller;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unibayreuth.bayceer.bayeos.gateway.model.grafana.DataPoint;

public class TestDataPoint {

	@Test
	public void testSerialize() throws JsonProcessingException {
		DataPoint p = new DataPoint(12.0F,10001L);
		ObjectMapper om = new ObjectMapper();		
		final String json = om.writeValueAsString(p);
		System.out.println(json);
	}

}
