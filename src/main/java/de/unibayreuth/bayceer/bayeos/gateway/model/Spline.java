package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class Spline extends UniqueEntity { 
	
	
    String name;
    
	@OneToMany(mappedBy="spline")
	@OrderBy("x ASC")
	List<KnotPoint> knotPoints;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<KnotPoint> getKnotPoints() {
		return knotPoints;
	}

	public void setKnotPoints(List<KnotPoint> knotPoints) {
		this.knotPoints = knotPoints;
	}
	         
   
    
}
