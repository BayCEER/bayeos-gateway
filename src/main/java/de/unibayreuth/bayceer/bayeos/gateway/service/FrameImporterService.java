package de.unibayreuth.bayceer.bayeos.gateway.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.copy.PGCopyOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import bayeos.frame.FrameParserException;
import bayeos.frame.Parser;
import de.unibayreuth.bayceer.bayeos.gateway.marshaller.CopyWriter;

@Service
public class FrameImporterService {
	
	private Logger log = Logger.getLogger(FrameImporterService.class);
		
	@Autowired
	private DataSource dataSource;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public boolean save(String sender, List<String> frames){		
		PGCopyOutputStream p = null;
		DataOutputStream out = null;				
		long r = 0;
		try (Connection con = dataSource.getConnection()){			
			CopyManager cm = ((PGConnection)con.unwrap(PGConnection.class)).getCopyAPI();
			CopyIn cin = cm.copyIn("COPY frame(origin,frame_time,type,rssi,value,validchecksum) FROM STDIN WITH BINARY");			
			p = new PGCopyOutputStream(cin);			
			out = new DataOutputStream(p);			
			CopyWriter cw = new CopyWriter(out);
			cw.writeHeader();
			Date now = new Date();
			for(String frame:frames){				
				Map<String,Object> res = Parser.parseBase64(frame,now,sender,null);
				cw.startRow(6);
				cw.writeText((String) res.get("origin"));				
				cw.writeTimestamp((long)res.get("ts"));				
				cw.writeText((String)res.get("type"));								
				if (res.get("rssi") != null){
					cw.writeInteger((Integer)res.get("rssi"));	
				} else {
					cw.writeInteger(null);
				}				
				cw.writeJSONB(mapper.writeValueAsString(res.get("value")));											
				cw.writeBoolean((Boolean)res.get("validchecksum"));
				r++;
			}
			cw.writeEnd();								
		} catch (SQLException | FrameParserException |  IOException e){
			log.error(e.getMessage());
			return false;
		} finally {
			try {
				p.close();
			} catch (IOException e) {				
			}			
		}
		log.info(r +  " frames saved.");
		return true;		
	}
	
	
}
