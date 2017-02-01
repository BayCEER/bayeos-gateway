package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Formula;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
public class Board extends CheckDevice {
			
	@JsonView(DataTablesOutput.View.class)
	String origin;
	
	@JsonView(DataTablesOutput.View.class)
	String name;
	
	@JsonView(DataTablesOutput.View.class)
	/* Column is managed by import routines */
	@Column(insertable=false,updatable=false)
	Short  lastRssi;
	
	@JsonView(DataTablesOutput.View.class)	
	/* Column is managed by import routines !! */
	@Column(insertable=false,updatable=false)
	Date lastResultTime;
	
	Integer dbFolderId;
	Boolean dbAutoExport = false;
	Boolean denyNewChannels = false;
	
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="board", cascade=CascadeType.REMOVE)
	List<Channel> channels;
		
	@Formula("(select count(*) from board_comment where board_comment.board_comments_id = id)")
	Integer commentCount;
	
	@Formula("(select count(*) from message where message.origin = origin)")
	Integer messageCount;
	
	@JsonView(DataTablesOutput.View.class)	
	@Formula("(select get_board_status(id))")
	Integer channelStatus;
	
	@ManyToOne	
	@JoinColumn(name = "board_group_id")
	@JsonView(DataTablesOutput.View.class)
	BoardGroup boardGroup;
	
	
	public Board(){
		super();
	}
		
	public Board(String origin) {
		this.origin = origin;
	}

	
	public Integer getStatus() {
		if (excludeFromNagios) {
			return null;
		} else {
			return channelStatus;
		}
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Short getLastRssi() {
		return lastRssi;
	}

	public void setLastRssi(Short lastRssi) {
		this.lastRssi = lastRssi;
	}

	public Date getLastResultTime() {
		return lastResultTime;
	}

	public void setLastResultTime(Date lastResultTime) {
		this.lastResultTime = lastResultTime;
	}

	public Integer getDbFolderId() {
		return dbFolderId;
	}

	public void setDbFolderId(Integer dbFolderId) {
		this.dbFolderId = dbFolderId;
	}

	public Boolean getDbAutoExport() {
		return dbAutoExport;
	}

	public void setDbAutoExport(Boolean dbAutoExport) {
		this.dbAutoExport = dbAutoExport;
	}

	public Boolean getDenyNewChannels() {
		return denyNewChannels;
	}

	public void setDenyNewChannels(Boolean denyNewChannels) {
		this.denyNewChannels = denyNewChannels;
	}

	

	public Integer getChannelStatus() {
		return channelStatus;
	}

	public void setChannelStatus(Integer channelStatus) {
		this.channelStatus = channelStatus;
	}

	public BoardGroup getBoardGroup() {
		return boardGroup;
	}

	public void setBoardGroup(BoardGroup boardGroup) {
		this.boardGroup = boardGroup;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	
	public Integer getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(Integer messageCount) {
		this.messageCount = messageCount;
	}
	

	public Channel findOrCreateChannel(String nr) {				
		for(Channel c:channels){
			if (c.getNr().equals(nr)){
				return c;
			}
		}		
		Channel c = new Channel(this,nr);
		channels.add(c);		
		return c;
	}

	public List<Channel> getChannels() {
		return channels;
	}
	
	public List<Long> getChannelIds(){
		List<Long> ret = new ArrayList<>();		
		for(Channel c:channels){
			ret.add(c.getId());
		}
		return ret;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}
		
	
	 

	
}
