package gateway

import groovy.sql.Sql
import java.sql.Array;
import java.sql.Connection
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map
import de.unibayreuth.bayceer.parser.simpleformat.CSVParser
import de.unibayreuth.bayceer.parser.simpleformat.Column
import de.unibayreuth.bayceer.parser.simpleformat.ColumnIndexParser
import de.unibayreuth.bayceer.parser.simpleformat.SimpleFormat
import javax.xml.bind.JAXB


class FileUploadController {
	def dataSource

	def boardService
	
	def upload() {
		[formatNames:FileFormat.executeQuery("select name from FileFormat order by 1")]
	}

	
	def save() {
			def file = request.getFile("file")
			int id
			int rows = 0;
			if(file && file.size>0){
				FileFormat fileFormat = FileFormat.findByName(params.format_name)				
				Board board = Board.findByOriginAndFrameStorage(params.board_origin,true)																
				if (board == null){					
					id  = boardService.createBoardByFileFormat(params.board_origin,fileFormat)					
				} else {
					id = board.id					
				}			
				if (params.zipped){
					File temp = File.createTempFile("bd_" + id, ".zip")					
					file.transferTo(temp)										
					rows = boardService.copyZippedData(id, new CSVParser(fileFormat.getXml()),temp)
					temp.delete();
					
				} else {
				 	rows = boardService.copyData(id, new CSVParser(fileFormat.getXml()), file.getInputStream())
				}				
			}
				
			flash.message = "${rows} imported."
			redirect(action: "upload")
	}
		
			
	
}	
	
	
		

