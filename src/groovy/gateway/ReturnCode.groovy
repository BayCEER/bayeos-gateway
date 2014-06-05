package gateway;


public enum ReturnCode {
	
	 OK(0), WARN(1), CRITICAL(2), UNKNOWN(3);

	private int code;
	
	private ReturnCode(int code){
		this.code = code;
	}
	
	public int getValue(){
		return code;
	}
	
	
	public static ReturnCode getCode(int value) {
		for(ReturnCode c : ReturnCode.values()) {
			if(c.getValue() == value) {
				return c;
			}
		}
		return null;
	}
}
