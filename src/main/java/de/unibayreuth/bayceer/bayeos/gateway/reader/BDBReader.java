package de.unibayreuth.bayceer.bayeos.gateway.reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import bayeos.binary.ByteArray;

public class BDBReader  {
	
	// Binary Logger Format
	// [0x50e0a10b][1 byte LoggerName length][LoggerName][4 byte ts][1 byte length][body]

	private InputStream in;
	
	private long HEADER = 0x50e0a10bL;
	
	public BDBReader(InputStream in) {
		this.in = in;
	}
	
	public void readHeader() throws IOException{
		byte[] h = new byte[4];
		int n = in.read(h);
		if (n!=4) {
			throw new IOException("Incomplete file header");
		}
		if (ByteArray.fromByteUInt32(h) != HEADER) {
			throw new IOException("Invalid BDB file");
		};
	}
	
	public String readOrigin() throws IOException{
		int length = in.read();		
		if (length < 1 ) {
			throw new IOException("Incomplete file header");
		};	
		byte[] b = new byte[length];
		int n = in.read(b);
		if (n != length) {
			throw new IOException("Incomplete file header");	
		}
		return new String(b);
	}
	
	public byte[] readData() throws IOException{
		byte[] ts = new byte[4];
		int b = in.read(ts);
		if (b == -1) return null;
		if (b != 4) {
			throw new IOException("Incomplete timestamp");
		}
		int l = in.read();
		
		if (l<1) {
			throw new IOException("Incomplete data block");
		}
		byte[] out = new byte[l+5];
		out[0] = 0x9; // Timestamp Frame
		out[1] = ts[0];
		out[2] = ts[1];
		out[3] = ts[2];
		out[4] = ts[3];
		for(int z=0;z<l;z++){
			int r = in.read();
			if (r == -1) break;	
			out[5 + z] = (byte) r;
		}		
		return out;
	}
	
	
}
