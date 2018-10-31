package de.unibayreuth.bayceer.bayeos.gateway;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.format.Formatter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


public class TimeZoneFormatter implements Formatter<Date> {
	
	private TimeZone tz = TimeZone.getDefault();	
	
	public void setTimeZone(){
		RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();		
		Object ra = requestAttributes.getAttribute("tz", RequestAttributes.SCOPE_SESSION);
		if (ra != null){			
			tz = TimeZone.getTimeZone((String)ra);
		}
	}	
	
	public Date parse(String text, Locale locale) throws ParseException {
		setTimeZone();
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,locale);		
		formatter.setTimeZone(tz);
		return formatter.parse(text);				
	}
	
	public String print(Date date, Locale locale) {
		setTimeZone();
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,locale);		
		formatter.setTimeZone(tz);
		return formatter.format(date);
	}
	
	
}

