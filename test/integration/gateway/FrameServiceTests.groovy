package gateway

import static org.junit.Assert.*
import org.junit.*
import org.apache.commons.codec.binary.Base64

import bayeos.frame.MsgFrame;
import bayeos.frame.FrameConstants.NumberType;
import bayeos.frame.data.*;
import bayeos.frame.wrapped.*;
import gateway.FrameService
import gateway.Board



class FrameServiceTests {
	static transactional = false
	
	def frameService	
	
    @Before
    void setUp() {									 		
    }
	
	
    @After
    void tearDown() {       
    }
	
	
    @Test
    void testSaveFlatData() {
		int n = 10000
		def frames = new String[n]
		for(i in 0..n-1) {
			DelayedFrame f = new DelayedFrame(i*10, new IndexFrame(NumberType.Float32,3.5F,2.5F,7.19F,80.0F).getBytes())
			frames[i] = new String(Base64.encodeBase64String(f.getBytes()))
		}
		frameService.saveFrames("testSaveFlat",frames)		
		Board b = Board.findByOrigin("testSaveFlat")
		assert(b)				
		b.delete();						
    }
	
	
	@Test
	void testMessageFrame() {
		MsgFrame msg = new MsgFrame("This is a message")		
		frameService.saveFrames("testMessageFrame",new String(Base64.encodeBase64String(msg.getBytes())));
		Board b = Board.findByOrigin("testMessageFrame")
		b.delete();
				
	}
	
//	@Test
//	void testPosts() {
//		HTTPClient cli = new HTTPClient("http://localhost:8080/bayeos-gateway/gateway/Frame/saveFlat", "root", "bayeos")		
//		MillisecondTimestampFrame f = new MillisecondTimestampFrame(new Date(1453197399820), new IndexFrame(NumberType.Float32,3.5F,2.5F,7.19F,80.0F).getBytes())
//		StringBuffer b = new StringBuffer(1000)
//		String frame = "bayeosframes[]=" + new String(Base64.encodeBase64String(f.getBytes()))
//		for (i in 1..1000){
//			b.append("&")
//			b.append(frame)	
//		}
//		cli.post("TEST",b.toString())
//	}
		

}
