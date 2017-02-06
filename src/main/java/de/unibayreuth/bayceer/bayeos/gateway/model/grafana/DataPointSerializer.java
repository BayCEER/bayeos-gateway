package de.unibayreuth.bayceer.bayeos.gateway.model.grafana;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DataPointSerializer extends JsonSerializer<DataPoint>{
	@Override
	public void serialize(DataPoint dp, JsonGenerator jgen, SerializerProvider provider)	throws IOException, JsonProcessingException {
		jgen.writeStartArray();		
		jgen.writeNumber(dp.getValue());
		jgen.writeNumber(dp.getMillis());
		jgen.writeEndArray();		
	}
}
