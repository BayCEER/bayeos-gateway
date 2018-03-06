package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VirtualChannelWebFlow implements Serializable {

	public VirtualChannelWebFlow() {		
	}

	public String nr;
	public Integer index;	
	public List<String> channelNrs = new ArrayList<String>(5);
	
	public void init(ChannelFunction f){
		for(ChannelFunctionParameter p:f.getParameters()){
			channelNrs.add("");
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


	public List<String> getChannelNrs() {
		return channelNrs;
	}


	public void setChannelNrs(List<String> channelNrs) {
		this.channelNrs = channelNrs;
	}
	
	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("nr:").append(nr).append(" index:").append(index).append(" size:");
		if (channelNrs!=null){
			b.append(channelNrs.size());	
		} else {
			b.append(0);			
		}
		
		return b.toString();
	}
	
}
