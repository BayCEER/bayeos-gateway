package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class BoardTemplate extends NamedDomainEntity  {
		
	private static final long serialVersionUID = 1784086458814880224L;
	
	
			


	String description;	
	String revision;
	
	
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
		

	@Column(name="data_sheet")	
	String dataSheet;

	@Column(name="date_created")	
	Date dateCreated = new Date();

	@Column(name="last_updated")	
	Date lastUpdated;

	@OneToMany(mappedBy="boardTemplate", cascade=CascadeType.ALL)	
	List<ChannelTemplate> templates = new ArrayList<>();
			
	public void addTemplate(ChannelTemplate t){
		if (templates == null){
			templates = new ArrayList<>(10);
		}
		t.setBoardTemplate(this);
		templates.add(t);
	}
	
	public void removeTemplate(int index){
		templates.remove(index);
	}
	
	public void removeTemplate(ChannelTemplate t){
		templates.remove(t);
	}
	
	public ChannelTemplate getTemplate(int index){
		return templates.get(index);
	}
	
	public Integer getTemplateCount(){
		return templates.size();
	}
	
	public Boolean getNagios() {
		return !excludeFromNagios;
	}
	public void setNagios(Boolean nagios) {
		this.excludeFromNagios = !nagios;
}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
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

	public String getDataSheet() {
		return dataSheet;
	}

	public void setDataSheet(String dataSheet) {
		this.dataSheet = dataSheet;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public List<ChannelTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(List<ChannelTemplate> templates) {
		this.templates = templates;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	


}
