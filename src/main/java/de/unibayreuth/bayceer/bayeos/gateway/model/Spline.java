package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Formula;

@Entity
public class Spline extends UniqueEntity implements Serializable { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1238818881211009181L;

	String name;
    
	@OneToMany(mappedBy="spline", cascade=CascadeType.ALL)
	@OrderBy("x ASC")
	List<KnotPoint> knotPoints;
	
			
	@Formula("(select count(c.id) > 0  from channel c where c.spline_id = id)")
	Boolean locked;
			
	
	public Spline() {		
	}
	
	public Spline(String name){
		this.name = name;
	}
		
	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

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
	        
	
	@Override
	public String toString() {
		return this.name;
	}
    
}
