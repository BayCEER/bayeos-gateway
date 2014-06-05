package gateway.time;

public class MidnightBasedTimeInterval extends AbstractTimeInterval {
	public MidnightBasedTimeInterval() {
		super();
		toMidnight(cal);
	}
}
