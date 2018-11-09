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
	
	
	@Column
	private Float value;
	
	private static final long serialVersionUID = 1L;
	
	@ManyToOne
	@JoinColumn(name="virtual_channel_id")
	private VirtualChannel virtualchannel;
	
	
	@ManyToOne
	@JoinColumn(name="channel_function_param_id")
	private ChannelFunctionParameter parameter;
	
	


	public String getNr() {
		return nr;
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


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public Float getValue() {
		return value;
	}


	public void setValue(Float value) {
		this.value = value;
	}


	
	
	   
}
