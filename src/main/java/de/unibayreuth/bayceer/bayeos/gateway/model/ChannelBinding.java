package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: ChannelBinding
 *
 */
@Entity
@Table(name="channel_binding")
public class ChannelBinding extends UniqueEntity implements Serializable {

	@Column(columnDefinition="text")
	private String nr;
	
	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	@JoinColumn(name="virtual_channel_id")
	private VirtualChannel virtualchannel;
	
	
	@ManyToOne
	@JoinColumn(name="channel_function_param_id")
	private ChannelFunctionParameter parameter;
	
	
	public ChannelBinding() {
		super();
	}   
	public String getNr() {
		return this.nr;
	}

	public void setNr(String nr) {
		this.nr = nr;
	}   
	public VirtualChannel getVirtualchannel() {
		return virtualchannel;
	}
	public void setVirtualchannel(VirtualChannel virtualchannel) {
		this.virtualchannel = virtualchannel;
	}
	public ChannelFunctionParameter getParameter() {
		return parameter;
	}
	public void setParameter(ChannelFunctionParameter parameter) {
		this.parameter = parameter;
	}
   
}
