package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
public class BoardTemplate extends CheckDevice  {
	
	String name;
	String description;
	String revision;

	@Column(name="data_sheet")
	String dataSheet;

	@Column(name="date_created")
	Date dateCreated;

	@Column(name="last_updated")
	Date lastUpdated;

	@OneToMany(fetch=FetchType.EAGER,mappedBy="boardTemplate")
	List<ChannelTemplate> channeTemplates;

}
