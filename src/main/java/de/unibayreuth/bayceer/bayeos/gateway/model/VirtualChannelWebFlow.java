package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

public class VirtualChannelWebFlow implements Serializable {

	public VirtualChannelWebFlow() {
	}

	// Channel nr
	public String nr;

	// ChannelFunction index in lists
	public Integer index;

	public ChannelFunction function;

	// Parameter binding
	public Map<String, Binding> binding;

	public void init(ChannelFunction f) {
		this.function = f;
		this.binding = new Hashtable<>();
		for(ChannelFunctionParameter p:f.getParameters()) {
			binding.put(p.getName(), new Binding());			
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

	public ChannelFunction getFunction() {
		return function;
	}

	public void setFunction(ChannelFunction function) {
		this.function = function;
	}

	public Map<String, Binding> getBinding() {
		return binding;
	}

	public void setBinding(Map<String, Binding> binding) {
		this.binding = binding;
	}

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
}
