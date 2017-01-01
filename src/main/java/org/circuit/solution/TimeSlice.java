package org.circuit.solution;

import java.io.Serializable;

public class TimeSlice implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final boolean[] input;
	private final boolean[] output;
	
	public TimeSlice(boolean[] input, boolean[] output) {
		this.input = input;
		this.output =  output;
	}

	public boolean[] getInput() {
		return input;
	}
	

	public boolean[] getOutput() {
		return output;
	}
	
}
