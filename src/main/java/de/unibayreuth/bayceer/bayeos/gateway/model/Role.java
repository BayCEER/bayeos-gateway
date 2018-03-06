package de.unibayreuth.bayceer.bayeos.gateway.model;

public enum Role {
	ADMIN("ADMIN"),
	USER("USER"),
	IMPORT("IMPORT"),
	CHECK("CHECK");
	
	public static final Role[] ALL = { ADMIN, USER, IMPORT, CHECK };
	
	
	private String name;
	
	public String getName() {
		return name;
	}
	
	private Role(final String name){
		this.name = name;		
	}
	
	@Override
	public String toString() {
		return name;
	}
}
