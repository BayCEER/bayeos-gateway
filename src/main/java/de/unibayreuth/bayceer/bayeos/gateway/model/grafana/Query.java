package de.unibayreuth.bayceer.bayeos.gateway.model.grafana;

import java.util.List;


public class Query {
	private Long panelId;
	private String interval;
	private String format;
	private Long maxDataPoints;
	private Range range;
	private List<Target> targets;
	
	
	public Query() {
	
	}

	public Long getPanelId() {
		return panelId;
	}

	public void setPanelId(Long panelId) {
		this.panelId = panelId;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}


	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Long getMaxDataPoints() {
		return maxDataPoints;
	}

	public void setMaxDataPoints(Long maxDataPoints) {
		this.maxDataPoints = maxDataPoints;
	}

	public Range getRange() {
		return range;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public List<Target> getTargets() {
		return targets;
	}

	public void setTargets(List<Target> targets) {
		this.targets = targets;
	}
	
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();		
		b.append("panelId:");
		b.append(panelId);
		b.append(" interval:");
		b.append(interval);		
		b.append(" from:");		
		b.append(range.getFrom());
		b.append(" to:");
		b.append(range.getTo());		
		b.append(" maxDataPoints:");
		b.append(maxDataPoints);		
		b.append(" targets:[");		
		long i = 0;
		for(Target t:targets){
			if (i!=0){
				b.append(",");				
			}
			b.append(t.getName());
			i++;
		}
		b.append("]");		
		return b.toString();
	}
}