package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Formula;


@Entity
public class BoardGroup extends NamedDomainEntity {

	@OneToMany(mappedBy="boardGroup", cascade=CascadeType.PERSIST)
	List<Board> boards;

	@Column(name="db_folder_id")
	Integer dbFolderId;
		
	@Formula("(select count(*) from board where board.board_group_id = id)")
	Integer boardCount;
	
	@Formula("(select max(board.last_result_time) from board where board.board_group_id = id)")
	Date lastResultTime;
	
	@Formula("(select max(get_board_status(board.id)) from board where board.board_group_id = id)")
	Integer groupStatus;

	public List<Board> getBoards() {
		return boards;
	}

	public void setBoards(List<Board> boards) {
		this.boards = boards;
	}

	public Integer getDbFolderId() {
		return dbFolderId;
	}

	public void setDbFolderId(Integer dbFolderId) {
		this.dbFolderId = dbFolderId;
	}

	public Integer getBoardCount() {
		return boardCount;
	}

	public void setBoardCount(Integer boardCount) {
		this.boardCount = boardCount;
	}

	public Date getLastResultTime() {
		return lastResultTime;
	}

	public void setLastResultTime(Date lastResultTime) {
		this.lastResultTime = lastResultTime;
	}

	public Integer getGroupStatus() {
		return groupStatus;
	}

	public void setGroupStatus(Integer groupStatus) {
		this.groupStatus = groupStatus;
	}
					
	
}
