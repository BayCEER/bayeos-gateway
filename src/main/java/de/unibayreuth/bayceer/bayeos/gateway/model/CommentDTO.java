package de.unibayreuth.bayceer.bayeos.gateway.model;

public class CommentDTO {
		
	public CommentDTO() {
		super();
	}
	
	public CommentDTO(Long id, Long boardID, Long userID, Long insertTime, String content) {
		super();
		this.id = id;
		this.boardID = boardID;
		this.userID = userID;
		this.insertTime = insertTime;
		this.content = content;			
	}
	
	Long id;
	Long boardID;
	Long userID;
	Long insertTime;	
	String content;		
	

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getBoardID() {
		return boardID;
	}
	public void setBoardID(Long boardID) {
		this.boardID = boardID;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public Long getUserID() {
		return userID;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public Long getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Long insertTime) {
		this.insertTime = insertTime;
	}

	

}
