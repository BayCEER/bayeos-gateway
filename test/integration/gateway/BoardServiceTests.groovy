package gateway

import static org.junit.Assert.*
import org.junit.*

class BoardServiceTests {
	
	def boardService
		   
	@Test
    void createBoardByFileFormat() {
//		
//		String xml = """<?xml version="1.0" encoding="UTF-8"?>
//		<sflformat name="eddy format micrometeorology">
//		<column num="0" name="result_time" type="date" format="yyMMddHHmmss" beginIndex="0" endIndex="11"/>
//		<column num="1" name="x" type="int" beginIndex="46" endIndex="51"/>
//		<column num="2" name="y" type="int" beginIndex="56" endIndex="61"/>
//		<column num="3" name="z" type="int" beginIndex="66" endIndex="71"/>
//		<column num="4" name="t" type="int" beginIndex="76" endIndex="81"/>
//		<column num="5" name="e1" type="int" beginIndex="86" endIndex="91"/>
//		<column num="6" name="e2" type="int" beginIndex="96" endIndex="101"/>
//		<column num="7" name="e3" type="int" beginIndex="106" endIndex="111"/>
//		<column num="8" name="e4" type="int" beginIndex="116" endIndex="121"/>
//		<column num="9" name="e5" type="int" beginIndex="126" endIndex="131"/>
//		<column num="10" name="e6" type="int" beginIndex="136" endIndex="141"/>
//		<column num="11" name="e7" type="int" beginIndex="146" endIndex="151"/>
//		<column num="12" name="e8" type="int" beginIndex="156" endIndex="161"/>
//		</sflformat>"""		
//		
//		FileFormat d = new FileFormat(name:"DUMMY", xml:xml)								
//		int id = boardService.createBoardByFileFormat("EDDY:1",d)										
//		Board b = Board.get(id)		
//		assertNotNull(b)		
//		b.delete();
    }
}
