package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.Observation;
import de.unibayreuth.bayceer.bayeos.gateway.repo.BoardRepository;

@RestController
public class BoardRestController {
	
	
	@Autowired
	BoardRepository repo;
	
	private JdbcTemplate jdbcTemplate;
	
	private static Logger log = Logger.getLogger(BoardRestController.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	// See https://datatables.net/manual/server-side
	// Module: https://github.com/darrachequesne/spring-data-jpa-datatables
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path ="/rest/boards", method = RequestMethod.POST)
	public DataTablesOutput<Board> findBoards(@Valid @RequestBody DataTablesInput input) {
		return repo.findAll(input);
	}
	
	
	// See https://docs.spring.io/spring/docs/current/spring-framework-reference/html/jdbc.html
	@RequestMapping(path="/rest/boards/chartData", method = RequestMethod.GET)
	public List<Observation> chartData(@RequestParam Long boardId, @RequestParam Long lastRowId){				
		if (lastRowId == 0){					
			Board b = repo.findOne(boardId);							 						
			Date lrt = (b.getLastResultTime() == null) ? new Date(): b.getLastResultTime();				
			GregorianCalendar gc = new GregorianCalendar();			
			gc.setTime(lrt);			
			gc.add(GregorianCalendar.HOUR,-2);						
			Date qt = gc.getTime();						
			String sql = "select id as rowId, channel_id as channelId, (EXTRACT(EPOCH FROM result_time)*1000)::int8 as millis,real_value(result_value,spline_id) as value " +
			"from " + 
			"(select o.id, c.id as channel_id, o.result_time, o.result_value, c.spline_id from observation o, channel c " +  
			"where c.id = o.channel_id and c.board_id = ? and o.result_time > ? " +
			"UNION " +  
			"select o.id, c.id as channel_id, o.result_time , o.result_value, c.spline_id from observation_exp o, channel c " + 
			"where c.id = o.channel_id and c.board_id = ? and o.result_time > ?" + 			
			") z order by 1 asc";									
			log.debug(sql);
			return jdbcTemplate.query(sql,new Object[]{boardId,qt,boardId,qt},new ObservationMapper());			
		} else {
			String sql = "select id as rowId, channel_id as channelId, (EXTRACT(EPOCH FROM result_time)*1000)::int8 as millis,real_value(result_value,spline_id) as value " + 
			"from (select o.id, c.id as channel_id, o.result_time,o.result_value, c.spline_id " +
			"from observation o, channel c where o.channel_id = c.id and c.board_id = ? and o.id > ? " +
			"union " + 
			"select o.id, c.id as channel_id, o.result_time , o.result_value, c.spline_id " +
			"from observation_exp o, channel c where o.channel_id = c.id and c.board_id = ? and o.id > ?) a " +
			"order by id asc;";	
			log.debug(sql);
			return jdbcTemplate.query(sql,new Object[]{boardId,lastRowId,boardId,lastRowId}, new ObservationMapper());
		}		
	}
	
	@RequestMapping(path="/rest/boards/findByOrigin", method = RequestMethod.GET)
	public ResponseEntity findByOrigin(@RequestParam String origin){
		Board b =  repo.findByOrigin(origin);
		if (b == null) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sf.setTimeZone(tz);
		JSONObject o = new JSONObject();
		o.put("id", b.getId());
		o.put("name", b.getName());
		o.put("origin", b.getOrigin());
		if (b.getLastResultTime() != null){
			o.put("lastResultTime", sf.format(b.getLastResultTime()));
		} 
		
		final HttpHeaders httpHeaders= new HttpHeaders();
	    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity(o.toString(),httpHeaders, HttpStatus.OK);
		
	}
	
	
	
	private static final class ObservationMapper implements RowMapper<Observation> {		
		public Observation mapRow(ResultSet rs, int rowNum) throws SQLException {
			Observation o = new Observation();
				o.setRowId(rs.getLong("rowId"));
				o.setChannelId(rs.getLong("channelId"));
				o.setMillis(rs.getLong("millis"));
				o.setValue(rs.getFloat("value"));
			return o;
		}
	}
	
	

}
