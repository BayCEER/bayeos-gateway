package gateway.time;

import java.util.Calendar;

public class LastMonth extends MidnightBasedTimeInterval {
	
	public LastMonth() {	
		super();
		cal.set(Calendar.DAY_OF_MONTH,1);
		endDate = cal.getTime();
		cal.add(Calendar.MONTH, -1);
		startDate = cal.getTime();
	}

}
