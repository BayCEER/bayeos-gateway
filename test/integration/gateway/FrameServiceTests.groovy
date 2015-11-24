package gateway

import static org.junit.Assert.*
import org.junit.*
import org.apache.commons.codec.binary.Base64

import bayeos.frame.FrameConstants.NumberType;
import bayeos.frame.data.*;
import bayeos.frame.wrapped.*;
import gateway.FrameService
import gateway.Board



class FrameServiceTests {
	static transactional = false
	
	def frameService	
	String[] frames
	
	int FRAME_COUNT = 10000  
	
    @Before
    void setUp() {		
		frames = new String[FRAME_COUNT]		
		for(i in 0..FRAME_COUNT-1) {			
			DelayedFrame f = new DelayedFrame(i*10, new IndexFrame(NumberType.Float32,3.5F,2.5F,7.19F,80.0F).getBytes())
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
