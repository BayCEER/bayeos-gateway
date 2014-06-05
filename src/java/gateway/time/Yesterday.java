package gateway.time;

import java.util.Calendar;

public class Yesterday extends AbstractTimeInterval {
	
	public Yesterday() {
		super();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		endDate = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY,-24);
		startDate = cal.getTime();
	}

	

}
