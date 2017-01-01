package org.circuit.solution;

import java.util.ArrayList;

public abstract class TrainingSet extends ArrayList<Solution> {

	private static final long serialVersionUID = 1L;
	
	public int getInputSize() {
		return this.get(0).get(0).getInput().length;
	}
	
	public int getOutputSize() {
		return this.get(0).get(0).getOutput().length;
	}
	

}
