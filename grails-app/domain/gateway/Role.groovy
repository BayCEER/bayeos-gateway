package gateway

class Role {

	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority blank: false, unique: true
	}
	    
	
	public getName() {
		String name
		if (authority.startsWith("ROLE_") && authority.size() > 5 ) {
			name = authority.substring(5)
		} else {
			name = authority
		}
		return name.toLowerCase().capitalize()
	}
}
