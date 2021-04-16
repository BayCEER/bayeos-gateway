package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;

public class Point implements Serializable {
	Float x;
	Float y;
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
	public Point() {		
							
	}
	
	public Point(Float x, Float y) {
		this.x = x;
		this.y = y;
	}
	
	
}