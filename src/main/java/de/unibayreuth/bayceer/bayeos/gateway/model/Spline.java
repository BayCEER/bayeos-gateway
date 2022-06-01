package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

@Entity
public class Spline extends NamedDomainEntity {
	
			
	@OneToMany(mappedBy="spline", cascade=CascadeType.ALL)
	@OrderBy("x ASC")
	List<KnotPoint> knotPoints;	
	
	@Transient
	Boolean locked = false;
			
	public Spline(String string) {
		super(string);
	}
	
	public Spline() {
		super();
	}


	public void addKnotPoint(KnotPoint k){
		if (knotPoints == null){
			knotPoints = new ArrayList<>(10);
		}
		k.setSpline(this);				
		knotPoints.add(k);
	}
	
	
	public void removeKnotPoint(KnotPoint p){
		knotPoints.remove(p);
	}
	
	public void removeKnotPoint(int index){
		knotPoints.remove(index);
	}

	public List<KnotPoint> getKnotPoints() {
		return knotPoints;
	}

	public void setKnotPoints(List<KnotPoint> knotPoints) {
		this.knotPoints = knotPoints;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}
	  	

}
