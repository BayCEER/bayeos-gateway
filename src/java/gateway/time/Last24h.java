package gateway.time;

import java.util.Calendar;

public class Last24h extends AbstractTimeInterval{
	
	public Last24h() {
		super();
		endDate = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY,-24);
		startDate = cal.getTime();		
	}


}
