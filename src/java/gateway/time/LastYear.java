package gateway.time;

import java.util.Calendar;

public class LastYear extends MidnightBasedTimeInterval {
	public LastYear() {
		super();
		cal.set(Calendar.DAY_OF_YEAR,1);
		endDate = cal.getTime();
		cal.add(Calendar.YEAR,-1);
		startDate = cal.getTime();		
	}
}
