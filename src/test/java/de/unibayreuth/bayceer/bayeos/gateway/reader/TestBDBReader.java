package de.unibayreuth.bayceer.bayeos.gateway.reader;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import bayeos.frame.FrameParserException;
import bayeos.frame.Parser;

public class TestBDBReader {

	@Test
	public void test() throws FileNotFoundException, IOException, FrameParserException {
		try(FileInputStream f = new FileInputStream("src/test/resources/CR123a-Logger.bdb")) {
			BDBReader r = new BDBReader(f);
			r.readHeader();			
			String origin = r.readOrigin();
			assertEquals("CR123a-Logger", origin);
			
			byte[] data = null;
			while ((data = r.readData())!=null){
				System.out.println(Parser.parse(data,new Date(),origin,null));
			}	
		}
	}

}
