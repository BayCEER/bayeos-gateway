package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;


/**
 * Entity implementation class for Entity: VirtualChannel
 *
 */
@Entity
@Table(name = "virtual_channel")
public class VirtualChannel extends UniqueEntity {	
		
		
	public VirtualChannel() {
		super();
	}

	@Column(columnDefinition = "text")
	private String nr;
	
	@OneToMany(mappedBy="virtualchannel", cascade=CascadeType.ALL)
	private List<ChannelBinding> channelBindings;
	
	@ManyToOne()
	@JoinColumn(name="channel_function_id")
	private ChannelFunction channelFunction;
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="board_id", nullable = false)
	private Board board;
		
		
	public Object eval(ScriptEngine engine, Map<String,Object> values) throws ScriptException {				
		Map<String,Object> params = new HashMap<>(channelBindings.size());	
		for(ChannelBinding b:channelBindings){			
			String pName = b.getParameter().getName();
			// Overwrite channel value with constant
			params.put(pName,(b.getValue() != null)?b.getValue():values.get(b.getNr()));
		}						
		return engine.eval(getChannelFunction().getBody(), new SimpleBindings(params));		
	}


	
	public String getDeclaration(){
		StringBuffer b = new StringBuffer(nr);
		b.append("=");
		b.append(channelFunction.getName());
		b.append("(");
		boolean first = true;
		for(ChannelBinding cb:channelBindings){
			if (!first){
				b.append(",");				
			} else {
				first = false;				
			}			
			b.append(cb.getParameter().getName()).append("=").append((cb.getValue()!=null)?cb.getValue():cb.getNr());
		}		
		b.append(")");
		return b.toString();
	}


	@Override
	public String toString() {
		return getDeclaration();
	}



	public String getNr() {
		return nr;
	}



	public void setNr(String nr) {
		this.nr = nr;
	}



	public List<ChannelBinding> getChannelBindings() {
		return channelBindings;
	}



	public void setChannelBindings(List<ChannelBinding> channelBindings) {
		this.channelBindings = channelBindings;
	}



	public ChannelFunction getChannelFunction() {
		return channelFunction;
	}



	public void setChannelFunction(ChannelFunction channelFunction) {
		this.channelFunction = channelFunction;
	}



	public Board getBoard() {
		return board;
	}



	public void setBoard(Board board) {
		this.board = board;
	}

	
}
