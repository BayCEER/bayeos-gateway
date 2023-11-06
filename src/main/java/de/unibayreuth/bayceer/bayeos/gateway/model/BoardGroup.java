package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;


@Entity
public class BoardGroup extends NamedDomainEntity {
		
	
	@OneToMany(mappedBy="boardGroup", cascade=CascadeType.PERSIST)
	@OrderBy("name")
	List<Board> boards;
	
	
	@OneToMany(mappedBy="boardGroup", cascade=CascadeType.REMOVE)
	List<Notification> notifications;
		
	public List<Board> getBoards() {
		return boards;
	}

	public void setBoards(List<Board> boards) {
		this.boards = boards;
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

		
	@Transient 
	public Integer getGroupStatus() {
		Integer s = null;		
		if (boards != null){
			for(Board b:boards){
				if (b.getStatus()!=null) {
					if (s != null) {
						s = (b.getStatus()>s)?b.getStatus():s;
					} else {
						s = b.getStatus();
					}					
				};				
			}
		}
		return s;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	};
	
}
