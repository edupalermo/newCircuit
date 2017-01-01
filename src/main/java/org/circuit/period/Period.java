package org.circuit.period;

public class Period {
	
	private long tick;
	private long delta;
	
	public Period(long delta) {
		this.delta = delta;
		this.tick = System.currentTimeMillis();
	}
	
	public boolean alarm() {
		boolean answer = (System.currentTimeMillis() - tick) > delta;
		if (answer) {
			this.tick = System.currentTimeMillis();
		}
		return answer;
	}

}
