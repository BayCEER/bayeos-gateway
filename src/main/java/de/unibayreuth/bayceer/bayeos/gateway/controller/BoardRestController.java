package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
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
			String sql = "select a.* from (" +
				"select o.id as rowId, channel.id as channelId, (EXTRACT(EPOCH FROM o.result_time)*1000)::int8 as millis,real_value(o.result_value,channel.spline_id) as value " + 
				"from board join channel on board.id = channel.board_id " + 
				"join all_observation o on channel.id = o.channel_id " +
				"left outer join spline on spline.id = channel.spline_id " +
				"where board.id = ? and o.result_time > (board.last_result_time - '60 min'::interval) order by 1 desc limit 600) a order by 1 asc;"; 						
			return jdbcTemplate.query(sql,new Object[]{boardId},new ObservationMapper());			
		} else {
			String sql = "select o.id as rowId, channel.id as channelId, (EXTRACT(EPOCH FROM o.result_time)*1000)::int8 as millis,real_value(o.result_value,channel.spline_id) as value " +
					"from board join channel on board.id = channel.board_id " +
					"join all_observation o on channel.id = o.channel_id " + 
					"left outer join spline on spline.id = channel.spline_id " + 
					"where board.id = ? and o.id > ? order by 1 asc;";			
			return jdbcTemplate.query(sql,new Object[]{boardId,lastRowId}, new ObservationMapper());
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
