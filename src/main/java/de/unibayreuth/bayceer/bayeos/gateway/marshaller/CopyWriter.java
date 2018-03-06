package de.unibayreuth.bayceer.bayeos.gateway.marshaller;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CopyWriter {

	private static final int JSON_VERSION = 1;
	
	private static long millisUntilMillenium = 946684800000L;
	private static long microsUntilMillenium = millisUntilMillenium * 1000;
	

	 DataOutputStream out;

	public CopyWriter(OutputStream out) {
		this.out = new DataOutputStream(new BufferedOutputStream(out));
	}

	public void writeHeader() throws IOException {		
	            out.writeBytes("PGCOPY\n\377\r\n\0");	    
	            out.writeInt(0);	  
	            out.writeInt(0);
	}

	/**
	 * @param nanos: Nanoseconds since 1970-01-01 
	 * @throws IOException
	 */
	public void writeTimestamp(final Long value) throws IOException {
				
		if (value == null) {
			out.writeInt(-1);
			return;
		} else {
			long micros1970 = value/1000;		
			long micros = micros1970-microsUntilMillenium;				
			out.writeInt(8);
			out.writeLong(micros);
		}
	}

	public void writeText(final String value) throws IOException {
		if (value == null) {
			out.writeInt(-1);
			return;
		} else {
			out.writeInt(value.length());
			out.write(value.getBytes());
		}
	}

	public void writeInteger(final Integer value) throws IOException {
		if (value == null) {
			out.writeInt(-1);
			return;
		} else {
			out.writeInt(4);
			out.writeInt(value);
		}
	}

	public void writeJSONB(final String value) throws IOException {
		if (value == null) {
			out.writeInt(-1);
			return;
		} else {
			byte[] bytes = value.getBytes("UTF-8");
			out.writeInt(bytes.length + 1);
			out.writeByte(JSON_VERSION);
			out.write(bytes);
		}
	}

	public void writeBoolean(final Boolean value) throws IOException {
		if (value == null) {
			out.writeInt(-1);
			return;
		} else {
			out.writeInt(1);
			if (value) {
				out.writeByte(1);
			} else {
				out.writeByte(0);
			}
		}
	}

	public void writeEnd() throws IOException {
		 out.writeShort(-1);
         out.flush();
         out.close();
	}
	
	public void startRow(int columns) throws IOException {
		out.writeShort(columns);
	}

}
