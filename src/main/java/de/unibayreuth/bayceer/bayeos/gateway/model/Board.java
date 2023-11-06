package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Formula;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class Board extends NamedDomainEntity {
	
	// Completeness Check
	@Column(name="sampling_interval")
	Integer samplingInterval;	
	
	// Check Delay
	@Column(name="check_delay")
	Integer checkDelay;	
	
	// Disable Alerts   	
	@Column(name="exclude_from_nagios")	
	Boolean excludeFromNagios = false;
	
	// Export valid records only
	@Column(name="filter_critical_values")
	Boolean filterCriticalValues = false;
	
	@Transient	
	Long domainIdCreated;
		
	@JsonView(DataTablesOutput.View.class)
	String origin;
	
	@JsonView(DataTablesOutput.View.class)
	/* Column is managed by import routines */
	@Column(insertable=false,updatable=false)	
	Short  lastRssi;
	
	@JsonView(DataTablesOutput.View.class)	
	/* Column is managed by import routines !! */
	@Column(insertable=false,updatable=false)
	Date lastResultTime;
	
	@OneToMany(mappedBy="board")
	List<Notification> notifications;
	
	
	Boolean export = false;
	
	Boolean denyNewChannels = false;
	
	@OneToMany(mappedBy="board", cascade=CascadeType.REMOVE)	
	List<Channel> channels;
		
	@OneToMany(mappedBy="board", cascade=CascadeType.REMOVE)
	List<VirtualChannel> virtualChannels;
				
	@JsonView(DataTablesOutput.View.class)	
	@Formula("(select get_board_status(id))")
	Integer channelStatus;
	
	@ManyToOne	
	@JoinColumn(name = "board_group_id")
	@JsonView(DataTablesOutput.View.class)	
	BoardGroup boardGroup;
	
	@ManyToOne  
	@JoinColumn(name = "influx_connection_id")
	InfluxConnection influxConnection;
	
	@Column(name="influx_measurement")
	String influxMeasurement;
	
				
	public Integer getStatus() {
		if (getExcludeFromNagios()) {
			return null;
		} else {
			return channelStatus;
		}
	}

	public Channel findOrCreateChannel(String nr) {				
		for(Channel c:channels){
			if (c.getNr().equals(nr)){
				return c;
			}
		}		
		Channel c = new Channel();
		c.setBoard(this);
		c.setNr(nr);
		channels.add(c);		
		return c;
	}
	

	
	public List<String> getChannelNrs() {
		List<String> ret = new ArrayList<String>();		
		for(Channel c:channels){
			ret.add(c.getNr());
		}
		return ret;
	}
	
	public List<Long> getChannelIds(){
		List<Long> ret = new ArrayList<>();		
		for(Channel c:channels){
			ret.add(c.getId());
		}
		return ret;
	}
	
	public Boolean getNagios() {
		return !excludeFromNagios;
	}
	public void setNagios(Boolean nagios) {
		this.excludeFromNagios = !nagios;
}

	public Integer getSamplingInterval() {
		return samplingInterval;
	}

	public void setSamplingInterval(Integer samplingInterval) {
		this.samplingInterval = samplingInterval;
	}

	public Integer getCheckDelay() {
		return checkDelay;
	}

	public void setCheckDelay(Integer checkDelay) {
		this.checkDelay = checkDelay;
	}

	public Boolean getExcludeFromNagios() {
		return excludeFromNagios;
	}

	public void setExcludeFromNagios(Boolean excludeFromNagios) {
		this.excludeFromNagios = excludeFromNagios;
	}

	public Boolean getFilterCriticalValues() {
		return filterCriticalValues;
	}

	public void setFilterCriticalValues(Boolean filterCriticalValues) {
		this.filterCriticalValues = filterCriticalValues;
	}

	public Long getDomainIdCreated() {
		return domainIdCreated;
	}

	public void setDomainIdCreated(Long domainIdCreated) {
		this.domainIdCreated = domainIdCreated;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Short getLastRssi() {
		return lastRssi;
	}

	public void setLastRssi(Short lastRssi) {
		this.lastRssi = lastRssi;
	}
	
	public Short getRssiLevel() {
		
		if (lastRssi == null) {
			return null;			
		}		
		if (lastRssi < -85) {
			return 1;
		} else if (lastRssi >= -85 && lastRssi < -75) {
			return 2;			
		} else if (lastRssi >= -75 && lastRssi < -65) {
			return 3;			
		} else if (lastRssi >= -65 && lastRssi < -45) {			
			return 4;			
		} else if (lastRssi >= -45) {			
			return 5;
		} else {
			return 0;
		}
	}

	public Date getLastResultTime() {
		return lastResultTime;
	}

	public void setLastResultTime(Date lastResultTime) {
		this.lastResultTime = lastResultTime;
	}
	

	public Boolean getExport() {
		return export;
	}

	public void setExport(Boolean export) {
		this.export = export;
	}

	public Boolean getDenyNewChannels() {
		return denyNewChannels;
	}

	public void setDenyNewChannels(Boolean denyNewChannels) {
		this.denyNewChannels = denyNewChannels;
	}

	public List<Channel> getChannels() {
		return channels;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	public List<VirtualChannel> getVirtualChannels() {
		return virtualChannels;
	}

	public void setVirtualChannels(List<VirtualChannel> virtualChannels) {
		this.virtualChannels = virtualChannels;
	}

	public Integer getChannelStatus() {
		return channelStatus;
	}

	public void setChannelStatus(Integer channelStatus) {
		this.channelStatus = channelStatus;
	}

	public BoardGroup getBoardGroup() {
		return boardGroup;
	}

	public void setBoardGroup(BoardGroup boardGroup) {
		this.boardGroup = boardGroup;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}
	
	public boolean hasEmptyChannelNames() {
		for(Channel c: channels) {
			if (c.getName() != null) {
				return false;
			}
		}
		return true;
	}

    public InfluxConnection getInfluxConnection() {
        return influxConnection;
    }

    public void setInfluxConnection(InfluxConnection influxConnection) {
        this.influxConnection = influxConnection;
    }

    public String getInfluxMeasurement() {
        return influxMeasurement;
    }

    public void setInfluxMeasurement(String measurement) {
        this.influxMeasurement = measurement;
    }

   

   
}
