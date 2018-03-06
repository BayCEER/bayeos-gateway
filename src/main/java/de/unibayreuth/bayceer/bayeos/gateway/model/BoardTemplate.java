package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class BoardTemplate extends CheckDevice  {
	
	
	private static final long serialVersionUID = 1784086458814880224L;
	
	String name;
	String description;
	String revision;

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
	
	public void removeTemplate(Integer index){
		templates.remove(index);
	}
	
	public void removeTemplate(ChannelTemplate t){
		templates.remove(t);
	}
	
	public ChannelTemplate getTemplate(int index){
		return templates.get(index);
	}
	
		
	public String getName() {
		return name;
	}



	public BoardTemplate() {
		super();
	}



	public BoardTemplate(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}



	public void setName(String name) {
		this.name = name;
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


	public void setTemplates(List<ChannelTemplate> channelTemplates) {
		this.templates = channelTemplates;
	}

	public Integer getTemplateCount(){
		return templates.size();
	}

}
