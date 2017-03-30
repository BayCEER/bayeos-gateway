package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;


@Entity
public class BoardGroup extends UniqueEntity{
	String name;
		
	@OneToMany(mappedBy="boardGroup", cascade=CascadeType.PERSIST)
	List<Board> boards;

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
	
	// @Formula("(select count(*) from board where board.board_group_id = id)")	
	@Transient
	public Integer getBoardCount(){
		return boards.size();
	};
	
	
	// @Formula("(select max(board.last_result_time) from board where board.board_group_id = id)")
	@Transient
	public Date getLastResultTime(){
		Date d = null;
		if (boards != null){
			for(Board b:boards){
				if (d == null){
					d = b.getLastResultTime();
				} else {					
					if (b.getLastResultTime().after(d)){
						d = b.getLastResultTime();
					}					
				}				
			}
		}
		return d;		
	};

	// @Formula("(select get_group_status(id))")	
	@Transient
	public Integer getGroupStatus(){
		Integer ret = null;		
		if (boards != null){
			for(Board b:boards){
				if (ret == null) {
					ret = b.getStatus();
				} else {
					if (b.getStatus() > ret){
						ret = b.getStatus();
					}	
				}				
			}
		}
		return ret;
	};
	
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
	
}
