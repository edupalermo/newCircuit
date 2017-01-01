package org.circuit.solution;

public class Clock {

	private boolean state = false;
	
	public boolean thick() {
		boolean answer = state;
		state = !state;
		return answer;
	}
	
}
