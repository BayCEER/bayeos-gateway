package de.unibayreuth.bayceer.bayeos.gateway.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;

import com.fasterxml.jackson.annotation.JsonView;

import javassist.expr.Instanceof;
import net.bytebuddy.utility.dispatcher.JavaDispatcher.Instance;

@MappedSuperclass
public abstract class UniqueEntity implements Serializable {
	
	public UniqueEntity() {
		super();
	}
			
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)	
	@JsonView(DataTablesOutput.View.class)	
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	
	@Override
		public boolean equals(Object obj) {	
	        if (obj == null) {
	            return false;
	        }
	        
	        if (obj.getClass() != this.getClass()) {
	            return false;
	        }
		    return this.getId().equals(((UniqueEntity)obj).getId()) ;
		}	
		
	
}
