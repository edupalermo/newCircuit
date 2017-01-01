package org.circuit.circuit;

import org.circuit.port.Port;

public class CircuitRandomGenerator {
	
	public static Circuit randomGenerate(int inputSize, int quantityOfRandomPort) {
		Circuit circuit = new Circuit(inputSize);
		for (int i = 0; i < quantityOfRandomPort; i++) {
			circuit.add(Port.random(circuit.size()));
		}
		
		return circuit;
	}
	
	public static void randomEnrich(Circuit circuit, int quantityOfRandomPort) {
		for (int i = 0; i < quantityOfRandomPort; i++) {
			circuit.add(Port.random(circuit.size()));
		}
	}
	
	

}
