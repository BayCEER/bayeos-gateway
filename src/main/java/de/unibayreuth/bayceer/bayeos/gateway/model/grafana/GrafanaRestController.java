package de.unibayreuth.bayceer.bayeos.gateway.model.grafana;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.unibayreuth.bayceer.bayeos.gateway.UserSession;

@RestController
@RequestMapping("/grafana")
public class GrafanaRestController {
	
	private final Logger log = Logger.getLogger(GrafanaRestController.class);
	private DataSource dataSource;
	
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Autowired
    UserSession userSession;
	
    @GetMapping
	public String index(){
		return "{\"msg\":\"This is a grafana endpoint.\"}";
	}
	
    @PostMapping("/search")	
	public List<String> search(@RequestBody Search search){
    	log.debug("Search:" + search);
    	List<String> f = new ArrayList<String>(100);
    	try (Connection con = dataSource.getConnection();
    			PreparedStatement st = con.prepareStatement("select path from channel_path where path ilike ? and (domain_id = ? or ? is null) order by 1")){    		
    		st.setString(1, String.format("%%%s%%",search.getTarget()));    		
    		if (userSession.getDomain()==null) {
    			st.setNull(2, java.sql.Types.BIGINT);
    			st.setNull(3, java.sql.Types.BIGINT);
    		} else {
    			st.setLong(2, userSession.getDomain().getId());	
    			st.setLong(3, userSession.getDomain().getId());
    		}
    		
    		ResultSet rs = st.executeQuery();    		
    		while(rs.next()) {
    			f.add(rs.getString(1));    			
    		}    		    		    		    		    	
    	} catch (SQLException e) {
    		log.error(e.getMessage());    		
    	} 
    	return f;
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
    	try (Connection con = dataSource.getConnection();PreparedStatement st = con.prepareStatement("select * from get_grafana_obs(?,?,?,?,?) order by result_time asc")   ){    	
    		for(Target t:q.getTargets()){        		
        		List<DataPoint> points = new ArrayList<>(100);        		        		 
        		if (userSession.getDomain() == null) {
        			st.setNull(1,java.sql.Types.BIGINT);    			
        		} else {
        			st.setLong(1,userSession.getDomain().getId());
        		}        		
        		st.setString(2,t.getName());
        		st.setString(3,q.getInterval());        		      		
        		st.setTimestamp(4,new Timestamp(q.getRange().getFrom().getTime()));
        		st.setTimestamp(5,new Timestamp(q.getRange().getTo().getTime()));        		        		        		        		
        		
        		ResultSet rs = st.executeQuery();    		
        		while(rs.next()) {
        			points.add(new DataPoint(rs.getFloat("result_value"),rs.getTimestamp("result_time").getTime()));			
        		}        		
        		Metric m = new Metric(t.getName());
        		m.setDatapoints(points);
        		log.debug("Metric:" + t.getName() + " Datapoints:" + points.size());
        		ret.add(m);
    		}        		    		
    	} catch (SQLException e) {
    		log.error(e.getMessage());
    	}
    		
    	return ret; 		
	}
	
	   

    
    
   
}

