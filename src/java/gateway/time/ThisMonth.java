package gateway.time;

import java.util.Calendar;

public class ThisMonth extends MidnightBasedTimeInterval {

	public ThisMonth() {
		super();
		cal.set(Calendar.DAY_OF_MONTH,1);
		startDate = cal.getTime();
		cal.add(Calendar.MONTH,1);
		endDate = cal.getTime();			
	}
			
}
