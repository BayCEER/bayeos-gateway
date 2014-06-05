package gateway.time;

import java.util.Calendar;

public class ThisYear extends MidnightBasedTimeInterval {
	
	public ThisYear() {
		super();
		cal.set(Calendar.DAY_OF_YEAR,1);
		startDate = cal.getTime();
		cal.add(Calendar.YEAR,1);
		endDate = cal.getTime();		
	}

}
