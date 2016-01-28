package gateway

import static org.junit.Assert.*
import org.junit.*
import org.apache.commons.codec.binary.Base64

import bayeos.frame.StringFrame
import bayeos.frame.FrameConstants;
import bayeos.frame.FrameConstants.NumberType;
import bayeos.frame.data.*;
import bayeos.frame.wrapped.*;
import gateway.FrameService
import gateway.Board
import groovy.sql.Sql



class FrameServiceTests {
	static transactional = false
	
	def frameService	
	def observationService
	def dataSource
	
	
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
		
		Sql db = new Sql(dataSource)		
		def row = db.firstRow("select count(*) from observation, board, channel where observation.channel_id = channel.id and channel.board_id = board.id and board.id = ?",[b.id])
		assert 40000 == row[0]
		db.close()		
		b.delete(flush:true);						
    }
	
	
	@Test
	void testMessageFrame() {
		StringFrame msg = new StringFrame(FrameConstants.Message, "This is a test message")		
		frameService.saveFrames("testMessageFrame",new String(Base64.encodeBase64String(msg.getBytes())));
		Board b = Board.findByOrigin("testMessageFrame")		
		Message m = Message.findByOrigin(b.getOrigin())
		assert "This is a test message" == m.getContent()		
		b.delete(flush:true);
		m.delete(flush:true);
		
				
	}
	
	@Test
	void testNaN() {
		
		Date now = new Date()		
		MillisecondTimestampFrame frame = new MillisecondTimestampFrame(now, new DataFrame(NumberType.Float32, Float.NaN,1.0F).getBytes())
		frameService.saveFrames("testNaN",new String(Base64.encodeBase64String(frame.getBytes())))
		Board b = Board.findByOrigin("testNaN")
				
		def data = observationService.findByIdAndRowId(b.id.toInteger(),null)		
		
		assert 1 == data.size()		
		assert 2 == data[0]['channel_nr']
		assert now.getTime() == data[0]['result_time'].getTime()
		assert 1.0F == data[0]['result_value']
		
		b.delete(flush:true);
		
	}	

}
