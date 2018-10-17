package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class KnotPoint  extends UniqueEntity {   
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2336185331669319124L;
	
	
    Float x;
    Float y;   
    
	@ManyToOne
	@JoinColumn(name="spline_id",nullable=false)
	private Spline spline;	
	
	
	public KnotPoint(Float x, Float y){
		this.x = x;
		this.y = y;		
	}


	public KnotPoint() {
		super();
	}


	public Float getX() {
		return x;
	}


	public void setX(Float x) {
		this.x = x;
	}


	public Float getY() {
		return y;
	}


	public void setY(Float y) {
		this.y = y;
	}


	public Spline getSpline() {
		return spline;
	}


	public void setSpline(Spline spline) {
		this.spline = spline;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
}
