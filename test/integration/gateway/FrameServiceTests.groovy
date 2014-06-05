package gateway

import static org.junit.Assert.*
import org.junit.*
import org.apache.commons.codec.binary.Base64

import frame.BayEOS
import frame.channel.IndexFrame
import frame.wrapped.DelayedFrame
import gateway.FrameService



class FrameServiceTests {
	static transactional = false
	
	def frameService	
	String[] frames
	
	int FRAME_COUNT = 10000  
	
    @Before
    void setUp() {		
		frames = new String[FRAME_COUNT]		
		for(i in 0..FRAME_COUNT-1) {			
			DelayedFrame f = new DelayedFrame(i*10, new IndexFrame<Float>().putAll(3.5F,2.5F,7.19F,80.0F).getBytes(BayEOS.Number.Float32))
			frames[i] = new String(Base64.encodeBase64String(f.getBytes()))
		}		
					 			
    }

    @After
    void tearDown() {       
    }

    @Test
    void testSaveFlatData() {			
		frameService.saveFlatFrames("FLAT",new Date(), frames)		
		Board b = Board.findByOrigin("FLAT")
		assert(b)				
		b.delete();						
    }
	

	@Test
	void testSaveMatrix() {
		frameService.saveMatrixFrames("MATRIX",new Date(),frames)		
		Board b = Board.findByOrigin("MATRIX")
		assertTrue(b.frameStorage)
		b.delete();
	}

}
