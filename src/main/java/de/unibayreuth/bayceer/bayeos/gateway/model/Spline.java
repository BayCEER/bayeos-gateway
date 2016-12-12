package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
class Spline extends UniqueEntity { 
	
	
    String name;
    
	@OneToMany(mappedBy="spline")
	List<KnotPoint> knotePoints;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<KnotPoint> getKnotePoints() {
		return knotePoints;
	}

	public void setKnotePoints(List<KnotPoint> knotePoints) {
		this.knotePoints = knotePoints;
	}
	         
   
    
}
