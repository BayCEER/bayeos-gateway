package gateway.time;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public abstract class AbstractTimeInterval {
	
	protected Date startDate;
	protected Date endDate;
	
	public Date getStartDate() {
		return startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	
	public void toMidnight(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);		
	}

	
	protected Calendar cal;

	public AbstractTimeInterval() {
		cal = new GregorianCalendar();
		cal.setTimeZone(TimeZone.getTimeZone("GMT+1"));
	}
	@Override
	public String toString() {		
		return this.getClass().getSimpleName() + "\t" + startDate + "\t" + endDate;
	}
	
	
//	public static void main(String[] args) {
//		
//		System.out.println(new Today());
//		System.out.println(new Last24h());
//		System.out.println(new Yesterday());
//		
//		System.out.println(new ThisWeek());
//		System.out.println(new LastWeek());
//		
//		System.out.println(new ThisMonth());
//		System.out.println(new LastMonth());
//		
//		System.out.println(new ThisYear());
//		System.out.println(new LastYear());
//		
//	}
}
