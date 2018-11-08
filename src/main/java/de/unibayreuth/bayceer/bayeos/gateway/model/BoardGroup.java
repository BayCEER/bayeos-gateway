package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;


@Entity
public class BoardGroup extends NamedDomainEntity {
		
	
	@OneToMany(mappedBy="boardGroup", cascade=CascadeType.PERSIST)
	List<Board> boards;

	@Column(name="db_folder_id")
	Integer dbFolderId;
		
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
			
	@Transient
	public Integer getBoardCount(){
		return boards.size();
	};
		
	@Transient
	public Date getLastResultTime(){
		Date d = null;
		if (boards != null){
			for(Board b:boards){
				if (b.getLastResultTime() != null){					
					if (d!=null){
						if (b.getLastResultTime().after(d)){
							d = b.getLastResultTime();
						}							
					} else {
						d = b.getLastResultTime();
					}
				}
			}
		}
		return d;		
	};

					
	
}
