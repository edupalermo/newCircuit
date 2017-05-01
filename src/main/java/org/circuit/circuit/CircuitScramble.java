package org.circuit.circuit;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import org.circuit.port.Port;
import org.circuit.port.PortInput;
import org.circuit.solution.TrainingSet;

public class CircuitScramble {

	public static Circuit join(TrainingSet trainingSet, Circuit c1, Circuit c2) {
		if (c1 == c2) {
			c2 = (Circuit) c1.clone();
		}
		Circuit answer = realJoin(c1, c2); 
		
		//CircuitUtils.useLowerPortsWithSameOutput(trainingSet, answer);
		//CircuitUtils.simplify(trainingSet, answer);
		
		return answer;
	}
	
	public static Circuit scramble(TrainingSet trainingSet, Circuit c1, Circuit c2) {
		if (c1 == c2) {
			c2 = (Circuit) c1.clone();
		}
		Circuit answer = realScramble(c1, c2); 
		
		//CircuitUtils.useLowerPortsWithSameOutput(trainingSet, answer);
		//CircuitUtils.simplify(trainingSet, answer);
		
		return answer;
	}
	
	
	private static Circuit realScramble(Circuit c1, Circuit c2) {
		
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		Map<Integer, Integer> translation = new TreeMap<Integer, Integer>();
		
		final int inputSize = getInputSize(c1);
		
		for (int i = 0; i < inputSize; i++) {
			translation.put(i, random.nextInt(c1.size()));
		}
		
		for (int i = inputSize; i < c2.size(); i++) {
			
			Port port = (Port) c2.get(i).clone();
			
			port.translate(translation);
			
			translation.put(i, c1.size());
			
			c1.add(port);
		}
		return c1;
	}

	private static Circuit realJoin(Circuit c1, Circuit c2) {
		
		Map<Integer, Integer> translation = new TreeMap<Integer, Integer>();
		
		final int inputSize = getInputSize(c1);
		
		for (int i = 0; i < inputSize; i++) {
			translation.put(i, i);
		}
		
		for (int i = inputSize; i < c2.size(); i++) {
			Port port = (Port) c2.get(i).clone();
			port.translate(translation);
			translation.put(i, c1.size());
			c1.add(port);
		}
		return c1;
	}

	
	
	private static int getInputSize(Circuit c) {
		int i = 0;
		
		while ((i < c.size()) && (c.get(i) instanceof PortInput)) {
			i++;
		};
		
		return i;
	}
	
	
	

}
