package gateway.time;

import java.util.Calendar;

public class LastWeek extends MidnightBasedTimeInterval {
	public LastWeek() {
		super();	
		cal.set( Calendar.DAY_OF_WEEK, Calendar.MONDAY );
		endDate = cal.getTime();		
		cal.add(Calendar.DAY_OF_WEEK,-7);
		startDate = cal.getTime();		
	}
}
