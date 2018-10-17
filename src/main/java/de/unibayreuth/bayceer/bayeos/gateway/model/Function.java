package de.unibayreuth.bayceer.bayeos.gateway.model;

import javax.persistence.Entity;


@Entity
public class Function extends NamedDomainEntity {
		public Function() {
			super();
		}
		
		public Function(String name) {
			this.name = name;
		}
	    
}
