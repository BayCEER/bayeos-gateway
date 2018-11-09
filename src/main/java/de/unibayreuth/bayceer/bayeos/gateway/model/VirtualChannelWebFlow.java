package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VirtualChannelWebFlow implements Serializable {

	public VirtualChannelWebFlow() {		
	}

	public String nr;
	public Integer index;	
	
	public class Binding implements Serializable {
		
		public Binding() {		
		}
		
		private String nr;
		private Float value;
		public String getNr() {
			return nr;
		}
		public void setNr(String nr) {
			this.nr = nr;
		}
		public Float getValue() {
			return value;
		}
		public void setValue(Float value) {
			this.value = value;
		}
	}
		 
	public List<Binding> bindings = new ArrayList<Binding>(5);
	
	public void init(ChannelFunction f){
		for(ChannelFunctionParameter p:f.getParameters()){
			bindings.add(new Binding());			
		}
	}
		
	public String getNr() {
		return nr;
	}


	public void setNr(String nr) {
		this.nr = nr;
	}


	public Integer getIndex() {
		return index;
	}


	public void setIndex(Integer index) {
		this.index = index;
	}

	public List<Binding> getBindings() {
		return bindings;
	}

	public void setBindings(List<Binding> bindings) {
		this.bindings = bindings;
	}


	
		
	
}
