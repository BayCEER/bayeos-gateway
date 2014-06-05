package gateway.time;

import java.util.Calendar;

public class Today extends AbstractTimeInterval {

	public Today() {
		super();		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		startDate = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY,24);
		endDate = cal.getTime();
	}
}
