package org.circuit.period;

public class Period {
	
	private long tick;
	private long delta;
	
	public Period(long delta) {
		this.delta = delta;
		this.tick = System.currentTimeMillis();
	}
	
	public boolean alarm() {
		long current = System.currentTimeMillis()
		boolean answer = (current - tick) > delta;
		if (answer) {
			this.tick = current;
		}
		return answer;
	}

}
