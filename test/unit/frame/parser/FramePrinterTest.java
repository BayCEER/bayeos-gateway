package frame.parser;

import java.io.IOException;

import org.junit.Test;
import org.springframework.security.core.codec.Base64;

import frame.BayEOS;
import frame.channel.DataFrame;

public class FramePrinterTest {
	
	@Test
	public void  printBase64() throws IOException {	
				
		System.out.println(new String(Base64.encode(new DataFrame<Integer>().addAll(1,2,3,4).getBytes(BayEOS.Number.Int32))));
		
	}

}
