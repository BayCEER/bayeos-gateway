package de.unibayreuth.bayceer.bayeos.gateway.model.grafana;

import java.util.List;

public class Metric {
	
	String target;
	
	public Metric(String target) {
		super();
		this.target = target;
	}
	
	List<DataPoint> datapoints;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public List<DataPoint> getDatapoints() {
		return datapoints;
	}

	public void setDatapoints(List<DataPoint> datapoints) {
		this.datapoints = datapoints;
	}

	
}
