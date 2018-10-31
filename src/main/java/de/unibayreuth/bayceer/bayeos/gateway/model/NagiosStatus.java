package de.unibayreuth.bayceer.bayeos.gateway.model;

public enum NagiosStatus  {
	
	ok(0), warn(1), critical(2), unknown(3);

	private int code;
	
	private NagiosStatus(int code){
		this.code = code;
	}
	
	public int getValue(){
		return code;
	}
		
		
	public static NagiosStatus get(int value) {
		for(NagiosStatus c : NagiosStatus.values()) {
			if(c.getValue() == value) {
				return c;
			}
		}
		return null;
	}
	
	
}
