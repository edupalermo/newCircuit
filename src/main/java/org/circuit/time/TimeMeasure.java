package org.circuit.time;

public class TimeMeasure {
	
	
	public static final long MSECOND = 1;
	public static final long SECOND = 1000 * MSECOND;
	public static final long MINUTE = 60 * SECOND;
	public static final long HOUR = 60 * MINUTE;
	
	private long start;
	
	public TimeMeasure() {
		this.start = System.currentTimeMillis();
	}

	
	public long elapsed() {
		return System.currentTimeMillis() - this.start;
	}

	
	public String formatedElapsed() {
		long delta = System.currentTimeMillis() - this.start;
		
		if (delta == 0) {
			return "0MS";
		}
		
		StringBuffer sb = new StringBuffer();
		delta = treat(delta, sb, "H", HOUR);
		delta = treat(delta, sb, "M", MINUTE);
		delta = treat(delta, sb, "S", SECOND);
		delta = treat(delta, sb, "MS", MSECOND);
		
		return sb.toString();
	}
	
	public void reset() {
		this.start = System.currentTimeMillis();
	}
	
	private long treat(long delta, StringBuffer sb, String tm, long base) {
		long amount = 0;
		
		if (delta >= base) {
			amount = delta / base;
			sb.append(amount).append(tm);
		}
		
		return delta - (amount * base);
	}

}
