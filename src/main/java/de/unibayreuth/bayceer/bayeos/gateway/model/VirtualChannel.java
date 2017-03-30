package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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
	
	
	@Column(columnDefinition = "text")
	private String nr;
	private static final long serialVersionUID = 1L;
	
	@OneToMany(mappedBy="virtualchannel", cascade=CascadeType.ALL)
	private List<ChannelBinding> channelBindings;
	
	@ManyToOne()
	@JoinColumn(name="channel_function_id")
	private ChannelFunction channelFunction;
	
	@ManyToOne()
	@JoinColumn(name="board_id")
	private Board board;
		
	public VirtualChannel() {
		super();
	}
	
	

	public ChannelFunction getChannelFunction() {
		return channelFunction;
	}

	public void setChannelFunction(ChannelFunction function) {
		this.channelFunction = function;
	}
	
	public Object eval(ScriptEngine engine, Map<String,Object> values) throws ScriptException {				
		Map<String,Object> params = new HashMap<>(channelBindings.size());	
		// System.out.println(MapUtils.toString(values));
		for(ChannelBinding b:channelBindings){
			String nr = b.getParameter().getName();
			Object o = values.get(b.getNr());
			if (o == null) throw new ScriptException("Missing input value for parameter " + nr + "."); 
			params.put(nr,o);
		}						
		return engine.eval(getChannelFunction().getBody(), new SimpleBindings(params));		
	}



	public String getNr() {
		return nr;
	}


	public void setNr(String nr) {
		this.nr = nr;
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
			b.append(cb.getParameter().getName()).append("=").append(cb.getNr());
		}		
		b.append(")");
		return b.toString();
	}



	public List<ChannelBinding> getChannelBindings() {
		return channelBindings;
	}



	public void setChannelBindings(List<ChannelBinding> channelBindings) {
		this.channelBindings = channelBindings;
	}



	public Board getBoard() {
		return board;
	}


	public void setBoard(Board board) {
		this.board = board;
	}

	@Override
	public String toString() {
		return getDeclaration();
	}

	
}
