package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Notification extends UniqueEntity {
		
	@ManyToOne()
	@JoinColumn(name="board_group_id")
	private BoardGroup boardGroup;

	@ManyToOne()
	@JoinColumn(name="board_id")
	private Board board;
	
				
	@ManyToOne()
	@JoinColumn(name="contact_id")
	private Contact contact;

	public BoardGroup getBoardGroup() {
		return boardGroup;
	}

	public void setBoardGroup(BoardGroup boardGroup) {
		this.boardGroup = boardGroup;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}
	
		

}
	
