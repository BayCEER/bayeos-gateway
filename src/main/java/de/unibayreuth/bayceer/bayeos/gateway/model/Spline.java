package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;

import org.hibernate.annotations.Formula;

@Entity
public class Spline extends NamedDomainEntity {
	
	
	
		

	@OneToMany(mappedBy="spline", cascade=CascadeType.ALL)
	@OrderBy("x ASC")
	@NotNull()
	@Size(min=1)
	List<KnotPoint> knotPoints;	
			
	@Formula("(select count(c.id) > 0  from channel c where c.spline_id = id)")
	Boolean locked;
			
		
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
