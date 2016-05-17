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


/*
 * Run this tests with:
 * 
 */


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
		assertEquals(40000,row[0])
		db.close()		
		b.delete(flush:true);						
    }
	
	
	@Test
	void testMessageFrame() {
		String mv = "This is a test message";
		StringFrame msg = new StringFrame(FrameConstants.Message,mv)		
		frameService.saveFrames("testMessageFrame",new String(Base64.encodeBase64String(msg.getBytes())));
		Board b = Board.findByOrigin("testMessageFrame")		
		Message m = Message.findByOrigin(b.getOrigin())
		assertEquals(mv,m.getContent());		
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
		assertEquals(1,data.size())		
		assertEquals("2",data[0]['channel_nr'])
		assertEquals(now.getTime(),data[0]['result_time'].getTime())
		assertEquals(1.0f,data[0]['result_value'],0.1)				
		b.delete(flush:true);
		
	}	
	
	@Test 
	void labeledFrames() {
		Date now = new Date()		
		LabeledFrame f = new LabeledFrame(NumberType.UInt8,"{'c1':1.0,'c11':11.0,'c3':3.0}")		
		MillisecondTimestampFrame frame = new MillisecondTimestampFrame(now,f.getBytes())		
		frameService.saveFrames("labeledFrames",new String(Base64.encodeBase64String(frame.getBytes())))
		Board b = Board.findByOrigin("labeledFrames")
		def data = observationService.findByIdAndRowId(b.id.toInteger(),null)		
		
		assertEquals("c1",data[0]['channel_nr'])
		assertEquals(1.0f,data[0]['result_value'],0.1)
		assertEquals(now.getTime(),data[0]['result_time'].getTime())
		
		assertEquals("c11",data[1]['channel_nr'])
		assertEquals(11f,data[1]['result_value'],0.1)
		assertEquals(now.getTime(),data[1]['result_time'].getTime())
		
		assertEquals("c3",data[2]['channel_nr'])
		assertEquals(3f,data[2]['result_value'],0.1)
		assertEquals(now.getTime(),data[2]['result_time'].getTime())
				
		b.delete(flush:true)
				
	}

}
