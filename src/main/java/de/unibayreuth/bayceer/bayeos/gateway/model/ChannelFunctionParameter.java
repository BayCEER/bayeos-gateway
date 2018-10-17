package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: ScriptParameter
 *
 */
@Entity
@Table(name = "channel_function_parameter")

public class ChannelFunctionParameter extends UniqueEntity implements Serializable {

	@Column(columnDefinition = "text")
	private String name;
	private static final long serialVersionUID = 1L;

	@Column(columnDefinition = "text")
	private String description;

	@ManyToOne()
	@JoinColumn(name = "channel_function_id")
	private ChannelFunction channelFunction;


	public ChannelFunctionParameter() {
		super();
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ChannelFunction getChannelFunction() {
		return channelFunction;
	}

	public void setChannelFunction(ChannelFunction channelFunction) {
		this.channelFunction = channelFunction;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
