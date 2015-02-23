package gateway

class DeleteJobConfig {
	
	Interval maxResultInterval =  new Interval("60 days")
	Interval maxMessageInterval = new Interval("60 days")
	Interval maxStatInterval = new Interval("60 days")
	Integer delayInterval	= 120	
	Boolean enabled = true
	
	static embedded = ['maxResultInterval', 'maxMessageInterval', 'maxStatInterval']
}
