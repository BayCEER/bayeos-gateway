package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "channel_function")
public class ChannelFunction extends UniqueEntity {
	
	
	public ChannelFunction() {
		super();
	}
	
			
	@Column(columnDefinition="text")
	String name;
	
	@Column(columnDefinition="text")
	String body;
	

	@OneToMany(mappedBy="channelFunction", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	private List<ChannelFunctionParameter> params = new ArrayList<>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<ChannelFunctionParameter> getParameters() {
	    return params;
	}

	public void setParameters(List<ChannelFunctionParameter> params) {
	    this.params = params;
	}
	
	public String getFunctionHead(){		
		StringBuffer buf = new StringBuffer(name);
		buf.append("(");
		boolean first = true;
		for(ChannelFunctionParameter p:params){
			if (!first){
				buf.append(",");
			} else {
				first = false;
			}
			buf.append(p.getName());
		}
		buf.append(")");
		return buf.toString();
	}
	
	public String toString(){
		return getName();
	}
	
}