package frame.parser;

import java.io.IOException;

import org.junit.Test;
import org.springframework.security.core.codec.Base64;

import bayeos.frame.FrameConstants.NumberType;
import bayeos.frame.data.*;


public class FramePrinterTest {
	
	@Test
	public void  printBase64() throws IOException {					
		System.out.println(new String(Base64.encode(new DataFrame(NumberType.Int32,1,2,3,4).getBytes())));		
	}

}
