package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Formula;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;


@Entity
public class BoardGroup extends UniqueEntity{
	String name;

	@Column(name="db_folder_id")
	Integer dbFolderId;

	@JsonView(DataTablesOutput.View.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getDbFolderId() {
		return dbFolderId;
	}

	public void setDbFolderId(Integer dbFolderId) {
		this.dbFolderId = dbFolderId;
	}
	
	@Formula("(select count(*) from board where board.board_group_id = id)")
	public Integer boardCount;
	
	@Formula("(select max(board.last_result_time) from board where board.board_group_id = id)")
	public Date lastResultTime;

	@Formula("(select get_group_status(id))")
	public Integer groupStatus;
	
}
