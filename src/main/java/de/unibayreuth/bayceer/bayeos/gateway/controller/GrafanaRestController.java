package de.unibayreuth.bayceer.bayeos.gateway.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.unibayreuth.bayceer.bayeos.gateway.model.grafana.DataPoint;
import de.unibayreuth.bayceer.bayeos.gateway.model.grafana.Metric;
import de.unibayreuth.bayceer.bayeos.gateway.model.grafana.Query;
import de.unibayreuth.bayceer.bayeos.gateway.model.grafana.Search;
import de.unibayreuth.bayceer.bayeos.gateway.model.grafana.Target;

@RestController
@RequestMapping("/grafana")
public class GrafanaRestController {
	
	private JdbcTemplate jdbcTemplate;
	private final Logger log = Logger.getLogger(GrafanaRestController.class);
	
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
    @GetMapping
	public String index(){
		return "{\"msg\":\"This is a grafana Endpoint.\"}";
	}
	
    @PostMapping("/search")	
	public List<String> search(@RequestBody Search search){
    	log.debug("Search:" + search);
		return jdbcTemplate.queryForList("select path from channel_path order by 1",String.class);
	}
	
	@PostMapping("/annotations")
	public String annotations(){
		log.debug("Annotations:");
		return null;
	}
	
	@PostMapping("/query")
	public List<Metric> query(@RequestBody Query q){ 
		log.debug("Query:" + q);
    	List<Metric> ret = new ArrayList<Metric>(q.getTargets().size());    	
    	for(Target t:q.getTargets()){
    		List<DataPoint> points = jdbcTemplate.query("select * from get_grafana_obs(?,?,?,?) order by result_time asc",
    				new Object[]{t.getName(),q.getInterval(),new Timestamp(q.getRange().getFrom().getTime()),new Timestamp(q.getRange().getTo().getTime())},
    				new RowMapper<DataPoint>(){
						@Override
						public DataPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
								return new DataPoint(rs.getFloat("result_value"),rs.getTimestamp("result_time").getTime());								
						}    		
    				}
    		);
    		Metric m = new Metric(t.getName());
    		m.setDatapoints(points);
    		log.debug("Metric:" + t.getName() + " Datapoints:" + points.size());
    		ret.add(m);
    	}    
    	return ret; 		
	}
	
	   

    
    
   
}

