package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.Channel;
import de.unibayreuth.bayceer.bayeos.gateway.model.ObsRow;
import de.unibayreuth.bayceer.bayeos.gateway.repo.datatable.ChannelRepository;

@RestController
public class ChannelRestController extends AbstractController{
	
	@Autowired 
	ChannelRepository repo;
	
	@Autowired
	UserSession userSession;

	private JdbcTemplate jdbcTemplate;
	
	@Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
	
	@RequestMapping(path="/rest/channel", method = RequestMethod.GET)	
	public List<ObsRow> getData(@RequestParam final Long id, @RequestParam(required = false) final Long startTime, @RequestParam(required = false) final Long endTime, @RequestParam(required = false) final Long lastRowId ){		
		Channel c = repo.findById(id).orElseThrow(()-> new EntityNotFoundException());		
		if (c == null) throw new EntityNotFoundException("Entity not found");
		checkWrite(c.getBoard());								
		if (lastRowId == null) {			
			Date endDate = (endTime==null)?new Date():new Date(endTime);
			Date startDate;
			if (startTime == null) {
				GregorianCalendar d = new GregorianCalendar();
				d.setTime(endDate);
				d.add(GregorianCalendar.HOUR, -24);
				startDate = d.getTime() ;
			} else {
				startDate = new Date(startTime);
			}		
			if (startDate.after(endDate)) {
				throw new IllegalArgumentException("Start time after end time.");
			}					
			String sql = "select id,(EXTRACT(EPOCH FROM result_time)*1000)::int8 as millis,real_value(result_value,spline_id) as value " +
					"from " + 
					"(select o.id,o.result_time,o.result_value,c.spline_id from observation o, channel c " +  
					"where c.id = o.channel_id and c.id = ? and o.result_time between ? and ? " +
					"UNION " +  
					"select o.id, o.result_time , o.result_value, c.spline_id from observation_exp o, channel c " + 
					"where c.id = o.channel_id and c.id = ? and o.result_time between ? and ?" + 			
					") z order by 2 asc";			
			return jdbcTemplate.query(sql,new Object[]{id,startDate,endDate,id,startDate,endDate}, new ChannelNameMapper());						
		} else {
			String sql = "select id,(EXTRACT(EPOCH FROM result_time)*1000)::int8 as millis,real_value(result_value,spline_id) as value " + 
					"from " +
					"(select o.id,o.result_time,o.result_value,c.spline_id " +
					"from observation o,channel c where o.channel_id = c.id and c.id = ? and o.id > ? " +
					"union " + 
					"select o.id,o.result_time,o.result_value,c.spline_id " +
					"from observation_exp o, channel c where o.channel_id = c.id and c.id = ? and o.id > ?) a " +
					"order by 2 asc;";						
			return jdbcTemplate.query(sql,new Object[]{id,lastRowId,id,lastRowId}, new ChannelNameMapper());
		}					
	}
	
	private static final class ChannelNameMapper implements RowMapper<ObsRow> {		
		@Override
		public ObsRow mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new ObsRow(rs.getLong("id"),rs.getLong("millis"),rs.getFloat("value"));
		}
	}

}
