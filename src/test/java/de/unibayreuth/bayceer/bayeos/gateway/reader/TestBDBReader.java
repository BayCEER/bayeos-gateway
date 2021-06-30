package de.unibayreuth.bayceer.bayeos.gateway.reader;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.junit.Test;

import bayeos.frame.FrameParserException;
import bayeos.frame.Parser;

public class TestBDBReader {

	@Test
	public void testParsing() throws FileNotFoundException, IOException, FrameParserException {
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
	
	@Test
	public void testOrigin() throws FileNotFoundException, IOException, FrameParserException {
		try(FileInputStream f = new FileInputStream("src/test/resources/Sylt_1.bdb")) {
			BDBReader r = new BDBReader(f);
			r.readHeader();			
			String origin = r.readOrigin();
			assertEquals("Sylt_1", origin);
			
			byte[] data = null;
			long min = 0;
			long max = 0;
			while ((data = r.readData())!=null){
				
				Map<String,Object> map = Parser.parse(data,new Date(),origin,null);
				long d = (Long)map.get("ts");
				if (max == 0 || d > max) {
					max = d;					
				}				
				if (min == 0 || d < min) {
					min = d;					
				}			
				Date ds = new Date(d/(1000*1000));
				System.out.println(ds + ":" + map);				
				
								
					
			}	
			
			Date minDate = new Date(min/(1000*1000));
			Date maxDate = new Date(max/(1000*1000));
			
			System.out.println("Min:" + minDate);
			System.out.println("Max:" + maxDate);
			
		}
	}

}
