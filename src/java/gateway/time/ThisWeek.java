package gateway.time;

import java.util.Calendar;

public class ThisWeek extends MidnightBasedTimeInterval {
	
	public ThisWeek() {
		super();					
		cal.set( Calendar.DAY_OF_WEEK, Calendar.MONDAY );
		startDate = cal.getTime();
		cal.add(Calendar.DAY_OF_WEEK,7);
		endDate = cal.getTime();				
	}

}
