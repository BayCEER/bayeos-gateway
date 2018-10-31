package de.unibayreuth.bayceer.bayeos.gateway.model.grafana;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class Range {
	@DateTimeFormat(iso=ISO.DATE)
	private Date from;
	@DateTimeFormat(iso=ISO.DATE)
	private Date to;

	public Range() {
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}
}