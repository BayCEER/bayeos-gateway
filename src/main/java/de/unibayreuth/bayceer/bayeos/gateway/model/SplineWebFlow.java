package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SplineWebFlow implements Serializable {
	String name;
	Domain domain;
	List<Point> points = new ArrayList<Point>();
			
	public SplineWebFlow() {

	}
	
	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}


	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

