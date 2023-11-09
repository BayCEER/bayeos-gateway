package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.sql.DataSource;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import de.unibayreuth.bayceer.bayeos.gateway.DomainFilter;
import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.Board;
import de.unibayreuth.bayceer.bayeos.gateway.model.Observation;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.BoardRepository;

@RestController
public class BoardRestController extends AbstractController {
		
	
	@Autowired
	DataTableSearch boardSearch;

	@Autowired
	public DomainFilter domainFilter;
	
	@Autowired 
	public BoardRepository repo;
	
	@Autowired
	public UserSession userSession;

			
	private JdbcTemplate jdbcTemplate;
	

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    	
	// See https://datatables.net/manual/server-side
	// Module: https://github.com/darrachequesne/spring-data-jpa-datatables
	@JsonView(DataTablesOutput.View.class)
	@RequestMapping(path ="/rest/boards", method = RequestMethod.POST)
	public DataTablesOutput<Board> findBoards(@Valid @RequestBody DataTablesInput input) {				
		boardSearch.setInput(input);				
		return repo.findAll(userSession.getUser(),domainFilter,input);	
	}
	
	
	@RequestMapping(path ="/rest/board/{id}", method = RequestMethod.GET)
	public ResponseEntity getBoard(@PathVariable Long id) {
	    Board board = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());  ;       
        User user = userSession.getUser();
        if (!user.inNullDomain()) {
            checkRead(board);   
        }           
        return new ResponseEntity<Board>(board,HttpStatus.OK);
	}
	
	@RequestMapping(path ="/rest/boards", method = RequestMethod.GET)
	public List<Board> getBoards(@RequestParam List<Long> id) {	    	    	        	   
        return repo.findAllByIds(userSession.getUser(),domainFilter,id);	    	    	    
	}
	
	
	// See https://docs.spring.io/spring/docs/current/spring-framework-reference/html/jdbc.html
	@RequestMapping(path="/rest/boards/chartData", method = RequestMethod.GET)
	public List<Observation> chartData(@RequestParam Long boardId, @RequestParam Long lastRowId){	
				
		Board b = repo.findOne(userSession.getUser(),boardId);	
						
		if (b == null) {
			throw new EntityNotFoundException();
		}
				
		if (lastRowId == 0){														 					
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

			return jdbcTemplate.query(sql,new Object[]{boardId,qt,boardId,qt},new ObservationMapper());			
		} else {
			String sql = "select id as rowId, channel_id as channelId, (EXTRACT(EPOCH FROM result_time)*1000)::int8 as millis,real_value(result_value,spline_id) as value " + 
			"from (select o.id, c.id as channel_id, o.result_time,o.result_value, c.spline_id " +
			"from observation o, channel c where o.channel_id = c.id and c.board_id = ? and o.id > ? " +
			"union " + 
			"select o.id, c.id as channel_id, o.result_time , o.result_value, c.spline_id " +
			"from observation_exp o, channel c where o.channel_id = c.id and c.board_id = ? and o.id > ?) a " +
			"order by id asc;";	
			
			return jdbcTemplate.query(sql,new Object[]{boardId,lastRowId,boardId,lastRowId}, new ObservationMapper());
		}		
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
