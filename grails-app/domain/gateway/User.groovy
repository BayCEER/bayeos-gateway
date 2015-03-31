package gateway

class User {

	transient springSecurityService

	String username
	String password
	boolean enabled = true
	
	boolean accountExpired = false
	boolean accountLocked = false
	boolean passwordExpired = false

	static constraints = {
		username blank: false, unique: true
		password blank: false
	}

	static mapping = {		
		table "users"		
		password column: '`password`'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}
	
	
	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}
	
	
	def beforeDelete() {
		UserRole.removeAll(this)
	}
	
	def String toString() {
		return username
	};
}
